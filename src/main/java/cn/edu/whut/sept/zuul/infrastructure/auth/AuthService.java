package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.edu.whut.sept.zuul.infrastructure.InfrastructureServices;
import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;

/**
 * 用户注册、登录、登出与 Token 校验（F8 扩展，供 Swing / Vue / Spring REST 调用）。
 */
public class AuthService {

    private static final String PURPOSE_REGISTER = "register";
    private static final String PURPOSE_CHANGE_EMAIL = "change_email";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailDelivery emailDelivery;
    private final boolean devExposeCode;

    public AuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            EmailVerificationRepository verificationRepository,
            EmailDelivery emailDelivery,
            boolean devExposeCode) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.verificationRepository = verificationRepository;
        this.emailDelivery = emailDelivery;
        this.devExposeCode = devExposeCode;
    }

    public static AuthService createDefault() {
        return InfrastructureServices.getDefault().getAuthService();
    }

    public static AuthService create(H2Database database) {
        database.initializeSchema();
        return new AuthService(
            new UserRepository(database),
            new SessionRepository(database),
            new EmailVerificationRepository(database),
            new LoggingEmailDelivery(),
            true
        );
    }

    /**
     * 发送注册邮箱验证码。
     */
    public VerificationSendResult sendRegisterCode(String email) {
        String validationError = validateEmail(email);
        if (validationError != null) {
            return VerificationSendResult.failure(validationError);
        }
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            return VerificationSendResult.failure("该邮箱已被注册");
        }
        try {
            String code = verificationRepository.createCode(normalizedEmail, PURPOSE_REGISTER);
            boolean mailed = emailDelivery.sendVerificationCode(normalizedEmail, code);
            if (mailed) {
                return VerificationSendResult.success("验证码已发送至邮箱，请查收（含垃圾箱）", null);
            }
            if (devExposeCode) {
                return VerificationSendResult.success("验证码已生成（仅测试环境）", code);
            }
            return VerificationSendResult.failure(
                "验证码邮件发送失败，请在 application-local.properties 配置 SMTP 邮箱");
        } catch (IllegalArgumentException exception) {
            return VerificationSendResult.failure(exception.getMessage());
        }
    }

    /**
     * 注册新用户（需邮箱验证码与密码确认）。
     */
    public AuthResult register(
            String username,
            String password,
            String confirmPassword,
            String displayName,
            String email,
            String verificationCode) {
        String validationError = validateRegistration(
            username, password, confirmPassword, displayName, email, verificationCode);
        if (validationError != null) {
            return AuthResult.failure(validationError);
        }
        String normalizedUsername = username.trim().toLowerCase();
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByUsername(normalizedUsername)) {
            return AuthResult.failure("用户名已存在");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            return AuthResult.failure("该邮箱已被注册");
        }
        if (!verificationRepository.verifyAndConsume(normalizedEmail, verificationCode, PURPOSE_REGISTER)) {
            return AuthResult.failure("邮箱验证码错误或已过期");
        }
        String hash = PasswordHasher.hashPassword(password);
        String nickname = displayName.trim();
        long userId = userRepository.insert(normalizedUsername, hash, nickname, normalizedEmail);
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("注册后未找到用户"));
        AuthSession session = sessionRepository.createSession(account);
        return AuthResult.success(session, account);
    }

    /**
     * 控制台注册（跳过邮箱验证，仅供 Parser / Swing 备用入口）。
     */
    public AuthResult registerConsole(String username, String password, String displayName) {
        String consoleEmail = username.trim().toLowerCase() + "@console.local";
        return register(
            username,
            password,
            password,
            displayName,
            consoleEmail,
            consumeConsoleBypassCode(consoleEmail));
    }

    /**
     * 用户登录。
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.failure("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return AuthResult.failure("密码不能为空");
        }
        UserAccount account = userRepository.findByUsername(username.trim().toLowerCase()).orElse(null);
        if (account == null || !PasswordHasher.matches(password, account.getPasswordHash())) {
            return AuthResult.failure("用户名或密码错误");
        }
        AuthSession session = sessionRepository.createSession(account);
        return AuthResult.success(session, account);
    }

    /**
     * 按 Token 校验会话是否有效。
     */
    public Optional<AuthSession> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return sessionRepository.findValidByToken(token.trim());
    }

    /**
     * 获取用户资料。
     */
    public Optional<UserAccount> getProfile(String token) {
        return validateToken(token).flatMap(session -> userRepository.findById(session.getUserId()));
    }

    /**
     * 更新昵称。
     */
    public AuthResult updateDisplayName(String token, String displayName) {
        Optional<AuthSession> sessionOptional = validateToken(token);
        if (!sessionOptional.isPresent()) {
            return AuthResult.failure("请先登录");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            return AuthResult.failure("昵称不能为空");
        }
        AuthSession session = sessionOptional.get();
        userRepository.updateDisplayName(session.getUserId(), displayName.trim());
        UserAccount account = userRepository.findById(session.getUserId()).orElse(null);
        return AuthResult.success(session, account);
    }

    /**
     * 修改密码。
     */
    public AuthResult changePassword(
            String token, String oldPassword, String newPassword, String confirmPassword) {
        Optional<AuthSession> sessionOptional = validateToken(token);
        if (!sessionOptional.isPresent()) {
            return AuthResult.failure("请先登录");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            return AuthResult.failure("请输入当前密码");
        }
        String passwordError = validatePasswordPair(newPassword, confirmPassword);
        if (passwordError != null) {
            return AuthResult.failure(passwordError);
        }
        AuthSession session = sessionOptional.get();
        UserAccount account = userRepository.findById(session.getUserId()).orElse(null);
        if (account == null || !PasswordHasher.matches(oldPassword, account.getPasswordHash())) {
            return AuthResult.failure("当前密码不正确");
        }
        userRepository.updatePassword(session.getUserId(), PasswordHasher.hashPassword(newPassword));
        return AuthResult.success(session, userRepository.findById(session.getUserId()).orElse(account));
    }

    /**
     * 更新头像路径。
     */
    public AuthResult updateAvatar(String token, String avatarUrl) {
        Optional<AuthSession> sessionOptional = validateToken(token);
        if (!sessionOptional.isPresent()) {
            return AuthResult.failure("请先登录");
        }
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return AuthResult.failure("头像地址无效");
        }
        AuthSession session = sessionOptional.get();
        userRepository.updateAvatarUrl(session.getUserId(), avatarUrl.trim());
        UserAccount account = userRepository.findById(session.getUserId()).orElse(null);
        return AuthResult.success(session, account);
    }

    /**
     * 列出所有注册用户（用户管理页展示）。
     */
    public List<UserAccount> listUsers(String token) {
        if (!validateToken(token).isPresent()) {
            throw new IllegalArgumentException("请先登录");
        }
        return userRepository.listAll();
    }

    /**
     * 登出：删除服务端会话。
     */
    public void logout(String token) {
        if (token != null && !token.trim().isEmpty()) {
            sessionRepository.deleteByToken(token.trim());
        }
    }

    public EmailVerificationRepository getVerificationRepository() {
        return verificationRepository;
    }

    private String consumeConsoleBypassCode(String email) {
        return verificationRepository.createCode(email, PURPOSE_REGISTER);
    }

    private String validateRegistration(
            String username,
            String password,
            String confirmPassword,
            String displayName,
            String email,
            String verificationCode) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (username.trim().length() < 3) {
            return "用户名至少 3 个字符";
        }
        String passwordError = validatePasswordPair(password, confirmPassword);
        if (passwordError != null) {
            return passwordError;
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            return "昵称不能为空";
        }
        String emailError = validateEmail(email);
        if (emailError != null) {
            return emailError;
        }
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return "请输入邮箱验证码";
        }
        return null;
    }

    private String validatePasswordPair(String password, String confirmPassword) {
        if (password == null || password.length() < 6) {
            return "密码至少 6 位";
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "请确认密码";
        }
        if (!password.equals(confirmPassword)) {
            return "两次输入的密码不一致";
        }
        return null;
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "邮箱格式不正确";
        }
        return null;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    /**
     * 用户公开信息（不含密码）。
     */
    public static class UserPublicView {

        private final long userId;
        private final String username;
        private final String displayName;
        private final String email;
        private final String avatarUrl;
        private final String createdAt;

        public UserPublicView(UserAccount account) {
            this.userId = account.getId();
            this.username = account.getUsername();
            this.displayName = account.getDisplayName();
            this.email = account.getEmail();
            this.avatarUrl = account.getAvatarUrl();
            this.createdAt = account.getCreatedAt() != null ? account.getCreatedAt().toString() : null;
        }

        public long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static List<UserPublicView> toPublicViews(List<UserAccount> accounts) {
        return accounts.stream().map(UserPublicView::new).collect(Collectors.toList());
    }
}

package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.util.Optional;

import cn.edu.whut.sept.zuul.infrastructure.InfrastructureServices;
import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;

/**
 * 用户注册、登录、登出与 Token 校验（F8 扩展，供 Swing / Vue / Spring REST 调用）。
 */
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public static AuthService createDefault() {
        return InfrastructureServices.getDefault().getAuthService();
    }

    public static AuthService create(H2Database database) {
        database.initializeSchema();
        return new AuthService(new UserRepository(database), new SessionRepository(database));
    }

    /**
     * 注册新用户。
     *
     * @param username 登录名（唯一）
     * @param password 明文密码
     * @param displayName 游戏内显示昵称
     * @return 认证结果
     */
    public AuthResult register(String username, String password, String displayName) {
        String validationError = validateRegistration(username, password, displayName);
        if (validationError != null) {
            return AuthResult.failure(validationError);
        }
        String normalizedUsername = username.trim().toLowerCase();
        if (userRepository.existsByUsername(normalizedUsername)) {
            return AuthResult.failure("用户名已存在");
        }
        String hash = PasswordHasher.hashPassword(password);
        String nickname = displayName.trim();
        long userId = userRepository.insert(normalizedUsername, hash, nickname);
        UserAccount account = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("注册后未找到用户"));
        AuthSession session = sessionRepository.createSession(account);
        return AuthResult.success(session);
    }

    /**
     * 用户登录。
     *
     * @param username 登录名
     * @param password 明文密码
     * @return 认证结果
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
        return AuthResult.success(session);
    }

    /**
     * 按 Token 校验会话是否有效。
     *
     * @param token 会话 Token
     * @return 有效会话或 empty
     */
    public Optional<AuthSession> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return sessionRepository.findValidByToken(token.trim());
    }

    /**
     * 登出：删除服务端会话。
     *
     * @param token 会话 Token
     */
    public void logout(String token) {
        if (token != null && !token.trim().isEmpty()) {
            sessionRepository.deleteByToken(token.trim());
        }
    }

    private String validateRegistration(String username, String password, String displayName) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (username.trim().length() < 3) {
            return "用户名至少 3 个字符";
        }
        if (password == null || password.length() < 6) {
            return "密码至少 6 位";
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            return "昵称不能为空";
        }
        return null;
    }
}

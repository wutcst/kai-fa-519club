package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthResult;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthSession;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService.UserPublicView;
import cn.edu.whut.sept.zuul.infrastructure.auth.UserAccount;
import cn.edu.whut.sept.zuul.infrastructure.auth.VerificationSendResult;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.service.AuthServiceProvider;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;

/**
 * 认证与用户资料 REST 接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Path AVATAR_DIR =
        Paths.get("data", "uploads", "avatars").toAbsolutePath().normalize();

    private final AuthService authService;
    private final MultiplayerRoomService multiplayerRoomService;

    public AuthController(AuthServiceProvider authServiceProvider,
                          MultiplayerRoomService multiplayerRoomService) {
        this.authService = authServiceProvider.getAuthService();
        this.multiplayerRoomService = multiplayerRoomService;
    }

    @PostMapping("/register/code")
    public ApiResponse<Map<String, Object>> sendRegisterCode(@RequestBody Map<String, String> body) {
        VerificationSendResult result = authService.sendRegisterCode(body.get("email"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("message", result.getMessage());
        return ApiResponse.ok(data);
    }

    @PostMapping("/signup")
    public ApiResponse<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        AuthResult result = authService.register(
            body.get("username"),
            body.get("password"),
            body.get("confirmPassword"),
            body.get("displayName"),
            body.get("email"),
            body.get("verificationCode"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/signin")
    public ApiResponse<Map<String, Object>> signin(@RequestBody Map<String, String> body) {
        AuthResult result = authService.login(body.get("username"), body.get("password"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/signout")
    public ApiResponse<String> signout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        authService.validateToken(token).ifPresent(session ->
            multiplayerRoomService.handleUserLogout(session.getUserId()));
        authService.logout(token);
        return ApiResponse.ok("已登出");
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        return authService.getProfile(token)
            .map(account -> ApiResponse.ok(toProfileMap(account)))
            .orElseGet(() -> ApiResponse.fail("请先登录"));
    }

    @PostMapping("/profile/display-name")
    public ApiResponse<Map<String, Object>> updateDisplayName(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        AuthResult result = authService.updateDisplayName(token, body.get("displayName"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/profile/password")
    public ApiResponse<Map<String, Object>> changePassword(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        AuthResult result = authService.changePassword(
            token,
            body.get("oldPassword"),
            body.get("newPassword"),
            body.get("confirmPassword"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, Object>> uploadAvatar(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("请选择头像文件");
        }
        String contentType = file.getContentType();
        if (!isAllowedAvatarType(contentType)) {
            return ApiResponse.fail("仅支持 JPG / PNG / WebP 图片");
        }
        try {
            Files.createDirectories(AVATAR_DIR);
            String extension = extensionFor(contentType);
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = AVATAR_DIR.resolve(filename);
            Files.write(target, file.getBytes());
            String avatarUrl = "/uploads/avatars/" + filename;
            AuthResult result = authService.updateAvatar(token, avatarUrl);
            if (!result.isSuccess()) {
                Files.deleteIfExists(target);
                return ApiResponse.fail(result.getMessage());
            }
            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            data.put("session", toSessionMap(result));
            return ApiResponse.ok(data);
        } catch (IOException exception) {
            return ApiResponse.fail("头像上传失败");
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @GetMapping("/users")
    public ApiResponse<List<UserPublicView>> listUsers(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            List<UserAccount> users = authService.listUsers(token);
            return ApiResponse.ok(AuthService.toPublicViews(users));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    private Map<String, Object> toSessionMap(AuthResult result) {
        Map<String, Object> map = new HashMap<>();
        UserAccount account = result.getAccount();
        map.put("userId", result.getSession().getUserId());
        map.put("username", result.getSession().getUsername());
        map.put("displayName",
            account != null && account.getDisplayName() != null
                ? account.getDisplayName()
                : result.getSession().getDisplayName());
        map.put("token", result.getSession().getToken());
        map.put("expiresAt", result.getSession().getExpiresAt().toString());
        if (account != null) {
            map.put("email", account.getEmail());
            map.put("avatarUrl", account.getAvatarUrl());
        }
        return map;
    }

    private Map<String, Object> toProfileMap(UserAccount account) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", account.getId());
        map.put("username", account.getUsername());
        map.put("displayName", account.getDisplayName());
        map.put("email", account.getEmail());
        map.put("avatarUrl", account.getAvatarUrl());
        map.put("createdAt", account.getCreatedAt() != null ? account.getCreatedAt().toString() : null);
        return map;
    }

    private boolean isAllowedAvatarType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg")
            || contentType.equals("image/jpg")
            || contentType.equals("image/pjpeg")
            || contentType.equals("image/png")
            || contentType.equals("image/webp");
    }

    private String extensionFor(String contentType) {
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }
}

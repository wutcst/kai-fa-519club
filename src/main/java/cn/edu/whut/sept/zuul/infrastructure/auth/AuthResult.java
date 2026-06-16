package cn.edu.whut.sept.zuul.infrastructure.auth;

/**
 * 认证操作结果（注册 / 登录 / 校验）。
 */
public class AuthResult {

    private final boolean success;
    private final String message;
    private final AuthSession session;

    private AuthResult(boolean success, String message, AuthSession session) {
        this.success = success;
        this.message = message;
        this.session = session;
    }

    public static AuthResult success(AuthSession session) {
        return new AuthResult(true, "ok", session);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuthSession getSession() {
        return session;
    }
}

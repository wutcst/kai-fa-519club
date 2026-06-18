package cn.edu.whut.sept.zuul.infrastructure.auth;

/**
 * 认证操作结果（注册 / 登录 / 校验）。
 */
public class AuthResult {

    private final boolean success;
    private final String message;
    private final AuthSession session;
    private final UserAccount account;

    private AuthResult(boolean success, String message, AuthSession session, UserAccount account) {
        this.success = success;
        this.message = message;
        this.session = session;
        this.account = account;
    }

    public static AuthResult success(AuthSession session, UserAccount account) {
        return new AuthResult(true, "ok", session, account);
    }

    public static AuthResult success(AuthSession session) {
        return success(session, null);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null, null);
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

    public UserAccount getAccount() {
        return account;
    }
}

package cn.edu.whut.sept.zuul.infrastructure.auth;

/**
 * 验证码发送结果。
 */
public class VerificationSendResult {

    private final boolean success;
    private final String message;
    private final String devCode;

    private VerificationSendResult(boolean success, String message, String devCode) {
        this.success = success;
        this.message = message;
        this.devCode = devCode;
    }

    public static VerificationSendResult success(String message, String devCode) {
        return new VerificationSendResult(true, message, devCode);
    }

    public static VerificationSendResult failure(String message) {
        return new VerificationSendResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDevCode() {
        return devCode;
    }
}

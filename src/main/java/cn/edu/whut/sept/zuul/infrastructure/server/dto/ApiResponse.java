package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 统一 REST 响应格式。
 */
public class ApiResponse<T> {

    private final int code;
    private final T data;
    private final String message;

    public ApiResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, data, "ok");
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(1, null, message);
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}

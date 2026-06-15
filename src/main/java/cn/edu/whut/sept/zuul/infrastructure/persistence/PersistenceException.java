package cn.edu.whut.sept.zuul.infrastructure.persistence;

/**
 * 持久化操作失败时抛出的运行时异常。
 */
public class PersistenceException extends RuntimeException {

    /**
     * 使用原因消息创建异常。
     *
     * @param message 错误说明
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * 使用原因消息与底层异常创建异常。
     *
     * @param message 错误说明
     * @param cause 底层异常
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

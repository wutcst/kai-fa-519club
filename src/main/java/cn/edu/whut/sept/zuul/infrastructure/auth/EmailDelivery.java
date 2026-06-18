package cn.edu.whut.sept.zuul.infrastructure.auth;

/**
 * 邮件发送抽象（SMTP 或控制台日志）。
 */
public interface EmailDelivery {

    /**
     * 发送注册/验证邮件。
     *
     * @param email 收件邮箱
     * @param code 验证码
     * @return 是否通过真实 SMTP 发出
     */
    boolean sendVerificationCode(String email, String code);
}

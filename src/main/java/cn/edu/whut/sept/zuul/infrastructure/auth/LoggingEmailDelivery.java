package cn.edu.whut.sept.zuul.infrastructure.auth;

/**
 * 开发环境邮件投递：将验证码输出到控制台。
 */
public class LoggingEmailDelivery implements EmailDelivery {

    @Override
    public boolean sendVerificationCode(String email, String code) {
        System.out.println("[Zuul Mail] 验证码已生成 -> " + email + " : " + code);
        return false;
    }
}

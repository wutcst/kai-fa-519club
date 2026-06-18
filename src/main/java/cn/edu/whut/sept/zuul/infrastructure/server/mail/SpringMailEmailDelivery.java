package cn.edu.whut.sept.zuul.infrastructure.server.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cn.edu.whut.sept.zuul.infrastructure.auth.EmailDelivery;

/**
 * 通过 Spring Mail 发送验证码。
 */
@Component
public class SpringMailEmailDelivery implements EmailDelivery {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMailEmailDelivery.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SpringMailEmailDelivery(
            JavaMailSender mailSender,
            @Value("${zuul.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public boolean sendVerificationCode(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            return false;
        }
        if (!StringUtils.hasText(fromAddress)) {
            LOGGER.error("未配置 zuul.mail.from，无法发送验证码邮件");
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("【熄灯前归寝】邮箱验证码");
            message.setText("您的验证码是：" + code + "，10 分钟内有效。如非本人操作请忽略。");
            mailSender.send(message);
            LOGGER.info("验证码邮件已发送至 {}", email);
            return true;
        } catch (Exception exception) {
            LOGGER.error("验证码邮件发送失败: {}", exception.getMessage());
            return false;
        }
    }
}

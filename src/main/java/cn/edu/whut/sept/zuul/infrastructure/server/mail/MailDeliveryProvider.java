package cn.edu.whut.sept.zuul.infrastructure.server.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cn.edu.whut.sept.zuul.infrastructure.auth.EmailDelivery;
import cn.edu.whut.sept.zuul.infrastructure.auth.LoggingEmailDelivery;

/**
 * 按配置选择 SMTP 或控制台邮件投递。
 */
@Component
public class MailDeliveryProvider {

    private final EmailDelivery emailDelivery;
    private final boolean devExposeCode;

    public MailDeliveryProvider(
            @Autowired(required = false) SpringMailEmailDelivery springMailEmailDelivery,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${zuul.mail.dev-expose-code:false}") boolean devExposeCode) {
        if (StringUtils.hasText(mailHost) && springMailEmailDelivery != null) {
            this.emailDelivery = springMailEmailDelivery;
        } else {
            this.emailDelivery = new LoggingEmailDelivery();
        }
        this.devExposeCode = devExposeCode;
    }

    public EmailDelivery getEmailDelivery() {
        return emailDelivery;
    }

    public boolean isDevExposeCode() {
        return devExposeCode;
    }

    public boolean isSmtpEnabled() {
        return emailDelivery instanceof SpringMailEmailDelivery;
    }
}

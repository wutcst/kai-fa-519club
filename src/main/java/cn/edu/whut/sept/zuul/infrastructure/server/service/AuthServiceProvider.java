package cn.edu.whut.sept.zuul.infrastructure.server.service;

import org.springframework.stereotype.Component;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.auth.EmailVerificationRepository;
import cn.edu.whut.sept.zuul.infrastructure.auth.SessionRepository;
import cn.edu.whut.sept.zuul.infrastructure.auth.UserRepository;
import cn.edu.whut.sept.zuul.infrastructure.persistence.DatabaseProvider;
import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;
import cn.edu.whut.sept.zuul.infrastructure.server.mail.MailDeliveryProvider;

/**
 * Spring 环境下的 AuthService 工厂（接入 SMTP 或控制台邮件）。
 */
@Component
public class AuthServiceProvider {

    private final AuthService authService;

    public AuthServiceProvider(MailDeliveryProvider mailDeliveryProvider) {
        H2Database database = DatabaseProvider.getDefault();
        this.authService = new AuthService(
            new UserRepository(database),
            new SessionRepository(database),
            new EmailVerificationRepository(database),
            mailDeliveryProvider.getEmailDelivery(),
            mailDeliveryProvider.isDevExposeCode());
    }

    public AuthService getAuthService() {
        return authService;
    }
}

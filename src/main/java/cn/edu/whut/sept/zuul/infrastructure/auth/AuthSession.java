package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.time.LocalDateTime;

/**
 * 登录成功后的会话信息（后续可映射为 REST Token）。
 */
public class AuthSession {

    private final long userId;
    private final String username;
    private final String displayName;
    private final String token;
    private final LocalDateTime expiresAt;

    /**
     * 创建登录会话。
     */
    public AuthSession(long userId, String username, String displayName,
                       String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 会话是否已过期。
     *
     * @param now 当前时间
     * @return 过期返回 true
     */
    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && now != null && now.isAfter(expiresAt);
    }
}

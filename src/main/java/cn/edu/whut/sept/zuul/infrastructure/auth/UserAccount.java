package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.time.LocalDateTime;

/**
 * 注册用户记录。
 */
public class UserAccount {

    private final long id;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final LocalDateTime createdAt;

    public UserAccount(long id, String username, String passwordHash,
                       String displayName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

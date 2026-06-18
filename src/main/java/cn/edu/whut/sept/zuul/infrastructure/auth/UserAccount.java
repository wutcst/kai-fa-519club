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
    private final String email;
    private final String avatarUrl;
    private final LocalDateTime createdAt;

    public UserAccount(long id, String username, String passwordHash,
                       String displayName, String email, String avatarUrl,
                       LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
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

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

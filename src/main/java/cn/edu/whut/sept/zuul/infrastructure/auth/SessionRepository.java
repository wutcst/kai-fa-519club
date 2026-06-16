package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;
import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * 登录会话 Token 数据访问。
 */
public class SessionRepository {

    private static final int SESSION_HOURS = 24;

    private final H2Database database;

    public SessionRepository(H2Database database) {
        this.database = database;
    }

    public AuthSession createSession(UserAccount user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_HOURS);
        String sql = "INSERT INTO user_session (user_id, token, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(expiresAt));
            statement.executeUpdate();
            return new AuthSession(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                token,
                expiresAt
            );
        } catch (SQLException exception) {
            throw new PersistenceException("创建登录会话失败", exception);
        }
    }

    public Optional<AuthSession> findValidByToken(String token) {
        String sql = "SELECT s.token, s.expires_at, u.id, u.username, u.display_name "
            + "FROM user_session s JOIN app_user u ON s.user_id = u.id WHERE s.token = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                LocalDateTime expiresAt = resultSet.getTimestamp("expires_at").toLocalDateTime();
                if (LocalDateTime.now().isAfter(expiresAt)) {
                    return Optional.empty();
                }
                return Optional.of(new AuthSession(
                    resultSet.getLong("id"),
                    resultSet.getString("username"),
                    resultSet.getString("display_name"),
                    resultSet.getString("token"),
                    expiresAt
                ));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("校验登录会话失败", exception);
        }
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM user_session WHERE token = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("注销会话失败", exception);
        }
    }

    public int getSessionHours() {
        return SESSION_HOURS;
    }
}

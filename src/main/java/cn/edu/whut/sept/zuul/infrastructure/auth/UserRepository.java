package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;
import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * 用户表数据访问。
 */
public class UserRepository {

    private final H2Database database;

    public UserRepository(H2Database database) {
        this.database = database;
    }

    public long insert(String username, String passwordHash, String displayName, String email) {
        String sql = "INSERT INTO app_user (username, password_hash, display_name, email) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, displayName);
            statement.setString(4, email);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new PersistenceException("用户注册成功但未返回主键");
        } catch (SQLException exception) {
            throw new PersistenceException("注册用户失败", exception);
        }
    }

    public Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, display_name, email, avatar_url, created_at "
            + "FROM app_user WHERE username = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询用户失败", exception);
        }
    }

    public Optional<UserAccount> findById(long userId) {
        String sql = "SELECT id, username, password_hash, display_name, email, avatar_url, created_at "
            + "FROM app_user WHERE id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询用户失败", exception);
        }
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM app_user WHERE email = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeEmail(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询邮箱失败", exception);
        }
    }

    public void updateDisplayName(long userId, String displayName) {
        String sql = "UPDATE app_user SET display_name = ? WHERE id = ?";
        executeUpdate(sql, displayName, userId);
    }

    public void updatePassword(long userId, String passwordHash) {
        String sql = "UPDATE app_user SET password_hash = ? WHERE id = ?";
        executeUpdate(sql, passwordHash, userId);
    }

    public void updateEmail(long userId, String email) {
        String sql = "UPDATE app_user SET email = ? WHERE id = ?";
        executeUpdate(sql, normalizeEmail(email), userId);
    }

    public void updateAvatarUrl(long userId, String avatarUrl) {
        String sql = "UPDATE app_user SET avatar_url = ? WHERE id = ?";
        executeUpdate(sql, avatarUrl, userId);
    }

    public List<UserAccount> listAll() {
        String sql = "SELECT id, username, password_hash, display_name, email, avatar_url, created_at "
            + "FROM app_user ORDER BY id ASC";
        List<UserAccount> users = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new PersistenceException("查询用户列表失败", exception);
        }
    }

    private void executeUpdate(String sql, String value, long userId) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("更新用户失败", exception);
        }
    }

    private UserAccount mapRow(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new UserAccount(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("password_hash"),
            resultSet.getString("display_name"),
            resultSet.getString("email"),
            resultSet.getString("avatar_url"),
            createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

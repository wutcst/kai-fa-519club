package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

    public long insert(String username, String passwordHash, String displayName) {
        String sql = "INSERT INTO app_user (username, password_hash, display_name) VALUES (?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, displayName);
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
        String sql = "SELECT id, username, password_hash, display_name, created_at "
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
        String sql = "SELECT id, username, password_hash, display_name, created_at "
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

    private UserAccount mapRow(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new UserAccount(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("password_hash"),
            resultSet.getString("display_name"),
            createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}

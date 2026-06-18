package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;
import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * 邮箱验证码数据访问。
 */
public class EmailVerificationRepository {

    private static final int CODE_MINUTES = 10;
    private static final int SEND_COOLDOWN_SECONDS = 60;

    private final H2Database database;

    public EmailVerificationRepository(H2Database database) {
        this.database = database;
    }

    public String createCode(String email, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        enforceCooldown(normalizedEmail);
        invalidatePending(normalizedEmail, purpose);
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_MINUTES);
        String sql = "INSERT INTO email_verification_code (email, code, purpose, expires_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedEmail);
            statement.setString(2, code);
            statement.setString(3, purpose);
            statement.setTimestamp(4, Timestamp.valueOf(expiresAt));
            statement.executeUpdate();
            return code;
        } catch (SQLException exception) {
            throw new PersistenceException("保存邮箱验证码失败", exception);
        }
    }

    public boolean verifyAndConsume(String email, String code, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = code == null ? "" : code.trim();
        String sql = "SELECT id, expires_at, used FROM email_verification_code "
            + "WHERE email = ? AND code = ? AND purpose = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedEmail);
            statement.setString(2, normalizedCode);
            statement.setString(3, purpose);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                if (resultSet.getBoolean("used")) {
                    return false;
                }
                LocalDateTime expiresAt = resultSet.getTimestamp("expires_at").toLocalDateTime();
                if (LocalDateTime.now().isAfter(expiresAt)) {
                    return false;
                }
                long id = resultSet.getLong("id");
                markUsed(connection, id);
                return true;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("校验邮箱验证码失败", exception);
        }
    }

    public Optional<String> findLatestCodeForTest(String email) {
        String sql = "SELECT code FROM email_verification_code WHERE email = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeEmail(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("code"));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询验证码失败", exception);
        }
    }

    private void enforceCooldown(String email) {
        String sql = "SELECT created_at FROM email_verification_code WHERE email = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                    if (createdAt.plusSeconds(SEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now())) {
                        throw new IllegalArgumentException("发送过于频繁，请稍后再试");
                    }
                }
            }
        } catch (SQLException exception) {
            throw new PersistenceException("检查验证码发送频率失败", exception);
        }
    }

    private void invalidatePending(String email, String purpose) {
        String sql = "UPDATE email_verification_code SET used = TRUE WHERE email = ? AND purpose = ? AND used = FALSE";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, purpose);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("作废旧验证码失败", exception);
        }
    }

    private void markUsed(Connection connection, long id) throws SQLException {
        String sql = "UPDATE email_verification_code SET used = TRUE WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private String generateCode() {
        int value = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}

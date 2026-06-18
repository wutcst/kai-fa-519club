package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户单机关卡通关进度（按账号持久化）。
 */
public class UserLevelProgressRepository {

    private final H2Database database;

    public UserLevelProgressRepository(H2Database database) {
        this.database = database;
    }

    /**
     * 查询用户已通关的最高关卡号，未通关任何关返回 0。
     */
    public int findHighestClearedLevel(long userId) {
        String sql = "SELECT highest_cleared_level FROM user_level_progress WHERE user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("highest_cleared_level");
                }
            }
            return 0;
        } catch (SQLException exception) {
            throw new PersistenceException("查询关卡进度失败", exception);
        }
    }

    /**
     * 记录用户通关某一关（仅提升，不降级）。
     */
    public void recordLevelCleared(long userId, int clearedLevel) {
        if (clearedLevel < 1) {
            return;
        }
        int existing = findHighestClearedLevel(userId);
        if (clearedLevel <= existing) {
            return;
        }
        String upsert = "MERGE INTO user_level_progress (user_id, highest_cleared_level, updated_at) "
            + "KEY (user_id) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(upsert)) {
            statement.setLong(1, userId);
            statement.setInt(2, clearedLevel);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("写入关卡进度失败", exception);
        }
    }
}

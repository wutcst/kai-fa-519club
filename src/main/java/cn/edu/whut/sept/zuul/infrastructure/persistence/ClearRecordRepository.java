package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通关记录表数据访问（F8）。
 */
public class ClearRecordRepository {

    private final H2Database database;

    /**
     * 创建通关记录仓储。
     *
     * @param database H2 数据库访问对象
     */
    public ClearRecordRepository(H2Database database) {
        this.database = database;
    }

    /**
     * 写入一条第五关全通记录。
     *
     * @param playerName 玩家昵称
     * @return 新记录主键
     */
    public long insert(String playerName) {
        String sql = "INSERT INTO clear_record (player_name) VALUES (?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new PersistenceException("通关记录写入成功但未返回主键");
        } catch (SQLException exception) {
            throw new PersistenceException("写入通关记录失败", exception);
        }
    }

    /**
     * 查询全部通关记录，按时间倒序。
     *
     * @return 通关记录列表
     */
    public List<ClearRecord> findAllOrderByClearedAtDesc() {
        String sql = "SELECT id, player_name, cleared_at FROM clear_record ORDER BY cleared_at DESC, id DESC";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<ClearRecord> records = new ArrayList<>();
            while (resultSet.next()) {
                Timestamp clearedAt = resultSet.getTimestamp("cleared_at");
                LocalDateTime clearedDateTime = clearedAt != null ? clearedAt.toLocalDateTime() : null;
                records.add(new ClearRecord(
                    resultSet.getLong("id"),
                    resultSet.getString("player_name"),
                    clearedDateTime
                ));
            }
            return records;
        } catch (SQLException exception) {
            throw new PersistenceException("查询通关记录失败", exception);
        }
    }
}

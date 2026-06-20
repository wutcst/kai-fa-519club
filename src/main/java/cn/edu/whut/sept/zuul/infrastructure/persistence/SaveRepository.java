package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.edu.whut.sept.zuul.level.LevelProgressSnapshot;

/**
 * 游戏存档表数据访问（F8）。
 */
public class SaveRepository {

    private static final String SELECT_HEADER = "SELECT id, player_name, user_id, level_number, "
        + "remaining_seconds, current_room_id, dormitory_submit, west_exit_locked, "
        + "west_lock_broken, gym_unlocked, dorm_password_unlocked, magic_cookie_used, saved_at ";

    private final H2Database database;
    private final SaveItemRepository saveItemRepository;

  /**
     * 创建存档仓储。
     *
     * @param database H2 数据库访问对象
     */
    public SaveRepository(H2Database database) {
        this.database = database;
        this.saveItemRepository = new SaveItemRepository(database);
    }

    /**
     * 写入完整存档快照（含背包与进度标志）。
     *
     * @param snapshot 存档快照
     * @return 新存档主键
     */
    public long insertSnapshot(GameSaveSnapshot snapshot) {
        String sql = "INSERT INTO game_save (player_name, level_number, remaining_seconds, user_id, "
            + "current_room_id, dormitory_submit, west_exit_locked, west_lock_broken, "
            + "gym_unlocked, dorm_password_unlocked, magic_cookie_used) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, snapshot.getPlayerName());
            statement.setInt(2, snapshot.getLevelNumber());
            statement.setInt(3, snapshot.getRemainingSeconds());
            if (snapshot.getUserId() == null) {
                statement.setObject(4, null);
            } else {
                statement.setLong(4, snapshot.getUserId());
            }
            statement.setString(5, snapshot.getCurrentRoomId());
            LevelProgressSnapshot progress = snapshot.getProgress();
            statement.setBoolean(6, progress.isDormitorySubmitCompleted());
            statement.setBoolean(7, progress.isWestBuildingExitLocked());
            statement.setBoolean(8, progress.isWestBuildingLockBroken());
            statement.setBoolean(9, progress.isGymStorageUnlocked());
            statement.setBoolean(10, progress.isDormitoryPasswordUnlocked());
            statement.setBoolean(11, progress.isMagicCookieBonusUsed());
            statement.executeUpdate();
            long saveId;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException("存档写入成功但未返回主键");
                }
                saveId = keys.getLong(1);
            }
            saveItemRepository.replaceItemsForSave(saveId, snapshot.getInventory());
            return saveId;
        } catch (SQLException exception) {
            throw new PersistenceException("写入存档失败", exception);
        }
    }

    /**
     * 按主键读取完整存档快照。
     *
     * @param id 存档主键
     * @return 快照，不存在时为空
     */
    public Optional<GameSaveSnapshot> findSnapshotById(long id) {
        String sql = SELECT_HEADER + "FROM game_save WHERE id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                GameSaveRecord record = mapRecord(resultSet);
                List<cn.edu.whut.sept.zuul.Item> inventory = saveItemRepository.findBySaveId(id);
                return Optional.of(toSnapshot(record, inventory, resultSet));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询存档失败", exception);
        }
    }

    /**
     * 查询全部存档摘要。
     *
     * @return 存档列表
     */
    public List<GameSaveRecord> findAllOrderBySavedAtDesc() {
        return queryRecords(SELECT_HEADER + "FROM game_save ORDER BY saved_at DESC, id DESC");
    }

    /**
     * 按用户 ID 查询存档摘要。
     *
     * @param userId 登录用户 ID
     * @return 存档列表
     */
    public List<GameSaveRecord> findAllByUserIdOrderBySavedAtDesc(long userId) {
        return queryRecords(SELECT_HEADER + "FROM game_save WHERE user_id = ? ORDER BY saved_at DESC, id DESC",
            userId);
    }

    private List<GameSaveRecord> queryRecords(String sql, Object... params) {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<GameSaveRecord> records = new ArrayList<>();
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet));
                }
                return records;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询存档列表失败", exception);
        }
    }

    private GameSaveRecord mapRecord(ResultSet resultSet) throws SQLException {
        Timestamp savedAt = resultSet.getTimestamp("saved_at");
        Long userId = resultSet.getObject("user_id") == null ? null : resultSet.getLong("user_id");
        return new GameSaveRecord(
            resultSet.getLong("id"),
            resultSet.getString("player_name"),
            userId,
            resultSet.getInt("level_number"),
            resultSet.getInt("remaining_seconds"),
            resultSet.getString("current_room_id"),
            savedAt != null ? savedAt.toLocalDateTime() : null
        );
    }

    private GameSaveSnapshot toSnapshot(GameSaveRecord record,
                                        List<cn.edu.whut.sept.zuul.Item> inventory,
                                        ResultSet resultSet) throws SQLException {
        LevelProgressSnapshot progress = new LevelProgressSnapshot(
            resultSet.getBoolean("dormitory_submit"),
            resultSet.getBoolean("west_exit_locked"),
            resultSet.getBoolean("west_lock_broken"),
            resultSet.getBoolean("gym_unlocked"),
            resultSet.getBoolean("dorm_password_unlocked"),
            resultSet.getBoolean("magic_cookie_used")
        );
        String roomId = record.getCurrentRoomId();
        if (roomId == null || roomId.isEmpty()) {
            roomId = "gate";
        }
        return new GameSaveSnapshot(
            record.getPlayerName(),
            record.getUserId(),
            record.getLevelNumber(),
            record.getRemainingSeconds(),
            roomId,
            inventory,
            progress
        );
    }
}

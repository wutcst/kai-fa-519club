package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.InfrastructureServices;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelManager;

/**
 * 游戏存档与通关记录的应用服务（F8）：协调 DAO 与游戏内核状态重建。
 */
public class GamePersistenceService {

    private final SaveRepository saveRepository;
    private final ClearRecordRepository clearRecordRepository;

    public GamePersistenceService(SaveRepository saveRepository,
                                  ClearRecordRepository clearRecordRepository) {
        this.saveRepository = saveRepository;
        this.clearRecordRepository = clearRecordRepository;
    }

    public static GamePersistenceService createDefault() {
        return InfrastructureServices.getDefault().getPersistenceService();
    }

    public static GamePersistenceService create(H2Database database) {
        database.initializeSchema();
        return new GamePersistenceService(new SaveRepository(database), new ClearRecordRepository(database));
    }

    /**
     * 将当前游戏进度（含背包与位置）写入 H2。
     */
    public long saveProgress(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("游戏实例不能为空");
        }
        if (game.getLevelManager().isGameWon()) {
            throw new PersistenceException("游戏已全部通关，无需再存档");
        }
        if (!game.getLevelManager().isLevelInProgress()) {
            throw new PersistenceException("当前关卡未进行中，无法存档");
        }
        return saveRepository.insertSnapshot(captureSnapshot(game));
    }

    /**
     * 从 H2 读档：仅恢复关卡与剩余秒数，背包清空并从大门起点重来。
     */
    public boolean loadProgress(Game game, long saveId) {
        if (game == null) {
            throw new IllegalArgumentException("游戏实例不能为空");
        }
        Optional<GameSaveSnapshot> snapshotOptional = saveRepository.findSnapshotById(saveId);
        if (!snapshotOptional.isPresent()) {
            return false;
        }
        GameSaveSnapshot snapshot = snapshotOptional.get();
        validateSnapshot(snapshot);
        game.getPlayer().setName(snapshot.getPlayerName());
        game.getLevelManager().loadSavedProgress(
            snapshot.getLevelNumber(), snapshot.getRemainingSeconds());
        return true;
    }

    public List<GameSaveRecord> listSaves() {
        return saveRepository.findAllOrderBySavedAtDesc();
    }

    /**
     * 查询指定登录用户的存档列表。
     *
     * @param userId 用户 ID
     * @return 存档列表
     */
    public List<GameSaveRecord> listSavesByUserId(long userId) {
        return saveRepository.findAllByUserIdOrderBySavedAtDesc(userId);
    }

    public long recordClear(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("玩家昵称不能为空");
        }
        return clearRecordRepository.insert(playerName.trim());
    }

    public List<ClearRecord> listClearRecords() {
        return clearRecordRepository.findAllOrderByClearedAtDesc();
    }

    /**
     * 从游戏实例采集存档快照。
     */
    public static GameSaveSnapshot captureSnapshot(Game game) {
        LevelManager levelManager = game.getLevelManager();
        String roomId = game.getCurrentRoom() != null ? game.getCurrentRoom().getRoomId() : "gate";
        Long userId = game.isLoggedIn() ? game.getAuthSession().getUserId() : null;
        return new GameSaveSnapshot(
            game.getPlayer().getName(),
            userId,
            levelManager.getCurrentLevel(),
            game.getLevelTimer().getRemainingSeconds(),
            roomId,
            game.getPlayer().getInventory(),
            levelManager.captureProgressSnapshot()
        );
    }

    private void validateSnapshot(GameSaveSnapshot snapshot) {
        int level = snapshot.getLevelNumber();
        if (level < LevelConfig.MIN_LEVEL || level > LevelConfig.MAX_LEVEL) {
            throw new PersistenceException("存档关卡号无效: " + level);
        }
        if (snapshot.getRemainingSeconds() < 0) {
            throw new PersistenceException("存档剩余秒数无效");
        }
    }
}

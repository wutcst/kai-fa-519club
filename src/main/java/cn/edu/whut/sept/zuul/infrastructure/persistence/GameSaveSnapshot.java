package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.level.LevelProgressSnapshot;

/**
 * 完整游戏存档快照：关卡、计时、位置、背包与进度标志。
 */
public class GameSaveSnapshot {

    private final String playerName;
    private final Long userId;
    private final int levelNumber;
    private final int remainingSeconds;
    private final String currentRoomId;
    private final List<Item> inventory;
    private final LevelProgressSnapshot progress;

    public GameSaveSnapshot(String playerName, Long userId, int levelNumber, int remainingSeconds,
                            String currentRoomId, List<Item> inventory, LevelProgressSnapshot progress) {
        this.playerName = playerName;
        this.userId = userId;
        this.levelNumber = levelNumber;
        this.remainingSeconds = remainingSeconds;
        this.currentRoomId = currentRoomId;
        this.inventory = inventory == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(inventory));
        this.progress = progress == null ? LevelProgressSnapshot.empty() : progress;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Long getUserId() {
        return userId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public LevelProgressSnapshot getProgress() {
        return progress;
    }
}

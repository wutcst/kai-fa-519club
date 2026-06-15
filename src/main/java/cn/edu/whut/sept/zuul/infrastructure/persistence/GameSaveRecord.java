package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 游戏存档摘要记录（F8）。
 */
public class GameSaveRecord {

    private final long id;
    private final String playerName;
    private final Long userId;
    private final int levelNumber;
    private final int remainingSeconds;
    private final String currentRoomId;
    private final LocalDateTime savedAt;

    public GameSaveRecord(long id, String playerName, Long userId, int levelNumber,
                          int remainingSeconds, String currentRoomId, LocalDateTime savedAt) {
        this.id = id;
        this.playerName = playerName;
        this.userId = userId;
        this.levelNumber = levelNumber;
        this.remainingSeconds = remainingSeconds;
        this.currentRoomId = currentRoomId;
        this.savedAt = savedAt;
    }

    public long getId() {
        return id;
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

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

  /**
     * 生成供 GUI 列表展示的摘要文本。
     *
     * @return 存档摘要
     */
    public String getSummaryText() {
        return String.format("#%d %s | 第%d关 | 剩余%d秒 | 位置:%s | %s",
            id, playerName, levelNumber, remainingSeconds, currentRoomId, savedAt);
    }
}

package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.time.LocalDateTime;

/**
 * 第五关全通后的通关记录（F8）。
 */
public class ClearRecord {

    private final long id;
    private final String playerName;
    private final LocalDateTime clearedAt;

    /**
     * 创建通关记录实例。
     *
     * @param id 记录主键
     * @param playerName 玩家昵称
     * @param clearedAt 通关时间
     */
    public ClearRecord(long id, String playerName, LocalDateTime clearedAt) {
        this.id = id;
        this.playerName = playerName;
        this.clearedAt = clearedAt;
    }

    public long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public LocalDateTime getClearedAt() {
        return clearedAt;
    }
}

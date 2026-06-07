package cn.edu.whut.sept.zuul.level;

/**
 * 关卡运行状态，供 LevelManager 与后续 LevelTimer、sleep 命令联动。
 */
public enum LevelState {
    /** 当前关卡进行中 */
    IN_PROGRESS,
    /** 当前关卡已通关，等待进入下一关或已全部通关 */
    COMPLETED,
    /** 当前关卡失败（如超时），可重开 */
    FAILED,
    /** 五关全部通关 */
    GAME_WON
}

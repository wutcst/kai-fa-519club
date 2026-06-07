package cn.edu.whut.sept.zuul.level;

import cn.edu.whut.sept.zuul.Game;

/**
 * 各命令操作耗时常量，扣减由 LevelTimer 统一处理（E11）。
 */
public final class ActionTimeCost {

    /** 移动 go */
    public static final int GO = 15;
    /** 环顾 look */
    public static final int LOOK = 10;
    /** 拾取 take */
    public static final int TAKE = 20;
    /** 丢弃 drop */
    public static final int DROP = 10;
    /** 使用 use */
    public static final int USE = 25;
    /** NPC 对话 */
    public static final int NPC = 30;

    private ActionTimeCost() {
    }

    /**
     * 命令成功后扣减对应秒数。
     *
     * @param game 游戏实例
     * @param seconds 耗时秒数
     */
    public static void deduct(Game game, int seconds) {
        if (game != null && game.getLevelTimer() != null) {
            game.getLevelTimer().deduct(seconds);
        }
    }
}

package cn.edu.whut.sept.zuul.level;

import cn.edu.whut.sept.zuul.Game;

/**
 * 熄灯倒计时：以秒为单位扣减，归零时通知 LevelManager 本关失败。
 */
public class LevelTimer {

    private final Game game;
    private int remainingSeconds;

    /**
     * 创建计时器并绑定游戏实例。
     *
     * @param game 游戏上下文
     */
    public LevelTimer(Game game) {
        this.game = game;
        this.remainingSeconds = 0;
    }

    /**
     * 按秒数重置倒计时。
     *
     * @param seconds 本关初始剩余秒数
     */
    public void reset(int seconds) {
        this.remainingSeconds = Math.max(0, seconds);
    }

    /**
     * 按关卡配置重置倒计时。
     *
     * @param config 当前关卡配置
     */
    public void resetForLevel(LevelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("关卡配置不能为空");
        }
        reset(config.getTimeLimitSeconds());
    }

    /**
     * 计时器是否处于可扣减状态（当前关进行中）。
     *
     * @return 本关进行中时为 true
     */
    public boolean isActive() {
        return game.getLevelManager().isLevelInProgress();
    }

    /**
     * 扣减操作耗时；归零时触发本关失败。
     *
     * @param seconds 扣减秒数
     * @return 扣减已执行返回 true，未执行返回 false
     */
    public boolean deduct(int seconds) {
        if (seconds <= 0 || !isActive()) {
            return false;
        }
        remainingSeconds = Math.max(0, remainingSeconds - seconds);
        System.out.println(getDisplayText());
        if (remainingSeconds == 0) {
            System.out.println("熄灯了！本关失败。");
            game.getLevelManager().failCurrentLevel();
        }
        return true;
    }

    /**
     * 增加剩余秒数（如魔法饼干加时 E7）。
     *
     * @param seconds 增加秒数
     */
    public void addSeconds(int seconds) {
        if (seconds > 0 && game.getLevelManager().isLevelInProgress()) {
            remainingSeconds += seconds;
        }
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    /**
     * 统一熄灯倒计时文案。
     *
     * @return 距熄灯（23:00）还有 XXX 秒
     */
    public String getDisplayText() {
        return "距熄灯（23:00）还有 " + remainingSeconds + " 秒";
    }
}

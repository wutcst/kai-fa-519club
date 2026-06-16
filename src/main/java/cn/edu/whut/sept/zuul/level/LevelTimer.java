package cn.edu.whut.sept.zuul.level;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.edu.whut.sept.zuul.Game;

/**
 * 熄灯倒计时：实时每秒扣减，归零时通知 LevelManager 本关失败。
 * <p>
 * E11 中 go/look/take/drop 等常规操作的额外扣时已移除，改由实时秒表统一流逝；
 * 黑暗罚时、喂猫、食用等仍通过 {@link ActionTimeCost} 额外扣减。
 */
public class LevelTimer {

    private final Game game;
    private final Object lock = new Object();
    private int remainingSeconds;
    private boolean autoTickEnabled;
    private TimerAuthority timerAuthority = TimerAuthority.LOCAL_CLIENT;
    private ScheduledExecutorService tickerExecutor;
    private ScheduledFuture<?> tickerFuture;

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
     * 启用或关闭实时秒表（命令行 play 时启用，单元测试默认关闭）。
     *
     * @param enabled 是否每秒自动扣减 1 秒
     */
    public void setAutoTickEnabled(boolean enabled) {
        synchronized (lock) {
            this.autoTickEnabled = enabled;
            syncTickerWithState();
        }
    }

    public boolean isAutoTickEnabled() {
        synchronized (lock) {
            return autoTickEnabled;
        }
    }

    /**
     * 设置计时权威来源（F6 联机：客户端不本地 tick，由服务端推送秒数）。
     *
     * @param authority 计时权威模式
     */
    public void setTimerAuthority(TimerAuthority authority) {
        synchronized (lock) {
            this.timerAuthority = authority == null ? TimerAuthority.LOCAL_CLIENT : authority;
            syncTickerWithState();
        }
    }

    public TimerAuthority getTimerAuthority() {
        synchronized (lock) {
            return timerAuthority;
        }
    }

    /**
     * 应用服务端权威剩余秒数（联机客户端）；归零时同样触发失败。
     *
     * @param seconds 服务端剩余秒数
     */
    public void applyAuthoritativeRemainingSeconds(int seconds) {
        synchronized (lock) {
            if (!isActive()) {
                return;
            }
            remainingSeconds = Math.max(0, seconds);
            if (remainingSeconds == 0) {
                handleTimeUp();
            }
        }
    }

    /**
     * 停止后台计时线程（退出游戏时调用）。
     */
    public void shutdown() {
        synchronized (lock) {
            autoTickEnabled = false;
            stopTickerInternal();
        }
    }

    /**
     * 按秒数重置倒计时。
     *
     * @param seconds 本关初始剩余秒数
     */
    public void reset(int seconds) {
        synchronized (lock) {
            this.remainingSeconds = Math.max(0, seconds);
            syncTickerWithState();
        }
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
        return deduct(seconds, true);
    }

    /**
     * 扣减秒数，可控制是否打印倒计时文案。
     *
     * @param seconds 扣减秒数
     * @param verbose 是否输出当前剩余时间
     * @return 扣减已执行返回 true
     */
    public boolean deduct(int seconds, boolean verbose) {
        if (seconds <= 0) {
            return false;
        }
        synchronized (lock) {
            if (!isActive()) {
                return false;
            }
            remainingSeconds = Math.max(0, remainingSeconds - seconds);
            if (verbose) {
                System.out.println(getDisplayText());
            }
            if (remainingSeconds == 0) {
                handleTimeUp();
            }
            return true;
        }
    }

    /**
     * 增加剩余秒数（如魔法饼干加时 E7）。
     *
     * @param seconds 增加秒数
     */
    public void addSeconds(int seconds) {
        if (seconds <= 0) {
            return;
        }
        synchronized (lock) {
            if (game.getLevelManager().isLevelInProgress()) {
                remainingSeconds += seconds;
            }
        }
    }

    public int getRemainingSeconds() {
        synchronized (lock) {
            return remainingSeconds;
        }
    }

    /**
     * 统一熄灯倒计时文案。
     *
     * @return 距熄灯（23:00）还有 XXX 秒
     */
    public String getDisplayText() {
        synchronized (lock) {
            return "距熄灯（23:00）还有 " + remainingSeconds + " 秒";
        }
    }

    /**
     * 供单元测试直接触发一次实时扣秒（等价于后台 ticker 一跳）。
     */
    void tickOnceForTest() {
        deduct(1, false);
    }

    private void syncTickerWithState() {
        boolean shouldTick = autoTickEnabled
            && (timerAuthority == TimerAuthority.LOCAL_CLIENT || timerAuthority == TimerAuthority.SERVER_HOST)
            && isActive()
            && remainingSeconds > 0;
        if (shouldTick) {
            startTickerInternal();
        } else {
            stopTickerInternal();
        }
    }

    private void startTickerInternal() {
        if (tickerFuture != null && !tickerFuture.isCancelled()) {
            return;
        }
        if (tickerExecutor == null || tickerExecutor.isShutdown()) {
            tickerExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "level-timer-tick");
                thread.setDaemon(true);
                return thread;
            });
        }
        tickerFuture = tickerExecutor.scheduleAtFixedRate(
            this::tickOneSecond,
            1,
            1,
            TimeUnit.SECONDS
        );
    }

    private void stopTickerInternal() {
        if (tickerFuture != null) {
            tickerFuture.cancel(false);
            tickerFuture = null;
        }
        if (tickerExecutor != null) {
            tickerExecutor.shutdownNow();
            tickerExecutor = null;
        }
    }

    private void tickOneSecond() {
        synchronized (lock) {
            if (!autoTickEnabled || !isActive() || remainingSeconds <= 0) {
                return;
            }
            if (timerAuthority == TimerAuthority.SERVER_CLIENT) {
                return;
            }
            remainingSeconds--;
            if (remainingSeconds == 0) {
                handleTimeUp();
            }
        }
    }

    private void handleTimeUp() {
        System.out.println("熄灯了！本关失败。");
        game.getLevelManager().failCurrentLevel();
        stopTickerInternal();
    }
}

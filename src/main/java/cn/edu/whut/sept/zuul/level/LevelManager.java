package cn.edu.whut.sept.zuul.level;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Room;

/**
 * 五关进度管理：关卡加载、通关进下一关、失败重开本关、每关开始清空背包。
 */
public class LevelManager {

    /** 博学西楼房间 ID（E14 困锁） */
    public static final String WEST_BUILDING_ROOM_ID = "boxue_west";
    /** 西楼困锁时无法离开的提示 */
    public static final String WEST_BUILDING_TRAP_MESSAGE = "门被从内侧锁死了，你需要找到工具破门。";

    private final Game game;
    private int currentLevel;
    private int highestUnlockedLevel;
    private LevelState state;
    private LevelConfig currentConfig;
    private boolean dormitorySubmitCompleted;
    private boolean westBuildingExitLocked;
    private boolean westBuildingLockBroken;
    private boolean gymStorageUnlocked;
    private boolean dormitoryPasswordUnlocked;
    private boolean magicCookieBonusUsed;

    /**
     * 创建关卡管理器并绑定游戏实例。
     *
     * @param game 游戏上下文
     */
    public LevelManager(Game game) {
        this.game = game;
        this.currentLevel = LevelConfig.MIN_LEVEL;
        this.highestUnlockedLevel = LevelConfig.MIN_LEVEL;
        this.state = LevelState.IN_PROGRESS;
        this.currentConfig = LevelConfig.forLevel(LevelConfig.MIN_LEVEL);
    }

    /**
     * 加载并启动指定关卡：校验解锁、清空背包、重置位置与历史。
     *
     * @param levelNumber 目标关卡号（1—5）
     */
    public void startLevel(int levelNumber) {
        if (levelNumber < LevelConfig.MIN_LEVEL || levelNumber > LevelConfig.MAX_LEVEL) {
            throw new IllegalArgumentException("关卡号必须在 " + LevelConfig.MIN_LEVEL
                + "—" + LevelConfig.MAX_LEVEL + " 之间");
        }
        if (levelNumber > highestUnlockedLevel) {
            throw new IllegalStateException("第 " + levelNumber + " 关尚未解锁");
        }

        currentLevel = levelNumber;
        currentConfig = LevelConfig.forLevel(levelNumber);
        state = LevelState.IN_PROGRESS;
        dormitorySubmitCompleted = false;
        westBuildingExitLocked = false;
        westBuildingLockBroken = false;
        gymStorageUnlocked = false;
        dormitoryPasswordUnlocked = false;
        magicCookieBonusUsed = false;
        game.resetUnlockRoomState();

        game.getPlayer().dropAllItems();

        Room startRoom = game.getRoomById(currentConfig.getStartRoomId());
        if (startRoom == null) {
            throw new IllegalStateException("关卡起点房间不存在: " + currentConfig.getStartRoomId());
        }
        game.resetPlayerPosition(startRoom);
        game.getLevelTimer().resetForLevel(currentConfig);
        game.applyLevelRoomState(currentConfig);

        System.out.println();
        System.out.println("=== " + currentConfig.getTitle() + " ===");
        System.out.println(game.getLevelTimer().getDisplayText());
        System.out.println("背包已清空，你从起点出发。");
        System.out.println();
    }

    /**
     * 标记当前关卡通关：解锁下一关；若已是第五关则进入 GAME_WON。
     *
     * @return 若五关全部通关返回 true，否则返回 false
     */
    public boolean completeCurrentLevel() {
        if (state != LevelState.IN_PROGRESS) {
            throw new IllegalStateException("当前状态不允许通关: " + state);
        }

        System.out.println("恭喜通关第 " + currentLevel + " 关！");

        if (currentLevel >= LevelConfig.MAX_LEVEL) {
            state = LevelState.GAME_WON;
            System.out.println("五关全部通关，你赶在熄灯前回到了寝室！");
            return true;
        }

        state = LevelState.COMPLETED;
        highestUnlockedLevel = Math.max(highestUnlockedLevel, currentLevel + 1);
        int nextLevel = currentLevel + 1;
        System.out.println("即将进入第 " + nextLevel + " 关...");
        startLevel(nextLevel);
        return false;
    }

    /**
     * 标记当前关卡失败（如超时），等待重开。
     */
    public void failCurrentLevel() {
        if (state == LevelState.GAME_WON) {
            throw new IllegalStateException("游戏已全部通关，无法标记失败");
        }
        state = LevelState.FAILED;
        System.out.println("本关失败！请重开本关后再试。");
    }

    /**
     * 失败或主动重开：重新加载当前关卡。
     */
    public void restartCurrentLevel() {
        if (currentLevel < LevelConfig.MIN_LEVEL || currentLevel > LevelConfig.MAX_LEVEL) {
            throw new IllegalStateException("无效的当前关卡: " + currentLevel);
        }
        System.out.println("重新挑战第 " + currentLevel + " 关...");
        startLevel(currentLevel);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getHighestUnlockedLevel() {
        return highestUnlockedLevel;
    }

    public LevelState getState() {
        return state;
    }

    public LevelConfig getCurrentLevelConfig() {
        return currentConfig;
    }

    public boolean isGameWon() {
        return state == LevelState.GAME_WON;
    }

    public boolean isLevelInProgress() {
        return state == LevelState.IN_PROGRESS;
    }

    /**
     * 标记当前关寝室归寝单已提交（供 E19 submit 命令调用）。
     */
    public void markDormitorySubmitCompleted() {
        dormitorySubmitCompleted = true;
    }

    /**
     * 当前关是否已完成寝室归寝单提交。
     *
     * @return 已提交返回 true
     */
    public boolean isDormitorySubmitCompleted() {
        return dormitorySubmitCompleted;
    }

    /**
     * 进入博学西楼时触发困锁（E14，第三关起生效）。
     */
    public void onEnterWestBuilding() {
        if (currentLevel >= 3 && !westBuildingLockBroken) {
            westBuildingExitLocked = true;
            System.out.println("门从身后锁上了，你需要想办法破门而出。");
        }
    }

    /**
     * 西楼东侧出口是否被门锁阻挡。
     *
     * @return 被锁返回 true
     */
    public boolean isWestBuildingExitLocked() {
        return westBuildingExitLocked;
    }

    /**
     * use 锤子成功后解除西楼困锁（供 E1 use 调用）。
     */
    public void unlockWestBuildingExit() {
        westBuildingLockBroken = true;
        westBuildingExitLocked = false;
    }

    /**
     * 体育馆器材室是否已解锁（供 E4 unlock 调用）。
     *
     * @return 已解锁返回 true
     */
    public boolean isGymStorageUnlocked() {
        return gymStorageUnlocked;
    }

    /**
     * 标记体育馆器材室已解锁。
     */
    public void markGymStorageUnlocked() {
        gymStorageUnlocked = true;
    }

    /**
     * 第五关寝室智能锁是否已打开（供 E4 unlock 与 E6 sleep 调用）。
     *
     * @return 已解锁返回 true
     */
    public boolean isDormitoryPasswordUnlocked() {
        return dormitoryPasswordUnlocked;
    }

    /**
     * 标记第五关寝室智能锁已打开。
     */
    public void markDormitoryPasswordUnlocked() {
        dormitoryPasswordUnlocked = true;
    }

    /**
     * 当前关是否仍可使用魔法饼干加时（E7，每关限一次）。
     *
     * @return 未使用过加时返回 true
     */
    public boolean isMagicCookieBonusAvailable() {
        return !magicCookieBonusUsed;
    }

    /**
     * 标记本关魔法饼干加时已使用。
     */
    public void markMagicCookieBonusUsed() {
        magicCookieBonusUsed = true;
    }
}

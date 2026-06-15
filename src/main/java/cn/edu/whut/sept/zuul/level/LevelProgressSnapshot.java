package cn.edu.whut.sept.zuul.level;

/**
 * 关卡进度标志快照（读档时恢复 submit / 西楼 / 解锁等状态）。
 */
public class LevelProgressSnapshot {

    private final boolean dormitorySubmitCompleted;
    private final boolean westBuildingExitLocked;
    private final boolean westBuildingLockBroken;
    private final boolean gymStorageUnlocked;
    private final boolean dormitoryPasswordUnlocked;
    private final boolean magicCookieBonusUsed;

    public LevelProgressSnapshot(boolean dormitorySubmitCompleted,
                                 boolean westBuildingExitLocked,
                                 boolean westBuildingLockBroken,
                                 boolean gymStorageUnlocked,
                                 boolean dormitoryPasswordUnlocked,
                                 boolean magicCookieBonusUsed) {
        this.dormitorySubmitCompleted = dormitorySubmitCompleted;
        this.westBuildingExitLocked = westBuildingExitLocked;
        this.westBuildingLockBroken = westBuildingLockBroken;
        this.gymStorageUnlocked = gymStorageUnlocked;
        this.dormitoryPasswordUnlocked = dormitoryPasswordUnlocked;
        this.magicCookieBonusUsed = magicCookieBonusUsed;
    }

    public static LevelProgressSnapshot empty() {
        return new LevelProgressSnapshot(false, false, false, false, false, false);
    }

    public boolean isDormitorySubmitCompleted() {
        return dormitorySubmitCompleted;
    }

    public boolean isWestBuildingExitLocked() {
        return westBuildingExitLocked;
    }

    public boolean isWestBuildingLockBroken() {
        return westBuildingLockBroken;
    }

    public boolean isGymStorageUnlocked() {
        return gymStorageUnlocked;
    }

    public boolean isDormitoryPasswordUnlocked() {
        return dormitoryPasswordUnlocked;
    }

    public boolean isMagicCookieBonusUsed() {
        return magicCookieBonusUsed;
    }
}

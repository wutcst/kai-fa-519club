package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 单机选关列表项。
 */
public class SoloLevelOptionDto {

    private int levelNumber;
    private String title;
    private String missionHint;
    private boolean unlocked;
    private boolean cleared;

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMissionHint() {
        return missionHint;
    }

    public void setMissionHint(String missionHint) {
        this.missionHint = missionHint;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
}

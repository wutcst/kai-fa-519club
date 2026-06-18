package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 单机 GUI 动作按钮可见性。
 */
public class UiActionFlagsDto {

    private boolean showNpc;
    private boolean showFeed;
    private boolean showCombine;
    private boolean showUnlock;
    private boolean showSleep;
    private boolean showSubmit;

    public boolean isShowNpc() {
        return showNpc;
    }

    public void setShowNpc(boolean showNpc) {
        this.showNpc = showNpc;
    }

    public boolean isShowFeed() {
        return showFeed;
    }

    public void setShowFeed(boolean showFeed) {
        this.showFeed = showFeed;
    }

    public boolean isShowCombine() {
        return showCombine;
    }

    public void setShowCombine(boolean showCombine) {
        this.showCombine = showCombine;
    }

    public boolean isShowUnlock() {
        return showUnlock;
    }

    public void setShowUnlock(boolean showUnlock) {
        this.showUnlock = showUnlock;
    }

    public boolean isShowSleep() {
        return showSleep;
    }

    public void setShowSleep(boolean showSleep) {
        this.showSleep = showSleep;
    }

    public boolean isShowSubmit() {
        return showSubmit;
    }

    public void setShowSubmit(boolean showSubmit) {
        this.showSubmit = showSubmit;
    }
}

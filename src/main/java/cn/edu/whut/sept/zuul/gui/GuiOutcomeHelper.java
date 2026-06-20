package cn.edu.whut.sept.zuul.gui;

import java.util.List;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelState;

/**
 * 结局弹层判定（超时失败、关卡通关、全通），供 Vue API 视图状态使用。
 */
public final class GuiOutcomeHelper {

    /**
     * 结局类型。
     */
    public enum OutcomeType {
        NONE,
        LEVEL_FAILED,
        LEVEL_PASSED,
        GAME_WON
    }

    public static final String FAIL_SNIPPET = "熄灯了！本关失败";
    public static final String PASS_SNIPPET = "恭喜通关第";
    public static final String GAME_WON_SNIPPET = "五关全部通关";

    private GuiOutcomeHelper() {
    }

    public static OutcomeType detectFromOutput(List<String> lines) {
        if (lines == null) {
            return OutcomeType.NONE;
        }
        boolean hasPass = false;
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            if (line.contains(GAME_WON_SNIPPET)) {
                return OutcomeType.GAME_WON;
            }
            if (line.contains(FAIL_SNIPPET)) {
                return OutcomeType.LEVEL_FAILED;
            }
            if (line.contains(PASS_SNIPPET)) {
                hasPass = true;
            }
        }
        if (hasPass) {
            return OutcomeType.LEVEL_PASSED;
        }
        return OutcomeType.NONE;
    }

    public static OutcomeType detectFromStateTransition(LevelState previous, LevelState current) {
        if (previous == LevelState.IN_PROGRESS && current == LevelState.FAILED) {
            return OutcomeType.LEVEL_FAILED;
        }
        return OutcomeType.NONE;
    }

    public static String buildTitle(OutcomeType type) {
        switch (type) {
            case LEVEL_FAILED:
                return "熄灯了";
            case LEVEL_PASSED:
                return "关卡通关";
            case GAME_WON:
                return "全部通关";
            default:
                return "";
        }
    }

    public static String buildMessage(OutcomeType type, Game game, List<String> outputLines) {
        switch (type) {
            case LEVEL_FAILED:
                return "本关失败，你没能赶在 23:00 前完成归寝。\n"
                    + "点击「重试本关」清空背包并从本关起点重来。";
            case LEVEL_PASSED:
                return joinRelevantLines(outputLines)
                    + "\n\n更多区域已开放，继续探索吧。";
            case GAME_WON:
                return "五关全部通关，你赶在熄灯前回到了寝室！\n"
                    + "辛苦了，519 的夜归传说又多一位。";
            default:
                return "";
        }
    }

    public static String buildActionLabel(OutcomeType type) {
        switch (type) {
            case LEVEL_FAILED:
                return "重试本关";
            case LEVEL_PASSED:
            case GAME_WON:
                return "继续";
            default:
                return "关闭";
        }
    }

    private static String joinRelevantLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            if (line.contains(PASS_SNIPPET) || line.contains("即将进入第") || line.contains(GAME_WON_SNIPPET)) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line.trim());
            }
        }
        return builder.toString();
    }
}

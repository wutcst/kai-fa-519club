package cn.edu.whut.sept.zuul.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.multiplayer.OutputCapture;

/**
 * GUI 与游戏内核的命令桥接（F7 阶段 1）。
 */
public final class GameGuiController {

    /**
     * 命令执行结果。
     */
    public static final class CommandResult {
        private final boolean quitRequested;
        private final List<String> outputLines;
        private final boolean lockedExitAttempt;
        private final boolean darkPenaltyTriggered;
        private final boolean teleported;
        private final String gatedDenialMessage;

        public CommandResult(
            boolean quitRequested,
            List<String> outputLines,
            boolean lockedExitAttempt,
            boolean darkPenaltyTriggered,
            boolean teleported,
            String gatedDenialMessage) {
            this.quitRequested = quitRequested;
            this.outputLines = outputLines == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(outputLines));
            this.lockedExitAttempt = lockedExitAttempt;
            this.darkPenaltyTriggered = darkPenaltyTriggered;
            this.teleported = teleported;
            this.gatedDenialMessage = gatedDenialMessage;
        }

        public boolean isQuitRequested() {
            return quitRequested;
        }

        public List<String> getOutputLines() {
            return outputLines;
        }

        public boolean isLockedExitAttempt() {
            return lockedExitAttempt;
        }

        public boolean isDarkPenaltyTriggered() {
            return darkPenaltyTriggered;
        }

        public boolean isTeleported() {
            return teleported;
        }

        public String getGatedDenialMessage() {
            return gatedDenialMessage;
        }

        public String joinedOutput() {
            return String.join("\n", outputLines);
        }
    }

    public GameGuiController() {
    }

    /**
     * 初始化 GUI 会话：启用实时倒计时。
     *
     * @param game 游戏实例
     */
    public void prepareGuiSession(Game game) {
        if (game != null && game.getLevelTimer() != null) {
            game.getLevelTimer().setAutoTickEnabled(true);
        }
    }

    /**
     * 结束 GUI 会话：停止计时线程。
     *
     * @param game 游戏实例
     */
    public void shutdownGuiSession(Game game) {
        if (game != null && game.getLevelTimer() != null) {
            game.getLevelTimer().shutdown();
        }
    }

    /**
     * 执行游戏命令并捕获控制台输出。
     *
     * @param game 游戏实例
     * @param commandWord 命令词
     * @param secondWord 参数
     * @return 执行结果
     */
    public CommandResult execute(Game game, String commandWord, String secondWord) {
        if (game == null || commandWord == null || commandWord.trim().isEmpty()) {
            return new CommandResult(
                false,
                Collections.singletonList("无效命令。"),
                false,
                false,
                false,
                null
            );
        }
        try (OutputCapture capture = OutputCapture.start()) {
            boolean quit = game.getCommandManager().executeCommand(
                commandWord.trim(),
                secondWord,
                game
            );
            List<String> lines = capture.getLines();
            boolean locked = containsLockedMessage(lines);
            boolean darkPenalty = GuiPhase3Helper.outputIndicatesDarkPenalty(lines);
            boolean teleported = GuiPhase3Helper.outputIndicatesTeleport(lines);
            String gatedDenial = GuiPhase3Helper.findGatedDenialMessage(lines);
            return new CommandResult(quit, lines, locked, darkPenalty, teleported, gatedDenial);
        }
    }

    /**
     * 构建当前房间公告文本（含本关任务等）。
     *
     * @param game 游戏实例
     * @return 公告文本
     */
    public String buildBulletinText(Game game) {
        if (game == null || game.getCurrentRoom() == null) {
            return "";
        }
        String bulletin = game.getCurrentRoom().getBulletin();
        if (bulletin == null || bulletin.trim().isEmpty()) {
            return game.getCurrentRoom().getShortDescription();
        }
        return bulletin;
    }

    /**
     * 当前关卡显示文案。
     *
     * @param game 游戏实例
     * @return 关卡标题
     */
    public String buildLevelTitle(Game game) {
        if (game == null || game.getLevelManager() == null) {
            return "";
        }
        return game.getLevelManager().getCurrentLevelConfig().getTitle();
    }

    private boolean containsLockedMessage(List<String> lines) {
        for (String line : lines) {
            if (line != null && line.contains(LevelConfig.LOCKED_EXIT_MESSAGE)) {
                return true;
            }
        }
        return false;
    }
}

package cn.edu.whut.sept.zuul.infrastructure.server.service;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.OutcomeOverlayDto;
import cn.edu.whut.sept.zuul.level.LevelState;

/**
 * 单机 Vue 客户端会话（内存中持有 Game 实例）。
 */
public class SinglePlayerSession {

    private final String sessionId;
    private final Game game;
    private final GameGuiController controller;
    private final Long userId;
    private LevelState trackedLevelState;
    private OutcomeOverlayDto pendingOutcome;
    private String pendingLockedOverlay;

    public SinglePlayerSession(String sessionId, Game game, Long userId) {
        this.sessionId = sessionId;
        this.game = game;
        this.userId = userId;
        this.controller = new GameGuiController();
        this.controller.prepareGuiSession(game);
        this.trackedLevelState = game.getLevelManager().getState();
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public Game getGame() {
        return game;
    }

    public GameGuiController getController() {
        return controller;
    }

    public LevelState getTrackedLevelState() {
        return trackedLevelState;
    }

    public void setTrackedLevelState(LevelState trackedLevelState) {
        this.trackedLevelState = trackedLevelState;
    }

    public OutcomeOverlayDto getPendingOutcome() {
        return pendingOutcome;
    }

    public void setPendingOutcome(OutcomeOverlayDto pendingOutcome) {
        this.pendingOutcome = pendingOutcome;
    }

    public String getPendingLockedOverlay() {
        return pendingLockedOverlay;
    }

    public void setPendingLockedOverlay(String pendingLockedOverlay) {
        this.pendingLockedOverlay = pendingLockedOverlay;
    }

    public void shutdown() {
        controller.shutdownGuiSession(game);
    }
}

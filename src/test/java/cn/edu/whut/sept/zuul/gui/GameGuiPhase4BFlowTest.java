package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 4-B：读档后游戏状态与 GUI 同步前提。
 */
public class GameGuiPhase4BFlowTest {

    private Game game;

    @BeforeEach
    public void setUp() {
        game = new Game();
    }

    @Test
    public void loadProgressRestoresInProgressStateAndClearsInventory() throws Exception {
        game.getPlayer().takeItem(new Item("magic cookie", 100));
        game.getPlayer().setName("试玩员");
        long saveId = game.getPersistenceService().saveProgress(game);

        game.getLevelManager().failCurrentLevel();
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());

        boolean loaded = game.getPersistenceService().loadProgress(game, saveId);
        assertTrue(loaded);
        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        assertTrue(game.getPlayer().getInventory().isEmpty());
        assertEquals("试玩员", game.getPlayer().getName());
        assertTrue(game.getLevelTimer().getRemainingSeconds() > 0);
    }

    @Test
    public void prepareGuiSessionKeepsAutoTickAfterLoad() throws Exception {
        GameGuiController controller = new GameGuiController();
        controller.prepareGuiSession(game);
        long saveId = game.getPersistenceService().saveProgress(game);

        game.getPersistenceService().loadProgress(game, saveId);
        controller.prepareGuiSession(game);

        assertTrue(game.getLevelTimer().isAutoTickEnabled());
        controller.shutdownGuiSession(game);
    }
}

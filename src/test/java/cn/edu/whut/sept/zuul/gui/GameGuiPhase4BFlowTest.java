package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.infrastructure.persistence.InMemoryGameTestSupport;
import cn.edu.whut.sept.zuul.level.LevelConfig;
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
        game = InMemoryGameTestSupport.createGameWithInMemoryPersistence();
    }

    @Test
    public void loadProgressRestoresInProgressStateAndClearsInventory() {
        game.getPlayer().takeItem(new Item("magic cookie", 100));
        game.getPlayer().setName("试玩员");
        game.getLevelTimer().deduct(55);
        int expectedSeconds = LevelConfig.forLevel(1).getTimeLimitSeconds() - 55;
        long saveId = game.getPersistenceService().saveProgress(game);

        game.getLevelManager().failCurrentLevel();
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());

        Game restarted = InMemoryGameTestSupport.createRestartedGame(game);
        assertTrue(restarted.getPersistenceService().loadProgress(restarted, saveId));
        assertEquals(LevelState.IN_PROGRESS, restarted.getLevelManager().getState());
        assertTrue(restarted.getPlayer().getInventory().isEmpty());
        assertEquals("试玩员", restarted.getPlayer().getName());
        assertEquals(1, restarted.getLevelManager().getCurrentLevel());
        assertEquals(expectedSeconds, restarted.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void prepareGuiSessionKeepsAutoTickAfterLoad() {
        GameGuiController controller = new GameGuiController();
        controller.prepareGuiSession(game);
        long saveId = game.getPersistenceService().saveProgress(game);

        Game restarted = InMemoryGameTestSupport.createRestartedGame(game);
        restarted.getPersistenceService().loadProgress(restarted, saveId);
        controller.prepareGuiSession(restarted);

        assertTrue(restarted.getLevelTimer().isAutoTickEnabled());
        controller.shutdownGuiSession(restarted);
    }
}

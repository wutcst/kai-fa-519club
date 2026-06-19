package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.infrastructure.persistence.InMemoryGameTestSupport;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T3：GUI 控制器 + F8 存档联动——存档后模拟重启，断言关卡与计时一致。
 */
public class GameGuiPersistenceBridgeTest {

    @Test
    public void saveLoadAfterGuiCommandsRestoresLevelAndTimer() {
        Game game = InMemoryGameTestSupport.createGameWithInMemoryPersistence();
        GameGuiController controller = new GameGuiController();
        controller.prepareGuiSession(game);
        game.getPlayer().setName("Vue桥接");

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        game.getLevelManager().completeCurrentLevel();
        int expectedLevel = 2;
        int expectedSeconds = LevelConfig.forLevel(2).getTimeLimitSeconds() - 30;
        game.getLevelTimer().deduct(30);

        long saveId = game.getPersistenceService().saveProgress(game);
        game.getLevelManager().failCurrentLevel();
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());

        Game restarted = InMemoryGameTestSupport.createRestartedGame(game);
        GameGuiController restartedController = new GameGuiController();
        restartedController.prepareGuiSession(restarted);
        assertTrue(restarted.getPersistenceService().loadProgress(restarted, saveId));

        assertEquals(expectedLevel, restarted.getLevelManager().getCurrentLevel());
        assertEquals(expectedSeconds, restarted.getLevelTimer().getRemainingSeconds());
        assertEquals(LevelState.IN_PROGRESS, restarted.getLevelManager().getState());
        assertEquals("Vue桥接", restarted.getPlayer().getName());
        assertTrue(restarted.getPlayer().getInventory().isEmpty());
        assertEquals("gate", restarted.getCurrentRoom().getRoomId());
        assertTrue(restarted.getLevelTimer().isAutoTickEnabled());

        restartedController.shutdownGuiSession(restarted);
        controller.shutdownGuiSession(game);
    }

    @Test
    public void saveAfterInventoryChangeDoesNotRestoreItemsOnLoad() {
        Game game = InMemoryGameTestSupport.createGameWithInMemoryPersistence();
        game.getPlayer().takeItem(new Item("magic cookie", 100));
        long saveId = game.getPersistenceService().saveProgress(game);

        Game restarted = InMemoryGameTestSupport.createRestartedGame(game);
        assertTrue(restarted.getPersistenceService().loadProgress(restarted, saveId));
        assertTrue(restarted.getPlayer().getInventory().isEmpty());
    }
}

package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelConfig;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GameGuiController 单元测试。
 */
public class GameGuiControllerTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
    }

    @Test
    public void prepareGuiSessionEnablesAutoTick() {
        controller.prepareGuiSession(game);
        assertTrue(game.getLevelTimer().isAutoTickEnabled());
        controller.shutdownGuiSession(game);
    }

    @Test
    public void executeTakeMoneyInNorthBuilding() {
        controller.prepareGuiSession(game);
        game.getCommandManager().executeCommand("go", "north", game);
        game.getCommandManager().executeCommand("go", "north", game);

        int before = game.getLevelTimer().getRemainingSeconds();
        GameGuiController.CommandResult result = controller.execute(
            game,
            "take",
            UseCommand.MONEY_ITEM
        );

        assertFalse(result.isQuitRequested());
        assertTrue(game.getPlayer().findItemInInventory(UseCommand.MONEY_ITEM) != null);
        assertTrue(game.getLevelTimer().getRemainingSeconds() <= before - ActionTimeCost.TAKE);
        controller.shutdownGuiSession(game);
    }

    @Test
    public void lockedExitAttemptDetectedWithoutEnteringRoom() {
        controller.prepareGuiSession(game);
        Room gymnasium = game.getRoomById("gymnasium");
        assertFalse(game.isRoomAccessible(gymnasium));

        GameGuiController.CommandResult result = controller.execute(game, "go", "west");
        assertTrue(result.isLockedExitAttempt());
        assertTrue(result.joinedOutput().contains(LevelConfig.LOCKED_EXIT_MESSAGE));
        assertTrue(game.getCurrentRoom().getRoomId().equals("gate"));
        controller.shutdownGuiSession(game);
    }

    @Test
    public void buildBulletinContainsRoomText() {
        String bulletin = controller.buildBulletinText(game);
        assertTrue(bulletin.contains("回寝") || bulletin.contains("校门"));
    }
}

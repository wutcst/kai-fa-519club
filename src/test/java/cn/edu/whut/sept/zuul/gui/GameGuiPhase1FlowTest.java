package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 1 第一关主线流程（GUI 控制器驱动，无 Swing）。
 */
public class GameGuiPhase1FlowTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
        controller.prepareGuiSession(game);
    }

    @Test
    public void levelOneMainQuestViaCommands() {
        assertEquals(1, game.getLevelManager().getCurrentLevel());
        assertEquals("gate", game.getCurrentRoom().getRoomId());

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        assertEquals("boxue_north", game.getCurrentRoom().getRoomId());

        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(UseCommand.MONEY_ITEM));

        controller.execute(game, "go", "west");
        assertEquals("supermarket", game.getCurrentRoom().getRoomId());

        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));

        controller.execute(game, "go", "north");
        assertEquals("dormitory", game.getCurrentRoom().getRoomId());

        controller.execute(game, "sleep", null);
        assertEquals(2, game.getLevelManager().getCurrentLevel());

        controller.shutdownGuiSession(game);
    }

    @Test
    public void lockedExitFromGateDoesNotChangeRoom() {
        GameGuiController.CommandResult result = controller.execute(game, "go", "west");
        assertTrue(result.isLockedExitAttempt());
        assertEquals("gate", game.getCurrentRoom().getRoomId());
        controller.shutdownGuiSession(game);
    }

    @Test
    public void autoTickEnabledDuringSession() {
        assertTrue(game.getLevelTimer().isAutoTickEnabled());
        controller.shutdownGuiSession(game);
    }
}

package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 3 第三关核心流程（GUI 控制器驱动）。
 */
public class GameGuiPhase3FlowTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
        controller.prepareGuiSession(game);
        while (game.getLevelManager().getCurrentLevel() < 3) {
            game.getLevelManager().completeCurrentLevel();
        }
    }

    @Test
    public void levelThreeWestBuildingHammerFlow() {
        GameGuiController.CommandResult darkTry = controller.execute(game, "go", "north");
        assertTrue(darkTry.isDarkPenaltyTriggered());
        assertEquals("gate", game.getCurrentRoom().getRoomId());

        controller.execute(game, "go", "west");
        controller.execute(game, "take", DarkRoom.FLASHLIGHT_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "west");
        assertEquals(UseCommand.WEST_BUILDING_ROOM_ID, game.getCurrentRoom().getRoomId());
        assertTrue(GuiPhase3Helper.isWestBuildingTrapped(game));

        controller.execute(game, "take", CombineCommand.STICK_ITEM);
        controller.execute(game, "take", CombineCommand.STONE_ITEM);
        controller.execute(game, "take", CombineCommand.ROPE_ITEM);
        controller.execute(game, "combine", null);
        assertNotNull(game.getPlayer().findItemInInventory(UseCommand.HAMMER_ITEM));

        controller.execute(game, "use", UseCommand.HAMMER_ITEM);
        assertFalse(game.getLevelManager().isWestBuildingExitLocked());

        controller.shutdownGuiSession(game);
    }
}

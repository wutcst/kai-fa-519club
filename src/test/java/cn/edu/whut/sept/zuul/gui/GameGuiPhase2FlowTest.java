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
 * 阶段 2 第二关主线流程（GUI 控制器驱动）。
 */
public class GameGuiPhase2FlowTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
        controller.prepareGuiSession(game);
        game.getLevelManager().completeCurrentLevel();
    }

    @Test
    public void levelTwoMainQuestViaCommands() {
        assertEquals(2, game.getLevelManager().getCurrentLevel());

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        NpcDialogHelper.performTalk(game);
        assertNotNull(game.getPlayer().findItemInInventory(UseCommand.DORM_FORM_ITEM));

        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        assertEquals("supermarket", game.getCurrentRoom().getRoomId());

        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);
        assertTrue(game.getLevelManager().isDormitorySubmitCompleted());

        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));

        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);
        assertEquals(3, game.getLevelManager().getCurrentLevel());

        controller.shutdownGuiSession(game);
    }
}

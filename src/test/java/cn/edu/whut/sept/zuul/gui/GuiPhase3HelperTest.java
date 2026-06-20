package cn.edu.whut.sept.zuul.gui;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.npc.NpcService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GuiPhase3Helper 单元测试。
 */
public class GuiPhase3HelperTest {

    private Game game;

    @BeforeEach
    public void setUp() {
        game = new Game();
    }

    @Test
    public void detectsDarkPenaltyOutput() {
        assertTrue(GuiPhase3Helper.outputIndicatesDarkPenalty(
            Arrays.asList("灯坏了，黑暗中摸索一分钟一无所获")
        ));
    }

    @Test
    public void detectsGatedDenialOutput() {
        String message = GuiPhase3Helper.findGatedDenialMessage(
            Arrays.asList(GatedRoom.LIBRARY_CARD_DENIED_MESSAGE)
        );
        assertEquals(GatedRoom.LIBRARY_CARD_DENIED_MESSAGE, message);
    }

    @Test
    public void combineMaterialsDetection() {
        Player player = game.getPlayer();
        player.takeItem(new cn.edu.whut.sept.zuul.Item(CombineCommand.STICK_ITEM, 1));
        player.takeItem(new cn.edu.whut.sept.zuul.Item(CombineCommand.STONE_ITEM, 1));
        player.takeItem(new cn.edu.whut.sept.zuul.Item(CombineCommand.ROPE_ITEM, 1));
        assertTrue(GuiPhase3Helper.shouldOfferCombinePrompt(game));
    }

    @Test
    public void feedButtonOnlyOnNorthBuildingLevelFour() {
        while (game.getLevelManager().getCurrentLevel() < 4) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.resetPlayerPosition(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));
        assertTrue(GuiPhase3Helper.shouldShowFeedButton(game));

        game.resetPlayerPosition(game.getRoomById("gate"));
        assertFalse(GuiPhase3Helper.shouldShowFeedButton(game));
    }

    @Test
    public void unlockButtonOnLevelFiveDormitory() {
        while (game.getLevelManager().getCurrentLevel() < 5) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.resetPlayerPosition(game.getRoomById("dormitory"));
        assertTrue(GuiPhase3Helper.shouldShowUnlockButton(game));
        game.getLevelManager().markDormitoryPasswordUnlocked();
        assertFalse(GuiPhase3Helper.shouldShowUnlockButton(game));
    }

    @Test
    public void controllerDetectsDarkPenaltyOnGo() {
        while (game.getLevelManager().getCurrentLevel() < 3) {
            game.getLevelManager().completeCurrentLevel();
        }
        GameGuiController controller = new GameGuiController();
        GameGuiController.CommandResult result = controller.execute(game, "go", "north");
        assertTrue(result.isDarkPenaltyTriggered());
        assertTrue(result.joinedOutput().contains(DarkRoom.PENALTY_MESSAGE));
        assertEquals("gate", game.getCurrentRoom().getRoomId());
    }
}

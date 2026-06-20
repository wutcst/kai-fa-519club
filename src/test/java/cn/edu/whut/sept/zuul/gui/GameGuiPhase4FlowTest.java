package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.npc.NpcService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 4 核心流程（吃魔法饼干、sleep 通关、超时检测）。
 */
public class GameGuiPhase4FlowTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
        controller.prepareGuiSession(game);
    }

    @Test
    public void levelFourEatMagicCookieByName() {
        while (game.getLevelManager().getCurrentLevel() < 4) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.getPlayer().takeItem(new Item(FoodItems.MAGIC_COOKIE, 100));
        int beforeTime = game.getLevelTimer().getRemainingSeconds();

        GameGuiController.CommandResult result = controller.execute(game, "eat", FoodItems.MAGIC_COOKIE);

        assertTrue(result.getOutputLines().stream()
            .anyMatch(line -> line.contains("你吃掉了 magic cookie")));
        assertEquals(
            beforeTime - ActionTimeCost.EAT + ActionTimeCost.COOKIE_BONUS,
            game.getLevelTimer().getRemainingSeconds());
        assertFalse(game.getLevelManager().isMagicCookieBonusAvailable());
        assertEquals(GuiOutcomeHelper.OutcomeType.NONE, GuiOutcomeHelper.detectFromOutput(result.getOutputLines()));
        controller.shutdownGuiSession(game);
    }

    @Test
    public void sleepSuccessTriggersLevelPassedOutcome() {
        fulfillLevelOneRequirements();
        GameGuiController.CommandResult result = controller.execute(game, "sleep", null);

        assertEquals(GuiOutcomeHelper.OutcomeType.LEVEL_PASSED,
            GuiOutcomeHelper.detectFromOutput(result.getOutputLines()));
        assertEquals(2, game.getLevelManager().getCurrentLevel());
        controller.shutdownGuiSession(game);
    }

    @Test
    public void timerTimeoutStateTransitionDetectsFailure() {
        game.getLevelManager().failCurrentLevel();
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());
        assertEquals(
            GuiOutcomeHelper.OutcomeType.LEVEL_FAILED,
            GuiOutcomeHelper.detectFromStateTransition(LevelState.IN_PROGRESS, LevelState.FAILED));
        controller.shutdownGuiSession(game);
    }

    @Test
    public void edibleItemDetectionForInventoryEatMenu() {
        assertTrue(FoodItems.isEdible(FoodItems.MAGIC_COOKIE));
        assertFalse(FoodItems.isEdible(GatedRoom.CAMPUS_CARD_ITEM));
        controller.shutdownGuiSession(game);
    }

    @Test
    public void feedCatAvailableOnLevelFourNorthBuilding() {
        while (game.getLevelManager().getCurrentLevel() < 4) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.resetPlayerPosition(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));
        assertTrue(GuiPhase3Helper.shouldShowFeedButton(game));
        assertTrue(game.getCommandManager().isFeedCommandAvailable());
        controller.shutdownGuiSession(game);
    }

    private void fulfillLevelOneRequirements() {
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 50));
        game.resetPlayerPosition(game.getRoomById("dormitory"));
    }
}

package cn.edu.whut.sept.zuul.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.gui.GuiPhase3Helper;
import cn.edu.whut.sept.zuul.gui.NpcDialogHelper;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T4 集成测试：关卡、计时、命令多模块串联（JUnit 5，无人工输入，CI 可运行）。
 */
public class GameIntegrationTest {

    private Game game;
    private GameGuiController controller;

    @BeforeEach
    public void setUp() {
        game = new Game();
        controller = new GameGuiController();
    }

    @AfterEach
    public void tearDown() {
        if (controller != null && game != null) {
            controller.shutdownGuiSession(game);
        }
    }

    /**
     * IT-01：第 1 关从大门换卡到 sleep 过关。
     */
    @Test
    public void integrationLevelOnePassFromGateToSleep() {
        assertEquals(1, game.getLevelManager().getCurrentLevel());
        assertEquals(240, game.getLevelTimer().getRemainingSeconds());

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));

        controller.execute(game, "go", "north");
        GameGuiController.CommandResult sleepResult = controller.execute(game, "sleep", null);

        assertEquals(2, game.getLevelManager().getCurrentLevel());
        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        assertTrue(sleepResult.joinedOutput().contains("恭喜通关"));
    }

    /**
     * IT-02：连续 look 命令耗尽计时，触发超时失败。
     */
    @Test
    public void integrationTimeoutFailureViaContinuousCommands() {
        int initialSeconds = game.getLevelTimer().getRemainingSeconds();
        assertEquals(LevelConfig.forLevel(1).getTimeLimitSeconds(), initialSeconds);

        while (game.getLevelManager().getState() == LevelState.IN_PROGRESS
                && game.getLevelTimer().getRemainingSeconds() > 0) {
            controller.execute(game, "look", null);
        }

        assertEquals(LevelState.FAILED, game.getLevelManager().getState());
        assertEquals(0, game.getLevelTimer().getRemainingSeconds());
        assertFalse(game.getLevelTimer().isActive());
    }

    /**
     * IT-03：第 3 关西楼困锁，合成锤子后 use 出门。
     */
    @Test
    public void integrationLevelThreeWestBuildingEscape() {
        advanceToLevel(3);

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
    }

    /**
     * IT-04：第 2 关 submit、条件门与 sleep 串联。
     */
    @Test
    public void integrationLevelTwoSubmitGatedDoorAndSleep() {
        advanceToLevel(2);
        int beforeTimer = game.getLevelTimer().getRemainingSeconds();

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        NpcDialogHelper.performTalk(game);
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);

        GameGuiController.CommandResult denied = controller.execute(game, "go", "north");
        assertEquals("supermarket", game.getCurrentRoom().getRoomId());
        assertNotNull(denied.getGatedDenialMessage());

        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);
        assertTrue(game.getLevelManager().isDormitorySubmitCompleted());

        controller.execute(game, "go", "north");
        assertEquals("dormitory", game.getCurrentRoom().getRoomId());
        assertTrue(game.getLevelTimer().getRemainingSeconds() < beforeTimer);

        controller.execute(game, "sleep", null);
        assertEquals(3, game.getLevelManager().getCurrentLevel());
    }

    /**
     * IT-05：连续移动扣时 + 第三关黑暗罚时。
     */
    @Test
    public void integrationContinuousMoveAndDarkPenaltyDeductTime() {
        int start = game.getLevelTimer().getRemainingSeconds();

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        int afterTwoMoves = game.getLevelTimer().getRemainingSeconds();
        assertEquals(start - ActionTimeCost.GO * 2, afterTwoMoves);

        advanceToLevel(3);
        int beforeDark = game.getLevelTimer().getRemainingSeconds();
        GameGuiController.CommandResult darkTry = controller.execute(game, "go", "north");

        assertTrue(darkTry.isDarkPenaltyTriggered());
        assertEquals("gate", game.getCurrentRoom().getRoomId());
        assertEquals(beforeDark - ActionTimeCost.DARK_PENALTY, game.getLevelTimer().getRemainingSeconds());
    }

    /**
     * IT-06：第 4 关喂猫得饼干，食用后倒计时增加。
     */
    @Test
    public void integrationFeedCatThenCookieExtendsTimer() {
        advanceToLevel(4);
        assertTrue(game.getCommandManager().isFeedCommandAvailable());

        controller.execute(game, "go", "east");
        controller.execute(game, "take", FeedCommand.SAUSAGE_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "go", "west");
        controller.execute(game, "take", DarkRoom.FLASHLIGHT_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");

        int beforeFeed = game.getLevelTimer().getRemainingSeconds();
        GameGuiController.CommandResult feedResult = controller.execute(game, "feed", null);
        assertTrue(feedResult.joinedOutput().contains("魔法饼干"));
        assertNull(game.getPlayer().findItemInInventory(FeedCommand.SAUSAGE_ITEM));
        assertNotNull(game.getPlayer().findMagicCookie());
        assertEquals(beforeFeed - ActionTimeCost.FEED, game.getLevelTimer().getRemainingSeconds());

        int beforeEat = game.getLevelTimer().getRemainingSeconds();
        GameGuiController.CommandResult eatResult = controller.execute(
            game, "eat", FoodItems.MAGIC_COOKIE);
        assertTrue(eatResult.joinedOutput().contains("magic cookie"));
        assertEquals(
            beforeEat - ActionTimeCost.EAT + ActionTimeCost.COOKIE_BONUS,
            game.getLevelTimer().getRemainingSeconds());
        assertFalse(game.getLevelManager().isMagicCookieBonusAvailable());
    }

    private void advanceToLevel(int targetLevel) {
        while (game.getLevelManager().getCurrentLevel() < targetLevel) {
            game.getLevelManager().completeCurrentLevel();
        }
        assertEquals(targetLevel, game.getLevelManager().getCurrentLevel());
    }
}

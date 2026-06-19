package cn.edu.whut.sept.zuul.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.gui.NpcDialogHelper;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T5 系统测试：五关主路径端到端（文本命令链，无人工输入）。
 * 对应 {@code docs/系统测试用例.md} 中 ST-L01—ST-L05。
 */
public class T5SystemPathTest {

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

  /** ST-L01：第一关大门 → 换卡 → sleep。 */
    @Test
    public void stL01LevelOneMainPath() {
        assertEquals(1, game.getLevelManager().getCurrentLevel());

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));

        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);

        assertEquals(2, game.getLevelManager().getCurrentLevel());
        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
    }

  /** ST-L02：第二关领单 → 换卡 → submit → sleep。 */
    @Test
    public void stL02LevelTwoMainPath() {
        advanceToLevel(2);

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        NpcDialogHelper.performTalk(game);
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);
        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);

        assertEquals(3, game.getLevelManager().getCurrentLevel());
    }

  /** ST-L03：第三关手电 → 双证 → 西楼锤子 → sleep。 */
    @Test
    public void stL03LevelThreeMainPath() {
        advanceToLevel(3);

        controller.execute(game, "go", "west");
        controller.execute(game, "take", DarkRoom.FLASHLIGHT_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        NpcDialogHelper.performTalk(game);
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);

        controller.execute(game, "go", "east");
        controller.execute(game, "go", "south");
        controller.execute(game, "go", "west");
        controller.execute(game, "take", CombineCommand.STICK_ITEM);
        controller.execute(game, "take", CombineCommand.STONE_ITEM);
        controller.execute(game, "take", CombineCommand.ROPE_ITEM);
        controller.execute(game, "combine", null);
        controller.execute(game, "use", UseCommand.HAMMER_ITEM);
        assertFalse(game.getLevelManager().isWestBuildingExitLocked());

        controller.execute(game, "go", "east");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "west");
        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);

        assertEquals(4, game.getLevelManager().getCurrentLevel());
    }

  /** ST-L04：第四关图书馆领单 → submit → sleep（喂猫为可选支线）。 */
    @Test
    public void stL04LevelFourMainPath() {
        advanceToLevel(4);

        controller.execute(game, "go", "west");
        controller.execute(game, "take", DarkRoom.FLASHLIGHT_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "east");
        NpcDialogHelper.performTalk(game);
        controller.execute(game, "go", "west");
        controller.execute(game, "go", "west");
        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);
        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);

        assertEquals(5, game.getLevelManager().getCurrentLevel());
    }

  /** ST-L05：第五关全图 → 解锁寝室 → 全通。 */
    @Test
    public void stL05LevelFiveMainPath() {
        advanceToLevel(5);

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "east");
        controller.execute(game, "go", "east");
        NpcDialogHelper.performTalk(game);
        controller.execute(game, "go", "west");
        controller.execute(game, "go", "west");
        controller.execute(game, "submit", UseCommand.DORM_FORM_ITEM);
        controller.execute(game, "go", "north");
        controller.execute(game, "unlock", UnlockService.DORMITORY_PASSWORD);
        controller.execute(game, "sleep", null);

        assertEquals(LevelState.GAME_WON, game.getLevelManager().getState());
        assertTrue(game.getLevelManager().isGameWon());
    }

  /** ST-L04-OPT：第四关喂猫支线（彭慧星 E5/E7 范围）。 */
    @Test
    public void stL04OptionalFeedCatPath() {
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

        GameGuiController.CommandResult feedResult = controller.execute(game, "feed", null);
        assertTrue(feedResult.joinedOutput().contains("魔法饼干"));
        assertNotNull(game.getPlayer().findMagicCookie());
    }

    private void advanceToLevel(int targetLevel) {
        while (game.getLevelManager().getCurrentLevel() < targetLevel) {
            game.getLevelManager().completeCurrentLevel();
        }
        assertEquals(targetLevel, game.getLevelManager().getCurrentLevel());
    }
}

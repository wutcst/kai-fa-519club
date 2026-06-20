package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.command.TalkCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.npc.NpcService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NpcDialogHelper 单元测试（F7 阶段 2）。
 */
public class NpcDialogHelperTest {

    private Game game;

    @BeforeEach
    public void setUp() {
        game = new Game();
    }

    @Test
    public void shouldShowNpcInSupermarketAndNorthBuilding() {
        assertTrue(NpcDialogHelper.shouldShowNpc(UseCommand.SUPERMARKET_ROOM_ID, 1));
        assertTrue(NpcDialogHelper.shouldShowNpc(NpcService.NORTH_BUILDING_ROOM_ID, 2));
    }

    @Test
    public void libraryNpcOnlyFromLevelFour() {
        assertFalse(NpcDialogHelper.shouldShowNpc(UseCommand.LIBRARY_ROOM_ID, 3));
        assertTrue(NpcDialogHelper.shouldShowNpc(UseCommand.LIBRARY_ROOM_ID, 4));
    }

    @Test
    public void performTalkIssuesDormFormAtLevelTwoWithoutTalkCommandPenalty() {
        game.getLevelManager().completeCurrentLevel();
        assertEquals(2, game.getLevelManager().getCurrentLevel());

        game.getCommandManager().executeCommand("go", "north", game);
        game.getCommandManager().executeCommand("go", "north", game);
        assertEquals(NpcService.NORTH_BUILDING_ROOM_ID, game.getCurrentRoom().getRoomId());

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        java.util.List<String> lines = NpcDialogHelper.performTalk(game);

        assertTrue(lines.stream().anyMatch(line -> line.contains("志愿者")));
        assertNotNull(game.getPlayer().findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertEquals(beforeSeconds, game.getLevelTimer().getRemainingSeconds());

        int beforeTalkCommand = game.getLevelTimer().getRemainingSeconds();
        new TalkCommand().execute(game, null);
        assertTrue(game.getLevelTimer().getRemainingSeconds() < beforeTalkCommand);
    }

    @Test
    public void canSubmitAtSupermarketFromLevelTwo() {
        game.getLevelManager().completeCurrentLevel();
        game.getCommandManager().executeCommand("go", "north", game);
        game.getCommandManager().executeCommand("go", "north", game);
        NpcDialogHelper.performTalk(game);
        game.getCommandManager().executeCommand("go", "west", game);
        assertEquals(UseCommand.SUPERMARKET_ROOM_ID, game.getCurrentRoom().getRoomId());
        assertTrue(NpcDialogHelper.canSubmitAtSupermarket(game));
    }
}

/**
 * SubmitCommand 单元测试：寝室门口提交归寝单与错物失败（E2 #19）。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.command.SubmitCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SubmitCommand 功能测试。
 */
public class SubmitCommandTest {

    private Game game;
    private Player player;
    private SubmitCommand submitCommand;
    private GoCommand goCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        submitCommand = new SubmitCommand();
        goCommand = new GoCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    private void setCurrentRoom(Room room) throws Exception {
        player.setCurrentRoom(room);
        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);
    }

    private void advanceToLevel(int level) {
        while (game.getLevelManager().getCurrentLevel() < level) {
            game.getLevelManager().completeCurrentLevel();
        }
    }

    @Test
    public void testSubmitDormFormInSupermarketLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);

        assertTrue(game.getLevelManager().isDormitorySubmitCompleted());
        assertNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertEquals(beforeSeconds - ActionTimeCost.SUBMIT, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("寝室门口"));
    }

    @Test
    public void testSubmitWithdrawalSlipInSupermarketLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        player.takeItem(new Item(SubmitCommand.WITHDRAWAL_SLIP_ITEM, 5));

        submitCommand.execute(game, SubmitCommand.WITHDRAWAL_SLIP_ITEM);

        assertTrue(game.getLevelManager().isDormitorySubmitCompleted());
        assertNull(player.findItemInInventory(SubmitCommand.WITHDRAWAL_SLIP_ITEM));
    }

    @Test
    public void testSubmitWrongItemFailsLevel() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        player.takeItem(new Item(SubmitCommand.WRONG_MEAL_CARD_ITEM, 5));

        submitCommand.execute(game, SubmitCommand.WRONG_MEAL_CARD_ITEM);

        assertFalse(game.getLevelManager().isDormitorySubmitCompleted());
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());
        assertTrue(outContent.toString().contains("本关失败"));
    }

    @Test
    public void testSubmitThenEnterDormitory() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);
        goCommand.execute(game, "north");

        assertEquals(game.getRoomById("dormitory"), game.getCurrentRoom());
    }

    @Test
    public void testSubmitNotRequiredOnLevelOne() throws Exception {
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);

        assertFalse(game.getLevelManager().isDormitorySubmitCompleted());
        assertTrue(outContent.toString().contains("无需提交"));
    }

    @Test
    public void testSubmitInWrongRoom() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById("gate"));
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);

        assertFalse(game.getLevelManager().isDormitorySubmitCompleted());
        assertTrue(outContent.toString().contains("教育超市"));
    }

    @Test
    public void testSubmitWithoutItem() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));

        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);

        assertFalse(game.getLevelManager().isDormitorySubmitCompleted());
        assertTrue(outContent.toString().contains("你没有"));
    }

    @Test
    public void testSubmitWhenAlreadyCompleted() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));
        game.getLevelManager().markDormitorySubmitCompleted();
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        submitCommand.execute(game, UseCommand.DORM_FORM_ITEM);

        assertNotNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertTrue(outContent.toString().contains("已经提交过"));
    }

    @Test
    public void testSubmitCommandRegistered() {
        assertTrue(containsCommand(game.getCommandManager().getCommandWords(), "submit"));
    }

    @Test
    public void testHelpShowsSubmitDescription() {
        outContent.reset();
        game.processCommand(new Command("help", null));
        assertTrue(outContent.toString().contains(SubmitCommand.getUsageDescription()));
    }

    private boolean containsCommand(String[] commandWords, String target) {
        for (String word : commandWords) {
            if (word.equals(target)) {
                return true;
            }
        }
        return false;
    }
}

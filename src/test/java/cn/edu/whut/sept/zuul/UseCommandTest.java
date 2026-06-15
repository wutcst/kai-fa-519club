/**
 * UseCommand 单元测试：超市换卡、西楼砸锁、图书馆盖章及异常路径。
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

import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * UseCommand 功能测试。
 */
public class UseCommandTest {

    private Game game;
    private Player player;
    private UseCommand useCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        useCommand = new UseCommand();
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
    public void testUseMoneyInSupermarket() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);
        player.takeItem(new Item(UseCommand.MONEY_ITEM, 10));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        useCommand.execute(game, UseCommand.MONEY_ITEM);

        String output = outContent.toString();
        assertTrue(output.contains("一卡通"));
        assertNull(player.findItemInInventory(UseCommand.MONEY_ITEM));
        assertNotNull(player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
        assertEquals(beforeSeconds - 25, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testUseRegistrationSlipInSupermarket() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);
        player.takeItem(new Item(UseCommand.REGISTRATION_SLIP_ITEM, 5));

        useCommand.execute(game, UseCommand.REGISTRATION_SLIP_ITEM);

        String output = outContent.toString();
        assertTrue(output.contains("一卡通"));
        assertNull(player.findItemInInventory(UseCommand.REGISTRATION_SLIP_ITEM));
        assertNotNull(player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
    }

    @Test
    public void testUseWithoutItem() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);

        useCommand.execute(game, UseCommand.MONEY_ITEM);

        assertTrue(outContent.toString().contains("你没有"));
    }

    @Test
    public void testUseMoneyInWrongRoom() throws Exception {
        Room gate = game.getRoomById("gate");
        setCurrentRoom(gate);
        player.takeItem(new Item(UseCommand.MONEY_ITEM, 10));

        useCommand.execute(game, UseCommand.MONEY_ITEM);

        assertTrue(outContent.toString().contains("无法使用"));
        assertNotNull(player.findItemInInventory(UseCommand.MONEY_ITEM));
        assertNull(player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
    }

    @Test
    public void testUseWithoutParameter() throws Exception {
        useCommand.execute(game, null);

        assertTrue(outContent.toString().contains("请指定要使用的物品"));
    }

    @Test
    public void testUseWhenAlreadyHasCampusCard() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);
        player.takeItem(new Item(UseCommand.MONEY_ITEM, 10));
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));

        useCommand.execute(game, UseCommand.MONEY_ITEM);

        assertTrue(outContent.toString().contains("已经有一卡通"));
        assertNotNull(player.findItemInInventory(UseCommand.MONEY_ITEM));
    }

    @Test
    public void testUseHammerInWestBuilding() throws Exception {
        advanceToLevel(3);
        Room west = game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID);
        game.setCurrentRoom(west);
        player.takeItem(new Item(UseCommand.HAMMER_ITEM, 200));

        assertTrue(game.getLevelManager().isWestBuildingExitLocked());

        useCommand.execute(game, UseCommand.HAMMER_ITEM);

        String output = outContent.toString();
        assertTrue(output.contains("砸开"));
        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
        assertNull(player.findItemInInventory(UseCommand.HAMMER_ITEM));
    }

    @Test
    public void testUseReceiptInLibrary() throws Exception {
        advanceToLevel(4);
        Room library = game.getRoomById(UseCommand.LIBRARY_ROOM_ID);
        setCurrentRoom(library);
        player.takeItem(new Item(UseCommand.RECEIPT_ITEM, 5));

        useCommand.execute(game, UseCommand.RECEIPT_ITEM);

        String output = outContent.toString();
        assertTrue(output.contains("归寝单"));
        assertNull(player.findItemInInventory(UseCommand.RECEIPT_ITEM));
        assertNotNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
    }

    @Test
    public void testUseStopwatchShowsRemainingMinutes() throws Exception {
        advanceToLevel(4);
        setCurrentRoom(game.getRoomById("gate"));
        player.takeItem(new Item(UseCommand.STOPWATCH_ITEM, 50));

        useCommand.execute(game, UseCommand.STOPWATCH_ITEM);

        assertTrue(outContent.toString().contains("surprise！你的时间还剩"));
    }

    @Test
    public void testUseProjectorRemoteWastesTime() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById("gate"));
        player.takeItem(new Item(UseCommand.PROJECTOR_REMOTE_ITEM, 30));
        int before = game.getLevelTimer().getRemainingSeconds();

        useCommand.execute(game, UseCommand.PROJECTOR_REMOTE_ITEM);

        assertTrue(outContent.toString().contains("什么都没有发生"));
        assertEquals(before - 25, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testHelpShowsUseDescription() {
        outContent.reset();
        game.processCommand(new Command("help", null));

        assertTrue(outContent.toString().contains("use"));
        assertTrue(outContent.toString().contains(UseCommand.getUsageDescription()));
    }

    @Test
    public void testUseCommandRegistered() {
        String[] words = game.getCommandManager().getCommandWords();
        boolean found = false;
        for (String word : words) {
            if ("use".equals(word)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}

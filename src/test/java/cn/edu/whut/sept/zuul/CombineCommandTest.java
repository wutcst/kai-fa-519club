/**
 * CombineCommand 单元测试：西楼合成锤子及与 use 砸锁衔接（E3 #20）。
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

import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * CombineCommand 功能测试。
 */
public class CombineCommandTest {

    private Game game;
    private Player player;
    private CombineCommand combineCommand;
    private UseCommand useCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        combineCommand = new CombineCommand();
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

    private void giveAllMaterials() {
        player.takeItem(new Item(CombineCommand.STICK_ITEM, 50));
        player.takeItem(new Item(CombineCommand.STONE_ITEM, 80));
        player.takeItem(new Item(CombineCommand.ROPE_ITEM, 30));
    }

    @Test
    public void testCombineInWestBuilding() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID));
        giveAllMaterials();

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        combineCommand.execute(game, null);

        assertNull(player.findItemInInventory(CombineCommand.STICK_ITEM));
        assertNull(player.findItemInInventory(CombineCommand.STONE_ITEM));
        assertNull(player.findItemInInventory(CombineCommand.ROPE_ITEM));
        assertNotNull(player.findItemInInventory(UseCommand.HAMMER_ITEM));
        assertEquals(beforeSeconds - 25, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("锤子"));
    }

    @Test
    public void testCombineWithMissingMaterial() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID));
        player.takeItem(new Item(CombineCommand.STICK_ITEM, 50));
        player.takeItem(new Item(CombineCommand.STONE_ITEM, 80));

        combineCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(CombineCommand.STICK_ITEM));
        assertNull(player.findItemInInventory(UseCommand.HAMMER_ITEM));
        assertTrue(outContent.toString().contains("材料不齐"));
    }

    @Test
    public void testCombineInWrongRoom() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById("gate"));
        giveAllMaterials();

        combineCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(CombineCommand.STICK_ITEM));
        assertNull(player.findItemInInventory(UseCommand.HAMMER_ITEM));
        assertTrue(outContent.toString().contains("只能在博学西楼"));
    }

    @Test
    public void testCombineWhenAlreadyHasHammer() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID));
        giveAllMaterials();
        player.takeItem(new Item(UseCommand.HAMMER_ITEM, 200));

        combineCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(CombineCommand.STICK_ITEM));
        assertTrue(outContent.toString().contains("已经有一把锤子"));
    }

    @Test
    public void testCombineThenUseHammerToUnlock() throws Exception {
        advanceToLevel(3);
        Room west = game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID);
        game.setCurrentRoom(west);
        giveAllMaterials();

        assertTrue(game.getLevelManager().isWestBuildingExitLocked());

        combineCommand.execute(game, null);
        useCommand.execute(game, UseCommand.HAMMER_ITEM);

        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
        assertNull(player.findItemInInventory(UseCommand.HAMMER_ITEM));
        assertTrue(outContent.toString().contains("砸开"));
    }

    @Test
    public void testCombineCommandRegistered() {
        String[] words = game.getCommandManager().getCommandWords();
        assertTrue(containsCommand(words, "combine"));
    }

    @Test
    public void testHelpShowsCombineDescription() {
        outContent.reset();
        game.processCommand(new Command("help", null));

        assertTrue(outContent.toString().contains(CombineCommand.getUsageDescription()));
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

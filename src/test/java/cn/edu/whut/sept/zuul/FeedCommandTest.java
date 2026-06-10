/**
 * FeedCommand 单元测试：北楼喂猫、火腿肠校验与耗时扣减。
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.npc.NpcService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * FeedCommand 功能测试。
 */
public class FeedCommandTest {

    private Game game;
    private Player player;
    private FeedCommand feedCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        feedCommand = new FeedCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    private void setCurrentRoom(Room room) throws Exception {
        player.setCurrentRoom(room);
        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);
    }

    @Test
    public void testFeedWithoutSausage() throws Exception {
        goToLevel(4);
        Room northBuilding = game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID);
        setCurrentRoom(northBuilding);

        feedCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("你没有火腿肠"));
        assertNull(player.findMagicCookie());
    }

    @Test
    public void testFeedOutsideNorthBuilding() throws Exception {
        player.takeItem(new Item(FeedCommand.SAUSAGE_ITEM, 10));
        Room gate = game.getRoomById("gate");
        setCurrentRoom(gate);

        feedCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("请前往博学北楼"));
        assertNotNull(player.findItemInInventory(FeedCommand.SAUSAGE_ITEM));
        assertNull(player.findMagicCookie());
    }

    @Test
    public void testFeedNotAvailableBeforeLevel4() throws Exception {
        assertFalse(game.getCommandManager().isFeedCommandAvailable());
        Room northBuilding = game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID);
        setCurrentRoom(northBuilding);
        player.takeItem(new Item(FeedCommand.SAUSAGE_ITEM, 10));

        game.getCommandManager().executeCommand("feed", null, game);

        assertTrue(outContent.toString().contains("I don't know"));
        assertNotNull(player.findItemInInventory(FeedCommand.SAUSAGE_ITEM));
        assertNull(player.findMagicCookie());
    }

    @Test
    public void testFeedAvailableFromLevel4() {
        goToLevel(4);
        assertTrue(game.getCommandManager().isFeedCommandAvailable());
    }

    private void goToLevel(int level) {
        while (game.getLevelManager().getCurrentLevel() < level) {
            game.getLevelManager().completeCurrentLevel();
        }
    }

    @Test
    public void testFeedSuccess() throws Exception {
        goToLevel(4);
        Room northBuilding = game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID);
        setCurrentRoom(northBuilding);
        player.takeItem(new Item(FeedCommand.SAUSAGE_ITEM, 10));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        feedCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("魔法饼干"));
        assertNull(player.findItemInInventory(FeedCommand.SAUSAGE_ITEM));
        assertNotNull(player.findMagicCookie());
        assertEquals(beforeSeconds - 60, game.getLevelTimer().getRemainingSeconds());
    }
}

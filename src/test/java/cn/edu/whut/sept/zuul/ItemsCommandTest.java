/**
 * 测试ItemsCommand功能
 *
 * @author liujing
 * @version 1.6
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.ItemsCommand;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemsCommandTest {
    private Game game;
    private Player player;
    private Room room;
    private ItemsCommand itemsCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();

        room = new Room("测试房间");
        player.setCurrentRoom(room);

        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);

        itemsCommand = new ItemsCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testItemsWithEmptyInventory() {
        itemsCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("=== 你的背包 ==="));
        assertTrue(output.contains("你没有携带任何物品"));
        assertTrue(output.contains("inspect"));
        assertFalse(output.contains("=== 房间物品 ==="));
    }

    @Test
    public void testItemsShowsOnlyInventory() {
        room.addItem(new Item("房间物品", 200));
        player.takeItem(new Item("背包物品", 100));

        outContent.reset();
        itemsCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("背包物品 (重量: 100g)"));
        assertFalse(output.contains("房间物品"));
        assertFalse(output.contains("房间物品总重量"));
    }
}

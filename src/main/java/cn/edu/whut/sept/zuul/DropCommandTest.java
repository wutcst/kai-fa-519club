/**
 * 测试DropCommand功能
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class DropCommandTest {
    private Game game;
    private Player player;
    private Room room;
    private DropCommand dropCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        // 创建游戏实例
        game = new Game();
        player = game.getPlayer();

        // 创建测试房间并设置为当前房间
        room = new Room("测试房间");
        player.setCurrentRoom(room);

        // 使用反射设置当前房间
        java.lang.reflect.Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);

        dropCommand = new DropCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 测试丢弃指定物品
     */
    @Test
    public void testDropItem() {
        // 玩家拾取物品
        Item item = new Item("测试物品", 500);
        player.takeItem(item);
        assertEquals(1, player.getInventory().size());
        assertEquals(500, player.getCurrentWeight());

        // 清空输出
        outContent.reset();

        // 执行drop命令
        dropCommand.execute(game, "测试物品");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示丢弃成功", output.contains("你丢弃了: 测试物品 (重量: 500g)"));
        assertTrue("应显示当前负重", output.contains("当前负重: 0g / 3000g"));

        // 验证物品已从玩家物品栏移除
        assertEquals(0, player.getInventory().size());
        assertEquals(0, player.getCurrentWeight());

        // 验证物品已放回房间
        List<Item> roomItems = room.getItems();
        assertEquals(1, roomItems.size());
        assertEquals("测试物品", roomItems.get(0).getDescription());
    }

    /**
     * 测试丢弃不存在的物品
     */
    @Test
    public void testDropNonexistentItem() {
        // 玩家没有携带任何物品
        dropCommand.execute(game, "不存在的物品");

        String output = outContent.toString();
        assertTrue("应提示没有该物品", output.contains("你没有携带 '不存在的物品'"));
    }

    /**
     * 测试丢弃所有物品
     */
    @Test
    public void testDropAllItems() {
        // 玩家拾取多个物品
        Item item1 = new Item("物品1", 500);
        Item item2 = new Item("物品2", 300);
        Item item3 = new Item("物品3", 700);

        player.takeItem(item1);
        player.takeItem(item2);
        player.takeItem(item3);

        assertEquals(3, player.getInventory().size());
        assertEquals(1500, player.getCurrentWeight());

        // 清空输出
        outContent.reset();

        // 执行drop all命令
        dropCommand.execute(game, "all");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示丢弃所有物品", output.contains("你丢弃了所有物品:"));
        assertTrue("应显示物品1", output.contains("物品1 (重量: 500g)"));
        assertTrue("应显示物品2", output.contains("物品2 (重量: 300g)"));
        assertTrue("应显示物品3", output.contains("物品3 (重量: 700g)"));
        assertTrue("应显示当前负重", output.contains("当前负重: 0g / 3000g"));

        // 验证玩家物品栏为空
        assertEquals(0, player.getInventory().size());
        assertEquals(0, player.getCurrentWeight());

        // 验证所有物品已放回房间
        List<Item> roomItems = room.getItems();
        assertEquals(3, roomItems.size());
    }

    /**
     * 测试丢弃所有物品（空物品栏）
     */
    @Test
    public void testDropAllWhenInventoryEmpty() {
        dropCommand.execute(game, "all");

        String output = outContent.toString();
        assertTrue("应提示没有携带物品", output.contains("你没有携带任何物品"));
    }

    /**
     * 测试不带参数的drop命令
     */
    @Test
    public void testDropWithoutParameter() {
        dropCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应提示需要指定物品", output.contains("Drop what? 请指定要丢弃的物品"));
    }

    /**
     * 测试丢弃带有空格的物品名称
     */
    @Test
    public void testDropItemWithSpaces() {
        // 玩家拾取带有空格的物品
        Item item = new Item("一张校园地图", 100);
        player.takeItem(item);

        // 清空输出
        outContent.reset();

        // 执行drop命令
        dropCommand.execute(game, "一张校园地图");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示丢弃成功", output.contains("你丢弃了: 一张校园地图 (重量: 100g)"));

        // 验证物品已从玩家物品栏移除
        assertEquals(0, player.getInventory().size());

        // 验证物品已放回房间
        assertEquals(1, room.getItems().size());
    }
}
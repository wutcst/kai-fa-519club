/**
 * 测试TakeCommand功能
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.TakeCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TakeCommandTest {
    private Game game;
    private Player player;
    private Room room;
    private TakeCommand takeCommand;
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
        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);

        takeCommand = new TakeCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 测试拾取房间中的物品
     */
    @Test
    public void testTakeItemFromRoom() throws Exception {
        // 添加物品到房间
        Item item = new Item("测试物品", 500);
        room.addItem(item);

        // 执行take命令
        takeCommand.execute(game, "测试物品");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示拾取成功信息", output.contains("你拾取了: 测试物品 (重量: 500g)"));
        assertTrue("应显示剩余负重", output.contains("剩余负重: 2500g / 3000g"));

        // 验证物品已从房间移除
        List<Item> roomItems = room.getItems();
        assertEquals(0, roomItems.size());

        // 验证物品已添加到玩家物品栏
        List<Item> inventory = player.getInventory();
        assertEquals(1, inventory.size());
        assertEquals("测试物品", inventory.get(0).getDescription());
    }

    /**
     * 测试拾取不存在的物品
     */
    @Test
    public void testTakeNonexistentItem() {
        // 执行take命令
        takeCommand.execute(game, "不存在的物品");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应提示物品不存在", output.contains("这个房间里没有 '不存在的物品'"));
    }

    /**
     * 测试拾取物品超过负重限制
     */
    @Test
    public void testTakeItemExceedsWeight() throws Exception {
        // 先让玩家携带一些物品，接近负重限制
        Item heavyItem = new Item("重物", 2800);
        player.takeItem(heavyItem);

        // 添加另一个物品到房间
        Item newItem = new Item("新物品", 500);
        room.addItem(newItem);

        // 清空输出
        outContent.reset();

        // 执行take命令
        takeCommand.execute(game, "新物品");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应提示物品太重", output.contains("你无法拾取 '新物品', 它太重了！"));
        assertTrue("应显示当前负重", output.contains("当前负重: 2800g / 3000g"));
        assertTrue("应显示所需重量和剩余重量", output.contains("需要: 500g, 但只剩: 200g"));

        // 验证物品仍在房间中
        List<Item> roomItems = room.getItems();
        assertEquals(1, roomItems.size());

        // 验证玩家物品栏没有增加
        assertEquals(1, player.getInventory().size());
    }

    /**
     * 测试拾取魔法饼干
     */
    @Test
    public void testTakeMagicCookie() throws Exception {
        // 添加魔法饼干到房间
        Item magicCookie = new Item("magic cookie", 100);
        room.addItem(magicCookie);

        // 执行take命令
        takeCommand.execute(game, "magic cookie");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示拾取成功", output.contains("你拾取了: magic cookie (重量: 100g)"));

        // 验证物品已从房间移除
        assertEquals(0, room.getItems().size());

        // 验证物品已添加到玩家物品栏
        assertEquals(1, player.getInventory().size());
        assertEquals("magic cookie", player.getInventory().get(0).getDescription());
    }

    /**
     * 测试不带参数的take命令
     */
    @Test
    public void testTakeWithoutParameter() {
        takeCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应提示需要指定物品", output.contains("Take what? 请指定要拾取的物品。"));
    }

    /**
     * 测试拾取带有空格的物品名称
     */
    @Test
    public void testTakeItemWithSpaces() throws Exception {
        // 添加带有空格的物品
        Item item = new Item("一张校园地图", 100);
        room.addItem(item);

        // 执行take命令
        takeCommand.execute(game, "一张校园地图");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示拾取成功", output.contains("你拾取了: 一张校园地图 (重量: 100g)"));

        // 验证物品已从房间移除
        assertEquals(0, room.getItems().size());

        // 验证物品已添加到玩家物品栏
        assertEquals(1, player.getInventory().size());
    }
}
/**
 * 测试ItemsCommand功能
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ItemsCommandTest {
    private Game game;
    private Player player;
    private Room room;
    private ItemsCommand itemsCommand;
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

        itemsCommand = new ItemsCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 测试显示空房间和空物品栏
     */
    @Test
    public void testItemsWithEmptyRoomAndInventory() {
        itemsCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应显示房间物品标题", output.contains("=== 房间物品 ==="));
        assertTrue("应提示房间没有物品", output.contains("这个房间里没有任何物品"));
        assertTrue("应显示玩家物品标题", output.contains("=== 你的物品 ==="));
        assertTrue("应提示没有携带物品", output.contains("你没有携带任何物品"));
    }

    /**
     * 测试显示房间物品和玩家物品
     */
    @Test
    public void testItemsWithRoomAndInventoryItems() {
        // 添加物品到房间
        room.addItem(new Item("房间物品1", 200));
        room.addItem(new Item("房间物品2", 300));

        // 玩家拾取物品
        Item playerItem1 = new Item("玩家物品1", 400);
        Item playerItem2 = new Item("玩家物品2", 600);
        player.takeItem(playerItem1);
        player.takeItem(playerItem2);

        // 清空输出
        outContent.reset();

        // 执行items命令
        itemsCommand.execute(game, null);

        String output = outContent.toString();

        // 验证房间物品
        assertTrue("应显示房间物品标题", output.contains("=== 房间物品 ==="));
        assertTrue("应显示房间物品1", output.contains("房间物品1 (重量: 200g)"));
        assertTrue("应显示房间物品2", output.contains("房间物品2 (重量: 300g)"));
        assertTrue("应显示房间物品总重量", output.contains("房间物品总重量: 500g"));

        // 验证玩家物品
        assertTrue("应显示玩家物品标题", output.contains("=== 你的物品 ==="));
        assertTrue("应显示玩家物品1", output.contains("玩家物品1 (重量: 400g)"));
        assertTrue("应显示玩家物品2", output.contains("玩家物品2 (重量: 600g)"));
        assertTrue("应显示总重量", output.contains("总重量: 1000g / 3000g"));
    }

    /**
     * 测试只显示房间物品（玩家物品栏为空）
     */
    @Test
    public void testItemsWithOnlyRoomItems() {
        // 添加物品到房间
        room.addItem(new Item("魔法饼干", 100));
        room.addItem(new Item("一本教科书", 800));

        itemsCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应显示房间物品", output.contains("魔法饼干 (重量: 100g)"));
        assertTrue("应显示房间物品总重量", output.contains("房间物品总重量: 900g"));
        assertTrue("应提示没有携带物品", output.contains("你没有携带任何物品"));
    }

    /**
     * 测试只显示玩家物品（房间为空）
     */
    @Test
    public void testItemsWithOnlyInventoryItems() {
        // 玩家拾取物品
        player.takeItem(new Item("一把钥匙", 50));
        player.takeItem(new Item("一个钱包", 200));

        itemsCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应提示房间没有物品", output.contains("这个房间里没有任何物品"));
        assertTrue("应显示玩家物品", output.contains("一把钥匙 (重量: 50g)"));
        assertTrue("应显示玩家物品", output.contains("一个钱包 (重量: 200g)"));
        assertTrue("应显示总重量", output.contains("总重量: 250g / 3000g"));
    }
}
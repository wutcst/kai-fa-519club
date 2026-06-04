/**
 * 测试Player类的核心功能，包括物品管理和负重系统
 * 新增：测试拾取、丢弃物品、负重限制和魔法饼干功能
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlayerTest {
    private Player player;
    private Room testRoom;

    @Before
    public void setUp() {
        testRoom = new Room("测试房间");
        player = new Player("测试玩家", testRoom);
    }

    /**
     * 测试Player初始化是否正确
     */
    @Test
    public void testPlayerInitialization() {
        assertEquals("测试玩家", player.getName());
        assertEquals(testRoom, player.getCurrentRoom());
        assertEquals(3000, player.getMaxWeight());
        assertEquals(0, player.getCurrentWeight());
        assertTrue(player.getInventory().isEmpty());
        assertEquals(3000, player.getRemainingCapacity());
    }

    /**
     * 测试拾取物品功能
     */
    @Test
    public void testTakeItem() {
        Item item = new Item("测试物品", 500);
        assertTrue("应该能拾取物品", player.takeItem(item));
        assertEquals(500, player.getCurrentWeight());
        assertEquals(1, player.getInventory().size());
        assertTrue(player.getInventory().contains(item));
        assertEquals(2500, player.getRemainingCapacity());
    }

    /**
     * 测试拾取物品超过负重限制
     */
    @Test
    public void testTakeItemExceedsWeightLimit() {
        Item heavyItem = new Item("重物", 3500);
        assertFalse("不应拾取超过负重的物品", player.takeItem(heavyItem));
        assertEquals(0, player.getCurrentWeight());
        assertTrue(player.getInventory().isEmpty());
    }

    /**
     * 测试丢弃指定物品
     */
    @Test
    public void testDropItem() {
        Item item1 = new Item("物品1", 500);
        Item item2 = new Item("物品2", 800);

        player.takeItem(item1);
        player.takeItem(item2);
        assertEquals(1300, player.getCurrentWeight());

        // 丢弃物品1
        Item dropped = player.dropItem("物品1");
        assertNotNull(dropped);
        assertEquals(item1, dropped);
        assertEquals(800, player.getCurrentWeight());
        assertEquals(1, player.getInventory().size());
        assertTrue(player.getInventory().contains(item2));
    }

    /**
     * 测试丢弃不存在的物品
     */
    @Test
    public void testDropNonexistentItem() {
        Item item = new Item("测试物品", 500);
        player.takeItem(item);

        Item dropped = player.dropItem("不存在的物品");
        assertNull(dropped);
        assertEquals(1, player.getInventory().size());
    }

    /**
     * 测试丢弃所有物品
     */
    @Test
    public void testDropAllItems() {
        Item item1 = new Item("物品1", 500);
        Item item2 = new Item("物品2", 800);
        Item item3 = new Item("物品3", 300);

        player.takeItem(item1);
        player.takeItem(item2);
        player.takeItem(item3);
        assertEquals(1600, player.getCurrentWeight());

        List<Item> dropped = player.dropAllItems();
        assertEquals(3, dropped.size());
        assertEquals(0, player.getCurrentWeight());
        assertTrue(player.getInventory().isEmpty());
    }

    /**
     * 测试查找物品
     */
    @Test
    public void testFindItemInInventory() {
        Item item1 = new Item("物品1", 500);
        Item item2 = new Item("magic cookie", 100);

        player.takeItem(item1);
        player.takeItem(item2);

        Item found = player.findItemInInventory("物品1");
        assertNotNull(found);
        assertEquals(item1, found);

        found = player.findItemInInventory("MAGIC COOKIE");
        assertNotNull(found);
        assertEquals(item2, found);

        found = player.findItemInInventory("不存在的物品");
        assertNull(found);
    }

    /**
     * 测试查找魔法饼干
     */
    @Test
    public void testFindMagicCookie() {
        Item item1 = new Item("普通物品", 500);
        Item magicCookie = new Item("magic cookie", 100);

        player.takeItem(item1);
        assertNull("没有魔法饼干时应返回null", player.findMagicCookie());

        player.takeItem(magicCookie);
        Item found = player.findMagicCookie();
        assertNotNull("应有魔法饼干", found);
        assertEquals(magicCookie, found);
    }

    /**
     * 测试移除物品
     */
    @Test
    public void testRemoveItemFromInventory() {
        Item item1 = new Item("物品1", 500);
        Item item2 = new Item("物品2", 300);

        player.takeItem(item1);
        player.takeItem(item2);
        assertEquals(800, player.getCurrentWeight());

        assertTrue(player.removeItemFromInventory(item1));
        assertEquals(300, player.getCurrentWeight());
        assertEquals(1, player.getInventory().size());
        assertTrue(player.getInventory().contains(item2));

        // 移除不存在的物品
        assertFalse(player.removeItemFromInventory(item1));
    }

    /**
     * 测试增加最大负重
     */
    @Test
    public void testIncreaseMaxWeight() {
        assertEquals(3000, player.getMaxWeight());

        player.increaseMaxWeight(1000);
        assertEquals(4000, player.getMaxWeight());
        assertEquals(4000, player.getRemainingCapacity());

        // 拾取一些物品后再增加负重
        Item item = new Item("物品", 2000);
        player.takeItem(item);
        assertEquals(2000, player.getCurrentWeight());
        assertEquals(2000, player.getRemainingCapacity());

        player.increaseMaxWeight(500);
        assertEquals(4500, player.getMaxWeight());
        assertEquals(2500, player.getRemainingCapacity());
    }

    /**
     * 测试获取物品详情
     */
    @Test
    public void testGetInventoryDetails() {
        // 空物品栏
        String details = player.getInventoryDetails();
        assertTrue(details.contains("你没有携带任何物品"));

        // 有物品
        Item item1 = new Item("一把旧钥匙", 50);
        Item item2 = new Item("一本教科书", 800);

        player.takeItem(item1);
        player.takeItem(item2);

        details = player.getInventoryDetails();
        assertTrue(details.contains("一把旧钥匙 (重量: 50g)"));
        assertTrue(details.contains("一本教科书 (重量: 800g)"));
        assertTrue(details.contains("总重量: 850g / 3000g"));
    }

    /**
     * 测试设置当前房间
     */
    @Test
    public void testSetCurrentRoom() {
        Room newRoom = new Room("新房间");
        player.setCurrentRoom(newRoom);
        assertEquals(newRoom, player.getCurrentRoom());
    }
}
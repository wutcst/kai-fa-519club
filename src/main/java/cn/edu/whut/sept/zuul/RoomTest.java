/**
 * 测试Room类的核心功能，重点覆盖新增的物品存储功能，
 * 同时验证原有出口功能完整性，确保扩展功能不破坏原有逻辑。
 *
 * @author liujing
 * @version 1.2
 */
package cn.edu.whut.sept.zuul;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class RoomTest {

    /**
     * 测试新建房间时物品列表为空。
     * 验证Room构造函数初始化后，物品列表默认是空集合。
     */
    @Test
    public void testRoomInitialization_ItemsEmpty() {
        Room room = new Room("测试房间");
        List<Item> items = room.getItems();

        assertNotNull("物品列表不应为null", items);
        assertTrue("新建房间应无物品", items.isEmpty());
    }

    /**
     * 测试向房间添加物品后，能正确获取物品列表。
     * 验证addItem()方法能添加物品，getItems()能返回完整的物品集合（且为副本，外部修改不影响内部）。
     */
    @Test
    public void testAddItemAndGetItems() {
        // 准备测试数据
        Room room = new Room("测试房间");
        Item item1 = new Item("钥匙", 50);
        Item item2 = new Item("教科书", 800);

        // 向房间添加物品
        room.addItem(item1);
        room.addItem(item2);

        // 获取物品列表并验证
        List<Item> items = room.getItems();
        assertEquals("物品数量应与添加数量一致", 2, items.size());
        assertTrue("物品列表应包含添加的物品1", items.contains(item1));
        assertTrue("物品列表应包含添加的物品2", items.contains(item2));

        // 验证返回的是副本（外部修改不影响房间内部列表）
        items.remove(item1);
        assertEquals("房间内部物品列表不应被外部修改", 2, room.getItems().size());
    }

    /**
     * 测试房间长描述（getLongDescription()）在无物品时的展示。
     * 验证无物品时，描述包含「没有任何物品」的提示。
     */
    @Test
    public void testGetLongDescription_NoItems() {
        Room room = new Room("空房间");
        room.setExit("east", new Room("东房间")); // 设置出口（验证原有功能）

        String longDesc = room.getLongDescription();

        // 验证包含房间基础描述、出口、无物品提示
        assertTrue("长描述应包含房间基础描述", longDesc.contains("You are 空房间."));
        assertTrue("长描述应包含出口信息", longDesc.contains("Exits: east"));
        assertTrue("无物品时应显示对应提示", longDesc.contains("这个房间里没有任何物品。"));
    }

    /**
     * 测试房间长描述（getLongDescription()）在有物品时的展示。
     * 验证有物品时，描述包含所有物品的详情（描述+重量）。
     */
    @Test
    public void testGetLongDescription_WithItems() {
        // 准备测试数据
        Room room = new Room("有物品的房间");
        Item key = new Item("一把旧钥匙", 50);
        Item book = new Item("一本教科书", 800);
        room.addItem(key);
        room.addItem(book);
        room.setExit("west", new Room("西房间")); // 设置出口

        String longDesc = room.getLongDescription();

        // 验证包含基础信息、所有物品详情
        assertTrue("长描述应包含房间基础描述", longDesc.contains("You are 有物品的房间."));
        assertTrue("长描述应包含出口信息", longDesc.contains("Exits: west"));
        assertTrue("长描述应包含物品列表提示", longDesc.contains("房间里有这些物品:"));
        assertTrue("长描述应包含物品1详情", longDesc.contains("一把旧钥匙 (重量: 50g)"));
        assertTrue("长描述应包含物品2详情", longDesc.contains("一本教科书 (重量: 800g)"));
    }

    /**
     * 验证原有出口功能未受扩展影响。
     * 测试setExit()和getExit()方法的正确性，确保扩展功能不破坏原有逻辑。
     * 【修改】不依赖出口显示顺序，只验证出口方向的集合一致性
     */
    @Test
    public void testExitFunctions_Unchanged() {
        // 准备测试数据
        Room roomA = new Room("房间A");
        Room roomB = new Room("房间B");
        Room roomC = new Room("房间C");

        // 设置出口（预期方向：north、south）
        roomA.setExit("north", roomB);
        roomA.setExit("south", roomC);

        // 验证获取出口正确（原有逻辑不变）
        assertEquals("北出口应指向房间B", roomB, roomA.getExit("north"));
        assertEquals("南出口应指向房间C", roomC, roomA.getExit("south"));
        assertNull("无东出口时返回null", roomA.getExit("east"));

        // 【修改后】验证出口描述：不关心顺序，只验证包含所有预期方向
        String exitString = roomA.getLongDescription().split("\n")[1].trim(); // 提取出口行并去除空格
        String prefix = "Exits:";
        assertTrue("出口描述应包含前缀'" + prefix + "'", exitString.startsWith(prefix));

        // 提取实际的出口方向（去掉前缀，按空格分割）
        String[] actualDirections = exitString.substring(prefix.length()).trim().split(" ");
        // 预期的出口方向集合
        List<String> expectedDirections = Arrays.asList("north", "south");
        List<String> actualDirectionList = Arrays.asList(actualDirections);

        // 验证出口方向的集合相等（忽略顺序）
        assertEquals("出口方向的数量应一致", expectedDirections.size(), actualDirectionList.size());
        assertTrue("出口应包含所有预期方向", actualDirectionList.containsAll(expectedDirections));
    }

    // 其他测试方法（testRoomInitialization_ItemsEmpty、testAddItemAndGetItems等）保持不变
}
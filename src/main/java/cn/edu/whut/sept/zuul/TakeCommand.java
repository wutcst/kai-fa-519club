/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.util.List;

/**
 * 处理拾取物品的命令类
 * 新增：实现take命令，允许玩家拾取当前房间内的物品
 *
 * @author liujing
 * @version 1.5
 */
public class TakeCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Take what? 请指定要拾取的物品。");
            return false;
        }

        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();
        List<Item> roomItems = currentRoom.getItems();

        // 查找房间中的物品
        Item targetItem = null;
        for (Item item : roomItems) {
            if (item.getDescription().equalsIgnoreCase(itemName.trim())) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            System.out.println("这个房间里没有 '" + itemName + "'。");
            return false;
        }

        // 尝试拾取物品
        if (player.takeItem(targetItem)) {
            // 从房间中移除物品
            removeItemFromRoom(currentRoom, targetItem);
            System.out.println("你拾取了: " + targetItem.getDetails());

            // 显示剩余负重
            int remaining = player.getRemainingCapacity();
            System.out.println("剩余负重: " + remaining + "g / " + player.getMaxWeight() + "g");
        } else {
            System.out.println("你无法拾取 '" + itemName + "', 它太重了！");
            System.out.println("当前负重: " + player.getCurrentWeight() + "g / " +
                    player.getMaxWeight() + "g");
            System.out.println("需要: " + targetItem.getWeight() + "g, 但只剩: " +
                    player.getRemainingCapacity() + "g");
        }

        return false;
    }

    /**
     * 从房间中移除指定物品
     *
     * @param room 房间实例
     * @param itemToRemove 要移除的物品
     */
    private void removeItemFromRoom(Room room, Item itemToRemove) {
        try {
            java.lang.reflect.Field itemsField = Room.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            List<Item> items = (List<Item>) itemsField.get(room);
            items.remove(itemToRemove);
        } catch (Exception e) {
            System.out.println("无法从房间移除物品: " + e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return "take";
    }
}
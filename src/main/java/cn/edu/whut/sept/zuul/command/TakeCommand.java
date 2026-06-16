/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul.command;

import java.util.List;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理拾取物品的命令类
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

        if (player.takeItem(targetItem)) {
            removeItemFromRoom(currentRoom, targetItem);
            ActionTimeCost.deduct(game, ActionTimeCost.TAKE);
            System.out.println("你拾取了: " + targetItem.getDetails());

            int remaining = player.getRemainingCapacity();
            System.out.println("剩余负重: " + remaining + "g / " + player.getMaxWeight() + "g");
        } else {
            System.out.println("你无法拾取 '" + itemName + "', 它太重了！");
            System.out.println("当前负重: " + player.getCurrentWeight() + "g / "
                    + player.getMaxWeight() + "g");
            System.out.println("需要: " + targetItem.getWeight() + "g, 但只剩: "
                    + player.getRemainingCapacity() + "g");
        }

        return false;
    }

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

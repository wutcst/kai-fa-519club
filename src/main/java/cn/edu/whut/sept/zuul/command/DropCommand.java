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
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理丢弃物品的命令类
 *
 * @author liujing
 * @version 1.5
 */
public class DropCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String itemName) {
        Player player = game.getPlayer();

        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Drop what? 请指定要丢弃的物品，或使用 'drop all' 丢弃所有物品。");
            return false;
        }

        if (itemName.trim().equalsIgnoreCase("all")) {
            List<Item> droppedItems = player.dropAllItems();
            if (droppedItems.isEmpty()) {
                System.out.println("你没有携带任何物品。");
            } else {
                System.out.println("你丢弃了所有物品:");
                for (Item item : droppedItems) {
                    System.out.println("- " + item.getDetails());
                    player.getCurrentRoom().addItem(item);
                }
                System.out.println("当前负重: " + player.getCurrentWeight() + "g / "
                        + player.getMaxWeight() + "g");
                ActionTimeCost.deduct(game, ActionTimeCost.DROP);
            }
            return false;
        }

        Item droppedItem = player.dropItem(itemName.trim());
        if (droppedItem == null) {
            System.out.println("你没有携带 '" + itemName + "'。");
        } else {
            System.out.println("你丢弃了: " + droppedItem.getDetails());
            player.getCurrentRoom().addItem(droppedItem);
            System.out.println("当前负重: " + player.getCurrentWeight() + "g / "
                    + player.getMaxWeight() + "g");
            ActionTimeCost.deduct(game, ActionTimeCost.DROP);
        }

        return false;
    }

    @Override
    public String getCommandName() {
        return "drop";
    }
}

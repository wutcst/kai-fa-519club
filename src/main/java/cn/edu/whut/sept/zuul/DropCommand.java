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
 * 处理丢弃物品的命令类
 * 新增：实现drop命令，允许玩家丢弃身上携带的物品
 * 【修改】支持多单词物品名称
 *
 * @author liujing
 * @version 1.5
 */
public class DropCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String itemName) {
        Player player = game.getPlayer();

        // 如果没有指定物品，提示用法
        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Drop what? 请指定要丢弃的物品，或使用 'drop all' 丢弃所有物品。");
            return false;
        }

        // 处理"drop all"命令
        if (itemName.trim().equalsIgnoreCase("all")) {
            List<Item> droppedItems = player.dropAllItems();
            if (droppedItems.isEmpty()) {
                System.out.println("你没有携带任何物品。");
            } else {
                System.out.println("你丢弃了所有物品:");
                for (Item item : droppedItems) {
                    System.out.println("- " + item.getDetails());
                    // 将物品放回当前房间
                    player.getCurrentRoom().addItem(item);
                }
                System.out.println("当前负重: " + player.getCurrentWeight() + "g / " +
                        player.getMaxWeight() + "g");
            }
            return false;
        }

        // 丢弃指定物品
        Item droppedItem = player.dropItem(itemName.trim());
        if (droppedItem == null) {
            System.out.println("你没有携带 '" + itemName + "'。");
        } else {
            System.out.println("你丢弃了: " + droppedItem.getDetails());
            // 将物品放回当前房间
            player.getCurrentRoom().addItem(droppedItem);
            System.out.println("当前负重: " + player.getCurrentWeight() + "g / " +
                    player.getMaxWeight() + "g");
        }

        return false;
    }

    @Override
    public String getCommandName() {
        return "drop";
    }
}
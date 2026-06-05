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
 * 处理物品显示的命令类
 * 新增：实现items命令，显示当前房间和玩家携带的物品
 *
 * @author liujing
 * @version 1.5
 */
public class ItemsCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 显示房间物品
        System.out.println("\n=== 房间物品 ===");
        List<Item> roomItems = currentRoom.getItems();
        if (roomItems.isEmpty()) {
            System.out.println("这个房间里没有任何物品。");
        } else {
            int totalRoomWeight = 0;
            System.out.println("房间里的物品:");
            for (Item item : roomItems) {
                System.out.println("- " + item.getDetails());
                totalRoomWeight += item.getWeight();
            }
            System.out.println("房间物品总重量: " + totalRoomWeight + "g");
        }

        // 显示玩家物品
        System.out.println("\n=== 你的物品 ===");
        String inventoryDetails = player.getInventoryDetails();
        System.out.println(inventoryDetails);

        return false;
    }

    @Override
    public String getCommandName() {
        return "items";
    }
}
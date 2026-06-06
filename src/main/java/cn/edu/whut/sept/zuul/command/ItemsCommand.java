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

/**
 * 处理物品显示的命令类
 *
 * @author liujing
 * @version 1.5
 */
public class ItemsCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

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

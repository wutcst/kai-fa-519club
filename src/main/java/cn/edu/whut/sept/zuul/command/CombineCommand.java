/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理物品合成的命令类（E3）：西楼内棍子 + 石头 + 绳子 → 锤子。
 *
 * @author liujing
 * @version 1.0
 */
public class CombineCommand implements CommandInterface {

    public static final String STICK_ITEM = "棍子";
    public static final String STONE_ITEM = "石头";
    public static final String ROPE_ITEM = "绳子";

    private static final int HAMMER_WEIGHT = 200;

    /**
     * help 中展示的 combine 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "combine - 在西楼合成锤子（须背包有棍子、石头、绳子）";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();
        Room room = player.getCurrentRoom();
        if (room == null || !UseCommand.WEST_BUILDING_ROOM_ID.equals(room.getRoomId())) {
            System.out.println("只能在博学西楼内合成锤子。");
            return false;
        }

        if (player.findItemInInventory(UseCommand.HAMMER_ITEM) != null) {
            System.out.println("你已经有一把锤子了。");
            return false;
        }

        if (!hasAllMaterials(player)) {
            System.out.println("材料不齐：需要棍子、石头和绳子。");
            return false;
        }

        removeMaterial(player, STICK_ITEM);
        removeMaterial(player, STONE_ITEM);
        removeMaterial(player, ROPE_ITEM);
        player.takeItem(new Item(UseCommand.HAMMER_ITEM, HAMMER_WEIGHT));
        System.out.println("你把棍子、石头和绳子绑在一起，做了一把锤子。");
        ActionTimeCost.deduct(game, ActionTimeCost.COMBINE);
        return false;
    }

    private boolean hasAllMaterials(Player player) {
        return player.findItemInInventory(STICK_ITEM) != null
            && player.findItemInInventory(STONE_ITEM) != null
            && player.findItemInInventory(ROPE_ITEM) != null;
    }

    private void removeMaterial(Player player, String itemName) {
        Item item = player.findItemInInventory(itemName);
        if (item != null) {
            player.removeItemFromInventory(item);
        }
    }

    @Override
    public String getCommandName() {
        return "combine";
    }
}

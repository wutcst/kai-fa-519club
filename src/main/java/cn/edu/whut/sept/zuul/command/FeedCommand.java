/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.npc.NpcService;

/**
 * 处理在北楼喂猫学长的命令类（E5）：消耗火腿肠，耗时 1 分钟，获得魔法饼干。
 */
public class FeedCommand implements CommandInterface {

    public static final String SAUSAGE_ITEM = "一根火腿肠";
    public static final String MAGIC_COOKIE_ITEM = "magic cookie";
    /** 猫学长现身后（第四关起）才注册 feed 命令 */
    public static final int MIN_FEED_LEVEL = 4;
    private static final int MAGIC_COOKIE_WEIGHT = 100;

    /**
     * help 中展示的 feed 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "feed - 在博学北楼用火腿肠喂猫学长，耗时 1 分钟，获得魔法饼干";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || !NpcService.NORTH_BUILDING_ROOM_ID.equals(room.getRoomId())) {
            System.out.println("请前往博学北楼再喂猫学长。");
            return false;
        }

        Player player = game.getPlayer();
        Item sausage = player.findItemInInventory(SAUSAGE_ITEM);
        if (sausage == null) {
            System.out.println("你没有火腿肠，无法喂猫学长。");
            return false;
        }

        player.removeItemFromInventory(sausage);
        player.takeItem(new Item(MAGIC_COOKIE_ITEM, MAGIC_COOKIE_WEIGHT));
        ActionTimeCost.deduct(game, ActionTimeCost.FEED);
        System.out.println("你把火腿肠递给猫学长，它满意地递给你一块魔法饼干。");
        return false;
    }

    @Override
    public String getCommandName() {
        return "feed";
    }
}

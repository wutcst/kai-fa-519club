/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理使用物品的命令类（E1/E16）：超市换卡、西楼砸锁、秒表与干扰物等。
 */
public class UseCommand implements CommandInterface {

    public static final String MONEY_ITEM = "湿漉漉的三十元钱";
    public static final String HAMMER_ITEM = "锤子";
    public static final String DORM_FORM_ITEM = "归寝单";
    public static final String STOPWATCH_ITEM = "一块遗弃的秒表";
    public static final String PROJECTOR_REMOTE_ITEM = "投影仪遥控器";
    public static final String PRAYER_PAPER_ITEM = "高数及格祈福黄纸";
    public static final String CHILI_PACKET_ITEM = "一个辣椒包";
    public static final String FORTUNE_SLIP_ITEM = "一张写了“吉”的抽签条";

    public static final String SUPERMARKET_ROOM_ID = "supermarket";
    public static final String NORTH_BUILDING_ROOM_ID = "boxue_north";
    public static final String WEST_BUILDING_ROOM_ID = "boxue_west";
    public static final String LIBRARY_ROOM_ID = "library";

    private static final int CAMPUS_CARD_WEIGHT = 5;

    public static String getUsageDescription() {
        return "use <物品> - 使用物品（超市用三十元换一卡通，西楼用锤子砸锁，"
            + "秒表查看剩余时间；部分干扰物 use 会有彩蛋）";
    }

    public static boolean isMoneyItem(String itemDescription) {
        if (itemDescription == null) {
            return false;
        }
        String trimmed = itemDescription.trim();
        return MONEY_ITEM.equalsIgnoreCase(trimmed) || "三十元钱".equalsIgnoreCase(trimmed);
    }

    @Override
    public boolean execute(Game game, String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Use what? 请指定要使用的物品。");
            return false;
        }

        Player player = game.getPlayer();
        String trimmedName = itemName.trim();
        Item item = player.findItemInInventory(trimmedName);
        if (item == null) {
            System.out.println("你没有 '" + trimmedName + "'。");
            return false;
        }

        if (tryUseGlobalDistraction(game, item)) {
            ActionTimeCost.deduct(game, ActionTimeCost.USE);
            return false;
        }

        if (STOPWATCH_ITEM.equalsIgnoreCase(trimmedName)) {
            ActionTimeCost.deduct(game, ActionTimeCost.USE);
            int seconds = game.getLevelTimer().getRemainingSeconds();
            int minutes = (seconds + 59) / 60;
            System.out.println("surprise！你的时间还剩 " + minutes + " 分钟");
            return false;
        }

        if (PROJECTOR_REMOTE_ITEM.equalsIgnoreCase(trimmedName)) {
            ActionTimeCost.deduct(game, ActionTimeCost.USE);
            System.out.println("你对着投影仪按了半天，电量又少了些，什么都没有发生。");
            return false;
        }

        Room room = player.getCurrentRoom();
        String roomId = room.getRoomId();
        boolean success;
        if (SUPERMARKET_ROOM_ID.equals(roomId)) {
            success = useInSupermarket(player, item);
        } else if (WEST_BUILDING_ROOM_ID.equals(roomId)) {
            success = useInWestBuilding(game, player, item);
        } else {
            System.out.println("在这里无法使用 '" + trimmedName + "'。");
            return false;
        }

        if (success) {
            ActionTimeCost.deduct(game, ActionTimeCost.USE);
        }
        return false;
    }

    private boolean tryUseGlobalDistraction(Game game, Item item) {
        String name = item.getDescription();
        if (PRAYER_PAPER_ITEM.equalsIgnoreCase(name)) {
            System.out.println("你在黄纸上写下 lim x→∞ hope = ?，"
                + "宿管阿姨路过说：极限不存在，别在这许愿了。");
            return true;
        }
        if (CHILI_PACKET_ITEM.equalsIgnoreCase(name) || "辣椒包".equalsIgnoreCase(name)) {
            ActionTimeCost.deduct(game, ActionTimeCost.MILK_TEA_DIARRHEA);
            System.out.println("你撕开辣椒包猛吸一口，辣得喷火，"
                + "多耗了 " + ActionTimeCost.MILK_TEA_DIARRHEA + " 秒缓劲。");
            return true;
        }
        if (FORTUNE_SLIP_ITEM.equalsIgnoreCase(name)) {
            System.out.println("签文写着「大吉」，可惜对智能锁和传送门都没有加成。");
            return true;
        }
        return false;
    }

    private boolean useInSupermarket(Player player, Item item) {
        if (!isMoneyItem(item.getDescription())) {
            System.out.println("在超市只能使用湿漉漉的三十元钱兑换一卡通。");
            return false;
        }

        if (player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM) != null) {
            System.out.println("你已经有一卡通了。");
            return false;
        }

        player.removeItemFromInventory(item);
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, CAMPUS_CARD_WEIGHT));
        System.out.println("宿管收下三十元，为你办好了一卡通，请妥善保管。");
        return true;
    }

    private boolean useInWestBuilding(Game game, Player player, Item item) {
        if (!HAMMER_ITEM.equalsIgnoreCase(item.getDescription())) {
            System.out.println("在这里只能使用锤子砸锁出门。");
            return false;
        }

        if (game.getLevelManager().getCurrentLevel() < 3) {
            System.out.println("这里的门锁暂时不需要砸开。");
            return false;
        }

        if (!game.getLevelManager().isWestBuildingExitLocked()) {
            System.out.println("门锁已经打开，不需要再用锤子了。");
            return false;
        }

        player.removeItemFromInventory(item);
        game.getLevelManager().unlockWestBuildingExit();
        System.out.println("你用锤子砸开了生锈的门锁，可以从东侧离开了。");
        return true;
    }

    @Override
    public String getCommandName() {
        return "use";
    }
}

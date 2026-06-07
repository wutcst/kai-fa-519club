/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理使用物品的命令类（E1）：超市换卡、西楼砸锁、图书馆盖章领归寝单。
 *
 * @author liujing
 * @version 1.0
 */
public class UseCommand implements CommandInterface {

    public static final String MONEY_ITEM = "三十元钱";
    public static final String REGISTRATION_SLIP_ITEM = "登记条";
    public static final String HAMMER_ITEM = "锤子";
    public static final String RECEIPT_ITEM = "回执";
    public static final String DORM_FORM_ITEM = "归寝单";

    public static final String SUPERMARKET_ROOM_ID = "supermarket";
    public static final String WEST_BUILDING_ROOM_ID = "boxue_west";
    public static final String LIBRARY_ROOM_ID = "library";

    private static final int CAMPUS_CARD_WEIGHT = 5;
    private static final int DORM_FORM_WEIGHT = 5;

    /**
     * help 中展示的 use 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "use <物品> - 使用物品（超市用三十元/登记条换一卡通，"
            + "西楼用锤子砸锁，图书馆用回执盖章领归寝单）";
    }

    @Override
    public boolean execute(Game game, String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Use what? 请指定要使用的物品。");
            return false;
        }

        Player player = game.getPlayer();
        Room room = player.getCurrentRoom();
        String trimmedName = itemName.trim();
        Item item = player.findItemInInventory(trimmedName);
        if (item == null) {
            System.out.println("你没有 '" + trimmedName + "'。");
            return false;
        }

        String roomId = room.getRoomId();
        boolean success;
        if (SUPERMARKET_ROOM_ID.equals(roomId)) {
            success = useInSupermarket(player, item);
        } else if (WEST_BUILDING_ROOM_ID.equals(roomId)) {
            success = useInWestBuilding(game, player, item);
        } else if (LIBRARY_ROOM_ID.equals(roomId)) {
            success = useInLibrary(player, item);
        } else {
            System.out.println("在这里无法使用 '" + trimmedName + "'。");
            return false;
        }

        if (success) {
            ActionTimeCost.deduct(game, ActionTimeCost.USE);
        }
        return false;
    }

    private boolean useInSupermarket(Player player, Item item) {
        String itemDescription = item.getDescription();
        if (!MONEY_ITEM.equalsIgnoreCase(itemDescription)
                && !REGISTRATION_SLIP_ITEM.equalsIgnoreCase(itemDescription)) {
            System.out.println("在超市只能使用三十元钱或登记条兑换一卡通。");
            return false;
        }

        if (player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM) != null) {
            System.out.println("你已经有一卡通了。");
            return false;
        }

        player.removeItemFromInventory(item);
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, CAMPUS_CARD_WEIGHT));
        System.out.println("宿管为你办好了一卡通，请妥善保管。");
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

    private boolean useInLibrary(Player player, Item item) {
        if (!RECEIPT_ITEM.equalsIgnoreCase(item.getDescription())) {
            System.out.println("在图书馆只能使用回执办理盖章。");
            return false;
        }

        if (player.findItemInInventory(DORM_FORM_ITEM) != null) {
            System.out.println("你已经领过归寝单了。");
            return false;
        }

        player.removeItemFromInventory(item);
        player.takeItem(new Item(DORM_FORM_ITEM, DORM_FORM_WEIGHT));
        System.out.println("工作人员在你的回执上盖了章，你领到了归寝单。");
        return true;
    }

    @Override
    public String getCommandName() {
        return "use";
    }
}

package cn.edu.whut.sept.zuul.npc;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.UseCommand;

/**
 * NPC 交互服务（E8）：按房间与关卡处理 talk 对话与归寝单发放。
 */
public final class NpcService {

    public static final String NORTH_BUILDING_ROOM_ID = UseCommand.NORTH_BUILDING_ROOM_ID;

    private static final int DORM_FORM_WEIGHT = 5;
    private static final int MIN_DORM_FORM_VOLUNTEER_LEVEL = 2;
    private static final int MAX_DORM_FORM_VOLUNTEER_LEVEL = 3;
    private static final int MIN_DORM_FORM_LIBRARY_LEVEL = 4;

    private NpcService() {
    }

    /**
     * 与当前房间 NPC 对话。
     *
     * @param game 游戏实例
     * @return 对话成功返回 true（含仅提示无物品发放）
     */
    public static boolean talk(Game game) {
        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || room.getRoomId() == null) {
            System.out.println("这里没有可以对话的人。");
            return false;
        }

        int level = game.getLevelManager().getCurrentLevel();
        switch (room.getRoomId()) {
            case UseCommand.SUPERMARKET_ROOM_ID:
                System.out.println("宿管阿姨：有钱就能办卡，交三十元 use 可兑换一卡通。");
                return true;
            case NORTH_BUILDING_ROOM_ID:
                return talkVolunteer(game, level);
            case UseCommand.LIBRARY_ROOM_ID:
                return talkLibrarian(game, level);
            default:
                System.out.println("这里没有可以对话的人。");
                return false;
        }
    }

    private static boolean talkVolunteer(Game game, int level) {
        if (level == 1) {
            System.out.println("志愿者：本关只需一卡通即可归寝，归寝单下周才查。");
            return true;
        }
        if (level >= MIN_DORM_FORM_VOLUNTEER_LEVEL && level <= MAX_DORM_FORM_VOLUNTEER_LEVEL) {
            System.out.println("志愿者：需要归寝单的话，登记一下就好。");
            return issueDormForm(game.getPlayer(), "志愿者为你登记并发放了归寝单。");
        }
        if (level >= MIN_DORM_FORM_LIBRARY_LEVEL) {
            System.out.println("志愿者：归寝单请前往图书馆，刷卡后找工作人员领取。");
            return true;
        }
        System.out.println("志愿者：本关暂无归寝单登记服务。");
        return true;
    }

    private static boolean talkLibrarian(Game game, int level) {
        if (level >= MIN_DORM_FORM_LIBRARY_LEVEL) {
            System.out.println("图书馆工作人员：请查看电子公告屏了解校史，"
                + "需要归寝单的话跟我说一声。");
            return issueDormForm(game.getPlayer(), "图书馆工作人员为你办理了归寝单。");
        }
        System.out.println("图书馆工作人员：本关暂无归寝单登记服务。");
        return true;
    }

    private static boolean issueDormForm(Player player, String successMessage) {
        if (player.findItemInInventory(UseCommand.DORM_FORM_ITEM) != null) {
            System.out.println("你已经领过归寝单了。");
            return true;
        }
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, DORM_FORM_WEIGHT));
        System.out.println(successMessage);
        return true;
    }
}

package cn.edu.whut.sept.zuul.npc;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.UseCommand;

/**
 * NPC 交互服务（E8）：按房间与关卡处理 talk 对话与 register 登记。
 */
public final class NpcService {

    public static final String NORTH_BUILDING_ROOM_ID = "boxue_north";

    private static final int CAMPUS_CARD_WEIGHT = 5;
    private static final int DORM_FORM_WEIGHT = 5;
    private static final int MIN_DORM_FORM_VOLUNTEER_LEVEL = 2;
    private static final int MAX_DORM_FORM_VOLUNTEER_LEVEL = 3;
    private static final int MIN_DORM_FORM_LIBRARY_LEVEL = 4;
    private static final int MAX_NPC_CARD_LEVEL = 4;

    private NpcService() {
    }

    /**
     * 与当前房间 NPC 对话。
     *
     * @param game 游戏实例
     * @return 对话成功返回 true
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
                System.out.println("宿管阿姨：需要一卡通请 register 登记，"
                    + "也可用三十元或登记条 use 兑换。");
                return true;
            case NORTH_BUILDING_ROOM_ID:
                return talkVolunteer(level);
            case UseCommand.LIBRARY_ROOM_ID:
                return talkLibrarian(level);
            default:
                System.out.println("这里没有可以对话的人。");
                return false;
        }
    }

    /**
     * 在当前房间 NPC 处办理登记业务。
     *
     * @param game 游戏实例
     * @return 登记成功返回 true
     */
    public static boolean register(Game game) {
        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || room.getRoomId() == null) {
            System.out.println("这里没有可以办理登记的地方。");
            return false;
        }

        int level = game.getLevelManager().getCurrentLevel();
        switch (room.getRoomId()) {
            case UseCommand.SUPERMARKET_ROOM_ID:
                return registerCampusCard(game.getPlayer(), level);
            case NORTH_BUILDING_ROOM_ID:
                return registerDormFormAtVolunteer(game.getPlayer(), level);
            case UseCommand.LIBRARY_ROOM_ID:
                return registerDormFormAtLibrary(game.getPlayer(), level);
            default:
                System.out.println("这里没有可以办理登记的地方。");
                return false;
        }
    }

    private static boolean talkVolunteer(int level) {
        if (level == 1) {
            System.out.println("志愿者：本关只需一卡通即可归寝。");
            return true;
        }
        if (level >= MIN_DORM_FORM_VOLUNTEER_LEVEL && level <= MAX_DORM_FORM_VOLUNTEER_LEVEL) {
            System.out.println("志愿者：需要归寝单请 register 登记领取。");
            return true;
        }
        if (level >= MIN_DORM_FORM_LIBRARY_LEVEL) {
            System.out.println("志愿者：归寝单请前往图书馆 register 领取。");
            return true;
        }
        System.out.println("志愿者：本关暂无归寝单登记服务。");
        return true;
    }

    private static boolean talkLibrarian(int level) {
        if (level >= MIN_DORM_FORM_LIBRARY_LEVEL) {
            System.out.println("图书馆工作人员：register 可领取归寝单，"
                + "有回执也可 use 盖章办理。");
            return true;
        }
        System.out.println("图书馆工作人员：本关暂无归寝单登记服务。");
        return true;
    }

    private static boolean registerCampusCard(Player player, int level) {
        if (level > MAX_NPC_CARD_LEVEL) {
            System.out.println("宿管阿姨：本关请直接前往寝室。");
            return false;
        }

        if (player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM) != null) {
            System.out.println("你已经有一卡通了。");
            return false;
        }

        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, CAMPUS_CARD_WEIGHT));
        System.out.println("宿管阿姨为你办好了一卡通，请妥善保管。");
        return true;
    }

    private static boolean registerDormFormAtVolunteer(Player player, int level) {
        if (level < MIN_DORM_FORM_VOLUNTEER_LEVEL || level > MAX_DORM_FORM_VOLUNTEER_LEVEL) {
            if (level >= MIN_DORM_FORM_LIBRARY_LEVEL) {
                System.out.println("志愿者：请到图书馆 register 领取归寝单。");
            } else {
                System.out.println("志愿者：本关暂无归寝单登记服务。");
            }
            return false;
        }

        return giveDormForm(player, "志愿者为你登记并发放了归寝单。");
    }

    private static boolean registerDormFormAtLibrary(Player player, int level) {
        if (level < MIN_DORM_FORM_LIBRARY_LEVEL) {
            System.out.println("图书馆工作人员：本关暂无归寝单登记服务。");
            return false;
        }

        return giveDormForm(player, "图书馆工作人员为你办理了归寝单。");
    }

    private static boolean giveDormForm(Player player, String successMessage) {
        if (player.findItemInInventory(UseCommand.DORM_FORM_ITEM) != null) {
            System.out.println("你已经领过归寝单了。");
            return false;
        }

        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, DORM_FORM_WEIGHT));
        System.out.println(successMessage);
        return true;
    }
}

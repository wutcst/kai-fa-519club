package cn.edu.whut.sept.zuul.unlock;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 密码解锁服务（E4）：体育馆 WHUT2026、第五关寝室 20000527。
 */
public final class UnlockService {

    public static final String GYM_ROOM_ID = "gymnasium";
    public static final String DORMITORY_ROOM_ID = "dormitory";
    public static final String GYM_PASSWORD = "WHUT2026";
    public static final String DORMITORY_PASSWORD = "20000527";
    public static final String DISTRACTION_DATE_PASSWORD = "2026.06.01";
    public static final String CANTEEN_NOTE_ITEM = "食堂纸条";

    private static final int MIN_GYM_UNLOCK_LEVEL = 3;
    private static final int DORMITORY_UNLOCK_LEVEL = 5;
    private static final int FLASHLIGHT_WEIGHT = 200;

    private UnlockService() {
    }

    /**
     * 尝试用密码解锁当前房间的锁。
     *
     * @param game 游戏实例
     * @param password 玩家输入的密码
     * @return 解锁成功返回 true
     */
    public static boolean unlock(Game game, String password) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Unlock what? 请输入密码。");
            return false;
        }

        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || room.getRoomId() == null) {
            System.out.println("这里无法输入密码。");
            return false;
        }

        String trimmedPassword = password.trim();
        String roomId = room.getRoomId();
        if (GYM_ROOM_ID.equals(roomId)) {
            return unlockGym(game, room, trimmedPassword);
        }
        if (DORMITORY_ROOM_ID.equals(roomId)) {
            return unlockDormitory(game, trimmedPassword);
        }

        System.out.println("这里无法输入密码。");
        return false;
    }

    private static boolean unlockGym(Game game, Room gym, String password) {
        int level = game.getLevelManager().getCurrentLevel();
        if (level < MIN_GYM_UNLOCK_LEVEL) {
            System.out.println("本关无需解锁体育馆器材室。");
            return false;
        }

        if (game.getLevelManager().isGymStorageUnlocked()) {
            System.out.println("器材室已经解锁。");
            return false;
        }

        if (GYM_PASSWORD.equals(password)) {
            game.getLevelManager().markGymStorageUnlocked();
            if (!gym.containsItem(DarkRoom.FLASHLIGHT_ITEM)) {
                gym.addItem(new Item(DarkRoom.FLASHLIGHT_ITEM, FLASHLIGHT_WEIGHT));
            }
            System.out.println("值班室密码正确，器材室门开了，失物招领处有一把手电筒。");
            return true;
        }

        handleWrongPassword(game, password);
        return false;
    }

    private static boolean unlockDormitory(Game game, String password) {
        if (game.getLevelManager().getCurrentLevel() != DORMITORY_UNLOCK_LEVEL) {
            System.out.println("本关无需解锁寝室智能锁。");
            return false;
        }

        if (game.getLevelManager().isDormitoryPasswordUnlocked()) {
            System.out.println("寝室智能锁已经打开。");
            return false;
        }

        if (DORMITORY_PASSWORD.equals(password)) {
            game.getLevelManager().markDormitoryPasswordUnlocked();
            System.out.println("密码正确，寝室智能锁已打开。");
            return true;
        }

        handleWrongPassword(game, password);
        return false;
    }

    private static void handleWrongPassword(Game game, String password) {
        if (DISTRACTION_DATE_PASSWORD.equals(password) || "20260601".equals(password)) {
            System.out.println("密码错误。食堂纸条上的日期并不是门锁密码。");
        } else {
            System.out.println("密码错误。");
        }
        ActionTimeCost.deduct(game, ActionTimeCost.WRONG_PASSWORD);
    }
}

package cn.edu.whut.sept.zuul;

/**
 * 黑暗区域房间（博学主楼断电区）：无手电筒不可进入，触发罚时并退回。
 */
public class DarkRoom extends Room {

    /** 准入所需物品名称 */
    public static final String FLASHLIGHT_ITEM = "手电筒";

    /** 无手电筒进入时的提示文案 */
    public static final String PENALTY_MESSAGE = "灯坏了，黑暗中摸索一分钟一无所获";

    public DarkRoom(String roomId, String description, String bulletin) {
        super(roomId, description, bulletin);
    }

    public boolean canEnter(Player player) {
        if (player == null) {
            return false;
        }
        return player.findItemInInventory(FLASHLIGHT_ITEM) != null;
    }
}

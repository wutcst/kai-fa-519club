package cn.edu.whut.sept.zuul;

/**
 * 黑暗区域房间（如博学主楼断电区）：无手电筒不可进入，触发罚时并退回。
 */
public class DarkRoom extends Room {

    /** 准入所需物品名称 */
    public static final String FLASHLIGHT_ITEM = "手电筒";

    /** 无手电筒进入时的提示文案 */
    public static final String PENALTY_MESSAGE = "灯坏了，黑暗中摸索一分钟一无所获";

    /**
     * 创建黑暗区域房间。
     *
     * @param description 房间描述
     */
    public DarkRoom(String description) {
        super(description);
    }

    /**
     * 判断玩家是否满足进入条件（携带手电筒）。
     *
     * @param player 玩家实例
     * @return 有手电筒返回 true
     */
    public boolean canEnter(Player player) {
        if (player == null) {
            return false;
        }
        return player.findItemInInventory(FLASHLIGHT_ITEM) != null;
    }
}

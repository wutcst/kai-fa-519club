/**
 * 具有传输功能的特殊房间类，玩家进入后会被随机传送到其他房间。
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 传输房间（F4 / E17）：进入后可随机传送到目标列表中的房间。
 */
public class TeleportRoom extends Room {

    private List<Room> targetRooms;
    private Random random;
    private boolean teleportEnabled;

    /**
     * 测试或临时房间用构造（无 ID，默认启用传送）。
     *
     * @param description 房间描述
     * @param targetRooms 可能被传送到的目标房间列表
     */
    public TeleportRoom(String description, List<Room> targetRooms) {
        super(description);
        this.targetRooms = targetRooms;
        this.random = new Random();
        this.teleportEnabled = true;
    }

    /**
     * 地图房间用构造：默认关闭传送，由关卡配置按关启用（E17）。
     *
     * @param roomId 房间标识
     * @param description 房间描述
     * @param bulletin 进入公告
     * @param targetRooms 传送目标列表
     */
    public TeleportRoom(String roomId, String description, String bulletin, List<Room> targetRooms) {
        super(roomId, description, bulletin);
        this.targetRooms = targetRooms != null ? new ArrayList<>(targetRooms) : new ArrayList<>();
        this.random = new Random();
        this.teleportEnabled = false;
    }

    /**
     * 是否启用进入后随机传送。
     *
     * @return 启用返回 true
     */
    public boolean isTeleportEnabled() {
        return teleportEnabled;
    }

    /**
     * 设置是否启用随机传送（E17 仅第五关开启）。
     *
     * @param teleportEnabled 是否启用
     */
    public void setTeleportEnabled(boolean teleportEnabled) {
        this.teleportEnabled = teleportEnabled;
    }

    /**
     * 更新传送目标房间列表。
     *
     * @param targetRooms 目标房间列表
     */
    public void setTargetRooms(List<Room> targetRooms) {
        this.targetRooms = targetRooms != null ? new ArrayList<>(targetRooms) : new ArrayList<>();
    }

    /**
     * 随机选择一个目标房间进行传输。
     *
     * @return 随机选择的目标房间；无目标时返回自身
     */
    public Room teleport() {
        if (targetRooms == null || targetRooms.isEmpty()) {
            return this;
        }
        int index = random.nextInt(targetRooms.size());
        return targetRooms.get(index);
    }
}

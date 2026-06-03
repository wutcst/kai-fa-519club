/**
 * 具有传输功能的特殊房间类，玩家进入后会被随机传送到其他房间。
 * 新增：实现随机传输功能，扩展基础Room类。
 *
 * @author liujing
 * @version 1.4
 */
package cn.edu.whut.sept.zuul;

import java.util.List;
import java.util.Random;

public class TeleportRoom extends Room {
    private List<Room> targetRooms; // 可能被传送到的目标房间列表
    private Random random; // 随机数生成器

    /**
     * 初始化传输房间实例
     *
     * @param description 房间的文本描述
     * @param targetRooms 可能被传送到的目标房间列表
     */
    public TeleportRoom(String description, List<Room> targetRooms) {
        super(description);
        this.targetRooms = targetRooms;
        this.random = new Random();
    }

    /**
     * 随机选择一个目标房间进行传输
     *
     * @return 随机选择的目标房间
     */
    public Room teleport() {
        if (targetRooms == null || targetRooms.isEmpty()) {
            return this; // 如果没有目标房间，不进行传输
        }
        int index = random.nextInt(targetRooms.size());
        return targetRooms.get(index);
    }
}
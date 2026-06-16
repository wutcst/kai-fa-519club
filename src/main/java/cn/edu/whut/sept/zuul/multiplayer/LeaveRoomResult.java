package cn.edu.whut.sept.zuul.multiplayer;

/**
 * 离开联机房间的结果。
 */
public class LeaveRoomResult {

    private final String roomId;
    private final boolean roomRemoved;

    public LeaveRoomResult(String roomId, boolean roomRemoved) {
        this.roomId = roomId;
        this.roomRemoved = roomRemoved;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isRoomRemoved() {
        return roomRemoved;
    }
}

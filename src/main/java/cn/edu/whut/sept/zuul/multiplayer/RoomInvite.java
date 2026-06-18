package cn.edu.whut.sept.zuul.multiplayer;

/**
 * 房间邀请（内存）。
 */
public class RoomInvite {

    private final String roomId;
    private final String roomName;
    private final long fromUserId;
    private final String fromDisplayName;
    private final long createdAtMs;

    public RoomInvite(String roomId, String roomName, long fromUserId,
                      String fromDisplayName, long createdAtMs) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.fromUserId = fromUserId;
        this.fromDisplayName = fromDisplayName;
        this.createdAtMs = createdAtMs;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public long getFromUserId() {
        return fromUserId;
    }

    public String getFromDisplayName() {
        return fromDisplayName;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }
}

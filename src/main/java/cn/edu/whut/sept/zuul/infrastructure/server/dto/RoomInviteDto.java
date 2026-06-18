package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 房间邀请。
 */
public class RoomInviteDto {

    private String roomId;
    private String roomName;
    private long fromUserId;
    private String fromDisplayName;
    private long createdAtMs;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromDisplayName() {
        return fromDisplayName;
    }

    public void setFromDisplayName(String fromDisplayName) {
        this.fromDisplayName = fromDisplayName;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }

    public void setCreatedAtMs(long createdAtMs) {
        this.createdAtMs = createdAtMs;
    }
}

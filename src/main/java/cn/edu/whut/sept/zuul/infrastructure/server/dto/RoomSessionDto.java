package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 创建/加入房间响应。
 */
public class RoomSessionDto {

    private String roomId;
    private String roomName;
    private String playerId;
    private String displayName;
    private GameStateDto state;

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

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public GameStateDto getState() {
        return state;
    }

    public void setState(GameStateDto state) {
        this.state = state;
    }
}

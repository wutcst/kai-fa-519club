package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 离开房间请求体。
 */
public class LeaveRoomRequest {

    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}

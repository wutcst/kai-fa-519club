package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 离开房间请求体。
 */
public class LeaveRoomRequest {

    private String playerId;
    private String action;
    private String newHostPlayerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNewHostPlayerId() {
        return newHostPlayerId;
    }

    public void setNewHostPlayerId(String newHostPlayerId) {
        this.newHostPlayerId = newHostPlayerId;
    }
}

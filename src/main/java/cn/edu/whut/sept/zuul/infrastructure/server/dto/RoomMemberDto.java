package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 组队房间成员。
 */
public class RoomMemberDto {

    private String playerId;
    private long userId;
    private String displayName;
    private boolean host;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }
}

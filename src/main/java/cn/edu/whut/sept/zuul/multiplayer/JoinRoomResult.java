package cn.edu.whut.sept.zuul.multiplayer;

/**
 * 加入联机房间的结果。
 */
public class JoinRoomResult {

    private final GameRoom room;
    private final String playerId;
    private final String displayName;

    public JoinRoomResult(GameRoom room, String playerId, String displayName) {
        this.room = room;
        this.playerId = playerId;
        this.displayName = displayName;
    }

    public GameRoom getRoom() {
        return room;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }
}

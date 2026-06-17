package cn.edu.whut.sept.zuul.multiplayer;

/**
 * 联机房间聊天消息。
 */
public class RoomChatMessage {

    private final long id;
    private final String playerId;
    private final String displayName;
    private final String text;
    private final long timestampMs;

    public RoomChatMessage(long id, String playerId, String displayName, String text, long timestampMs) {
        this.id = id;
        this.playerId = playerId;
        this.displayName = displayName;
        this.text = text;
        this.timestampMs = timestampMs;
    }

    public long getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getText() {
        return text;
    }

    public long getTimestampMs() {
        return timestampMs;
    }
}

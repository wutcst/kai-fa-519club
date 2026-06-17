package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import cn.edu.whut.sept.zuul.multiplayer.RoomChatMessage;

/**
 * 联机房间聊天消息 DTO。
 */
public class RoomChatMessageDto {

    private long id;
    private String playerId;
    private String displayName;
    private String text;
    private long timestampMs;

    public static RoomChatMessageDto from(RoomChatMessage message) {
        RoomChatMessageDto dto = new RoomChatMessageDto();
        dto.id = message.getId();
        dto.playerId = message.getPlayerId();
        dto.displayName = message.getDisplayName();
        dto.text = message.getText();
        dto.timestampMs = message.getTimestampMs();
        return dto;
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

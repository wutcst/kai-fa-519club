package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 发送联机房间聊天消息请求。
 */
public class SendChatRequest {

    private String playerId;
    private String text;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

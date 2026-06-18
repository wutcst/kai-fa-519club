package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 创建单机会话请求。
 */
public class CreateSoloSessionRequest {

    private String playerName;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

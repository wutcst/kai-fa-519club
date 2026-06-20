package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 创建单机会话请求。
 */
public class CreateSoloSessionRequest {

    private String playerName;
    private Integer levelNumber;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }
}

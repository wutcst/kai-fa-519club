package cn.edu.whut.sept.zuul.infrastructure.social;

/**
 * 好友在线状态。
 */
public enum UserPresenceStatus {
    OFFLINE("离线"),
    ONLINE("在线"),
    SOLO_PLAYING("单机游戏中"),
    IN_ROOM("房间中"),
    MULTIPLAYER_PLAYING("联机游戏中");

    private final String label;

    UserPresenceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

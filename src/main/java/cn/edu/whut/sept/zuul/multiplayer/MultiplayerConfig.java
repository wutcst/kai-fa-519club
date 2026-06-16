package cn.edu.whut.sept.zuul.multiplayer;

/**
 * 联机模式全局配置。
 */
public final class MultiplayerConfig {

    /** 单房间最大玩家数 */
    public static final int MAX_PLAYERS_PER_ROOM = 4;

    /** 默认服务端地址 */
    public static final String DEFAULT_SERVER_URL = "http://localhost:8080";

    /** 客户端状态轮询间隔（毫秒） */
    public static final long STATE_POLL_INTERVAL_MS = 1000L;

    private MultiplayerConfig() {
    }
}

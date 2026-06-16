package cn.edu.whut.sept.zuul.multiplayer;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.whut.sept.zuul.Game;

/**
 * 联机房间：持有权威 {@link Game} 实例与玩家元数据。
 */
public class GameRoom {

    private final String roomId;
    private final String roomName;
    private final Game game;
    private final String hostPlayerId;
    private final Instant createdAt;
    private final Map<String, String> playerDisplayNames = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public GameRoom(String roomId, String roomName, Game game, String hostPlayerId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.game = game;
        this.hostPlayerId = hostPlayerId;
        this.createdAt = Instant.now();
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Game getGame() {
        return game;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getPlayerDisplayNames() {
        return playerDisplayNames;
    }

    public Object getLock() {
        return lock;
    }

    public int getPlayerCount() {
        return playerDisplayNames.size();
    }

    public boolean hasPlayer(String playerId) {
        return playerDisplayNames.containsKey(playerId);
    }
}

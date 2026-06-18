package cn.edu.whut.sept.zuul.multiplayer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private String hostPlayerId;
    private long hostUserId;
    private final Instant createdAt;
    private final Map<String, String> playerDisplayNames = new ConcurrentHashMap<>();
    private final Map<String, Long> playerUserIds = new ConcurrentHashMap<>();
    private final List<RoomChatMessage> chatMessages = Collections.synchronizedList(new ArrayList<>());
    private final Object lock = new Object();
    private long chatSequence;
    private volatile boolean inGame;

    private static final int MAX_CHAT_MESSAGES = 80;
    private static final int MAX_CHAT_TEXT_LENGTH = 200;

    public GameRoom(String roomId, String roomName, Game game, String hostPlayerId, long hostUserId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.game = game;
        this.hostPlayerId = hostPlayerId;
        this.hostUserId = hostUserId;
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

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }

    public long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(long hostUserId) {
        this.hostUserId = hostUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getPlayerDisplayNames() {
        return playerDisplayNames;
    }

    public Map<String, Long> getPlayerUserIds() {
        return playerUserIds;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
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

    public Long getUserIdForPlayer(String playerId) {
        return playerUserIds.get(playerId);
    }

    public RoomChatMessage addChatMessage(String playerId, String displayName, String text) {
        if (text == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        if (trimmed.length() > MAX_CHAT_TEXT_LENGTH) {
            trimmed = trimmed.substring(0, MAX_CHAT_TEXT_LENGTH);
        }
        RoomChatMessage message = new RoomChatMessage(
            ++chatSequence,
            playerId,
            displayName == null ? "玩家" : displayName.trim(),
            trimmed,
            System.currentTimeMillis());
        chatMessages.add(message);
        while (chatMessages.size() > MAX_CHAT_MESSAGES) {
            chatMessages.remove(0);
        }
        return message;
    }

    public List<RoomChatMessage> getChatMessages() {
        synchronized (chatMessages) {
            return new ArrayList<>(chatMessages);
        }
    }
}

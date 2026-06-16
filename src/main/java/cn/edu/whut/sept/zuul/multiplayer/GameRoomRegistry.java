package cn.edu.whut.sept.zuul.multiplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.whut.sept.zuul.Game;

/**
 * 内存联机房间注册表（服务端权威）。
 */
public class GameRoomRegistry {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom createRoom(String roomName, String hostDisplayName) {
        String normalizedName = roomName == null || roomName.trim().isEmpty()
            ? "联机房间" : roomName.trim();
        String hostName = hostDisplayName == null || hostDisplayName.trim().isEmpty()
            ? "房主" : hostDisplayName.trim();
        Game game = Game.createMultiplayerHostGame();
        String hostPlayerId = game.addOnlinePlayer(hostName);
        String roomId = UUID.randomUUID().toString();
        GameRoom room = new GameRoom(roomId, normalizedName, game, hostPlayerId);
        room.getPlayerDisplayNames().put(hostPlayerId, hostName);
        rooms.put(roomId, room);
        return room;
    }

    public GameRoom findRoom(String roomId) {
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    public JoinRoomResult joinRoom(String roomId, String displayName) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return null;
        }
        String playerName = displayName == null || displayName.trim().isEmpty()
            ? "玩家" + (room.getPlayerCount() + 1) : displayName.trim();
        synchronized (room.getLock()) {
            if (room.getPlayerCount() >= MultiplayerConfig.MAX_PLAYERS_PER_ROOM) {
                throw new IllegalStateException(
                    "房间已满（最多 " + MultiplayerConfig.MAX_PLAYERS_PER_ROOM + " 人）");
            }
            String playerId = room.getGame().addOnlinePlayer(playerName);
            room.getPlayerDisplayNames().put(playerId, playerName);
            return new JoinRoomResult(room, playerId, playerName);
        }
    }

    public LeaveRoomResult leaveRoom(String roomId, String playerId) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return null;
        }
        synchronized (room.getLock()) {
            if (!room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            room.getGame().removeOnlinePlayer(playerId);
            room.getPlayerDisplayNames().remove(playerId);
            boolean roomRemoved = false;
            if (room.getPlayerCount() == 0) {
                removeRoom(roomId);
                roomRemoved = true;
            }
            return new LeaveRoomResult(roomId, roomRemoved);
        }
    }

    public void removeRoom(String roomId) {
        GameRoom room = rooms.remove(roomId);
        if (room != null) {
            room.getGame().getLevelTimer().shutdown();
        }
    }

    public List<GameRoom> listRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * 测试专用：清空所有房间。
     */
    public void clearAllForTest() {
        for (GameRoom room : rooms.values()) {
            room.getGame().getLevelTimer().shutdown();
        }
        rooms.clear();
    }
}

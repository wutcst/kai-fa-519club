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
    private final Map<Long, String> userRoomIndex = new ConcurrentHashMap<>();

    public GameRoom createRoom(String roomName, String hostDisplayName, long hostUserId) {
        String normalizedName = roomName == null || roomName.trim().isEmpty()
            ? "联机房间" : roomName.trim();
        String hostName = hostDisplayName == null || hostDisplayName.trim().isEmpty()
            ? "房主" : hostDisplayName.trim();
        Game game = Game.createMultiplayerHostGame();
        String hostPlayerId = game.addOnlinePlayer(hostName);
        String roomId = UUID.randomUUID().toString();
        GameRoom room = new GameRoom(roomId, normalizedName, game, hostPlayerId, hostUserId);
        room.getPlayerDisplayNames().put(hostPlayerId, hostName);
        room.getPlayerUserIds().put(hostPlayerId, hostUserId);
        rooms.put(roomId, room);
        userRoomIndex.put(hostUserId, roomId);
        return room;
    }

    public GameRoom findRoom(String roomId) {
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    public GameRoom findRoomByUserId(long userId) {
        String roomId = userRoomIndex.get(userId);
        return roomId == null ? null : rooms.get(roomId);
    }

    public JoinRoomResult joinRoom(String roomId, String displayName, long userId) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return null;
        }
        String playerName = displayName == null || displayName.trim().isEmpty()
            ? "玩家" + (room.getPlayerCount() + 1) : displayName.trim();
        synchronized (room.getLock()) {
            String existingRoomId = userRoomIndex.get(userId);
            if (existingRoomId != null && !existingRoomId.equals(roomId)) {
                throw new IllegalStateException("你已在其他房间中，请先离开");
            }
            for (Map.Entry<String, Long> entry : room.getPlayerUserIds().entrySet()) {
                if (entry.getValue() == userId) {
                    return new JoinRoomResult(
                        room, entry.getKey(), room.getPlayerDisplayNames().get(entry.getKey()));
                }
            }
            if (room.isInGame()) {
                throw new IllegalStateException("房间游戏中，请等待本局结束");
            }
            if (room.getPlayerCount() >= MultiplayerConfig.MAX_PLAYERS_PER_ROOM) {
                throw new IllegalStateException(
                    "房间已满（最多 " + MultiplayerConfig.MAX_PLAYERS_PER_ROOM + " 人）");
            }
            String playerId = room.getGame().addOnlinePlayer(playerName);
            room.getPlayerDisplayNames().put(playerId, playerName);
            room.getPlayerUserIds().put(playerId, userId);
            userRoomIndex.put(userId, roomId);
            return new JoinRoomResult(room, playerId, playerName);
        }
    }

    public LeaveRoomResult leaveRoom(String roomId, String playerId, LeaveRoomAction action,
                                     String newHostPlayerId) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return null;
        }
        synchronized (room.getLock()) {
            if (!room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            boolean isHost = playerId.equals(room.getHostPlayerId());
            Long userId = room.getPlayerUserIds().get(playerId);

            if (isHost && action == LeaveRoomAction.DISSOLVE) {
                return dissolveRoomInternal(room);
            }

            if (isHost && action == LeaveRoomAction.TRANSFER_HOST) {
                if (newHostPlayerId == null || !room.hasPlayer(newHostPlayerId)) {
                    throw new IllegalArgumentException("请选择有效的新房主");
                }
                if (newHostPlayerId.equals(playerId)) {
                    throw new IllegalArgumentException("不能将房主转让给自己");
                }
                room.setHostPlayerId(newHostPlayerId);
                Long newHostUserId = room.getPlayerUserIds().get(newHostPlayerId);
                if (newHostUserId != null) {
                    room.setHostUserId(newHostUserId);
                }
            } else if (isHost && room.getPlayerCount() > 1 && action == LeaveRoomAction.LEAVE) {
                throw new IllegalArgumentException("房主离开前请选择解散房间或转移房主");
            }

            removePlayerFromRoom(room, playerId, userId);
            boolean roomRemoved = false;
            if (room.getPlayerCount() == 0) {
                removeRoom(roomId);
                roomRemoved = true;
            }
            return new LeaveRoomResult(roomId, roomRemoved);
        }
    }

    public void abandonLobbyByHost(String roomId, long hostUserId) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return;
        }
        synchronized (room.getLock()) {
            if (room.getHostUserId() != hostUserId) {
                return;
            }
            dissolveRoomInternal(room);
        }
    }

    public void dissolveRoom(String roomId) {
        GameRoom room = findRoom(roomId);
        if (room == null) {
            return;
        }
        synchronized (room.getLock()) {
            dissolveRoomInternal(room);
        }
    }

    private LeaveRoomResult dissolveRoomInternal(GameRoom room) {
        String roomId = room.getRoomId();
        for (Long userId : new ArrayList<>(room.getPlayerUserIds().values())) {
            userRoomIndex.remove(userId);
        }
        removeRoom(roomId);
        return new LeaveRoomResult(roomId, true);
    }

    private void removePlayerFromRoom(GameRoom room, String playerId, Long userId) {
        room.getGame().removeOnlinePlayer(playerId);
        room.getPlayerDisplayNames().remove(playerId);
        room.getPlayerUserIds().remove(playerId);
        if (userId != null) {
            String indexedRoom = userRoomIndex.get(userId);
            if (room.getRoomId().equals(indexedRoom)) {
                userRoomIndex.remove(userId);
            }
        }
    }

    public void removeRoom(String roomId) {
        GameRoom room = rooms.remove(roomId);
        if (room != null) {
            for (Long userId : room.getPlayerUserIds().values()) {
                userRoomIndex.remove(userId);
            }
            room.getGame().getLevelTimer().shutdown();
        }
    }

    public List<GameRoom> listRooms() {
        return new ArrayList<>(rooms.values());
    }

    public void clearAllForTest() {
        for (GameRoom room : rooms.values()) {
            room.getGame().getLevelTimer().shutdown();
        }
        rooms.clear();
        userRoomIndex.clear();
    }
}

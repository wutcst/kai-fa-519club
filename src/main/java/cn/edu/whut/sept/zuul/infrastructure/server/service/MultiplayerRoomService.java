package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.CommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.GameStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomChatMessageDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInfoDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInviteDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomMemberDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomSessionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.TeamRoomDto;
import cn.edu.whut.sept.zuul.infrastructure.social.UserPresenceRegistry;
import cn.edu.whut.sept.zuul.infrastructure.social.UserPresenceStatus;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.multiplayer.GameCommandResult;
import cn.edu.whut.sept.zuul.multiplayer.GameEngineFacade;
import cn.edu.whut.sept.zuul.multiplayer.GameRoom;
import cn.edu.whut.sept.zuul.multiplayer.GameRoomRegistry;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.JoinRoomResult;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomAction;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomResult;
import cn.edu.whut.sept.zuul.multiplayer.RoomChatMessage;
import cn.edu.whut.sept.zuul.multiplayer.RoomInvite;
import cn.edu.whut.sept.zuul.multiplayer.RoomInviteRegistry;

/**
 * 联机房间与命令业务服务。
 */
@Service
public class MultiplayerRoomService {

    private final GameRoomRegistry roomRegistry = new GameRoomRegistry();
    private final GameEngineFacade gameEngineFacade = new GameEngineFacade();
    private final UserPresenceRegistry presenceRegistry;
    private final RoomInviteRegistry inviteRegistry;
    private final FriendService friendService;
    private final SoloProgressService soloProgressService;

    public MultiplayerRoomService(UserPresenceRegistry presenceRegistry,
                                  RoomInviteRegistry inviteRegistry,
                                  FriendService friendService,
                                  SoloProgressService soloProgressService) {
        this.presenceRegistry = presenceRegistry;
        this.inviteRegistry = inviteRegistry;
        this.friendService = friendService;
        this.soloProgressService = soloProgressService;
    }

    public RoomSessionDto createRoom(String roomName, String hostName, long hostUserId) {
        GameRoom existing = roomRegistry.findRoomByUserId(hostUserId);
        if (existing != null) {
            throw new IllegalStateException("你已在房间「" + existing.getRoomName() + "」中");
        }
        GameRoom room = roomRegistry.createRoom(roomName, hostName, hostUserId);
        syncRoomPresence(room);
        return toSessionDto(room, room.getHostPlayerId(), hostUserId);
    }

    public RoomSessionDto joinRoom(String roomId, String displayName, long userId) {
        JoinRoomResult result = roomRegistry.joinRoom(roomId, displayName, userId);
        if (result == null) {
            return null;
        }
        GameRoom room = result.getRoom();
        syncRoomPresence(room);
        inviteRegistry.clearInvitesForUser(userId);
        return toSessionDto(room, result.getPlayerId(), userId);
    }

    public TeamRoomDto getMyTeamRoom(long userId) {
        GameRoom room = roomRegistry.findRoomByUserId(userId);
        if (room == null) {
            return null;
        }
        String playerId = findPlayerIdForUser(room, userId);
        if (playerId == null) {
            return null;
        }
        return toTeamRoomDto(room, playerId, userId);
    }

    public List<RoomInfoDto> listRooms() {
        return roomRegistry.listRooms().stream()
            .map(RoomInfoDto::from)
            .collect(Collectors.toList());
    }

    public List<RoomInviteDto> listInvites(long userId) {
        GameRoom room = roomRegistry.findRoomByUserId(userId);
        if (room != null && room.getHostUserId() != userId) {
            inviteRegistry.clearInvitesForUser(userId);
            return List.of();
        }
        return inviteRegistry.listInvites(userId).stream()
            .map(this::toInviteDto)
            .collect(Collectors.toList());
    }

    public void inviteFriend(String roomId, long fromUserId, String fromDisplayName, long friendUserId) {
        GameRoom room = requireRoom(roomId);
        synchronized (room.getLock()) {
            if (!room.getPlayerUserIds().containsValue(fromUserId)) {
                throw new IllegalArgumentException("你不在该房间中");
            }
            if (room.isInGame()) {
                throw new IllegalArgumentException("游戏进行中无法邀请好友");
            }
            if (!friendService.listFriends(fromUserId).stream()
                    .anyMatch(friend -> friend.getUserId() == friendUserId)) {
                throw new IllegalArgumentException("只能邀请好友加入房间");
            }
            if (room.getPlayerUserIds().containsValue(friendUserId)) {
                throw new IllegalArgumentException("该好友已在队伍中");
            }
            GameRoom friendRoom = roomRegistry.findRoomByUserId(friendUserId);
            if (friendRoom != null && friendRoom.getHostUserId() != friendUserId) {
                throw new IllegalArgumentException("该好友已在其他房间中");
            }
            UserPresenceStatus status = presenceRegistry.resolveStatus(friendUserId);
            if (status != UserPresenceStatus.ONLINE && status != UserPresenceStatus.SOLO_PLAYING) {
                throw new IllegalArgumentException("只能邀请在线或单机游戏中的好友");
            }
            RoomInvite invite = new RoomInvite(
                room.getRoomId(),
                room.getRoomName(),
                fromUserId,
                fromDisplayName,
                System.currentTimeMillis());
            inviteRegistry.sendInvite(friendUserId, invite);
        }
    }

    public void rejectInvite(long userId, String roomId) {
        if (inviteRegistry.consumeInvite(userId, roomId) == null) {
            throw new IllegalArgumentException("邀请不存在或已失效");
        }
    }

    public void startGame(String roomId, long userId, int levelNumber) {
        GameRoom room = requireRoom(roomId);
        synchronized (room.getLock()) {
            if (room.getHostUserId() != userId) {
                throw new IllegalArgumentException("仅房主可以开始游戏");
            }
            soloProgressService.assertLevelUnlocked(userId, levelNumber);
            int highestUnlocked = soloProgressService.getHighestUnlockedForUser(userId);
            room.getGame().getLevelManager().setHighestUnlockedLevel(highestUnlocked);
            if (levelNumber != room.getGame().getLevelManager().getCurrentLevel()) {
                room.getGame().getLevelManager().startLevel(levelNumber);
            }
            room.setInGame(true);
            syncRoomPresence(room);
        }
    }

    public void endRound(String roomId) {
        GameRoom room = requireRoom(roomId);
        synchronized (room.getLock()) {
            room.setInGame(false);
            room.getGame().getLevelManager().startLevel(LevelConfig.MIN_LEVEL);
            syncRoomPresence(room);
        }
    }

    public void abandonLobby(String roomId, long userId) {
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            return;
        }
        List<Long> memberIds = new ArrayList<>(room.getPlayerUserIds().values());
        roomRegistry.abandonLobbyByHost(roomId, userId);
        inviteRegistry.clearInvitesForRoom(roomId);
        for (Long memberId : memberIds) {
            presenceRegistry.update(memberId, UserPresenceStatus.ONLINE, null);
        }
    }

    /**
     * 用户登出时清理联机房间：房主解散房间，队员仅离开房间。
     */
    public void handleUserLogout(long userId) {
        GameRoom room = roomRegistry.findRoomByUserId(userId);
        if (room == null) {
            presenceRegistry.markOffline(userId);
            return;
        }
        String roomId = room.getRoomId();
        if (room.getHostUserId() == userId) {
            List<Long> memberIds = new ArrayList<>(room.getPlayerUserIds().values());
            roomRegistry.dissolveRoom(roomId);
            inviteRegistry.clearInvitesForRoom(roomId);
            for (Long memberId : memberIds) {
                if (memberId == userId) {
                    presenceRegistry.markOffline(memberId);
                } else {
                    presenceRegistry.update(memberId, UserPresenceStatus.ONLINE, null);
                }
            }
            return;
        }
        String playerId = findPlayerIdForUser(room, userId);
        if (playerId != null) {
            leaveRoom(roomId, playerId, LeaveRoomAction.LEAVE, null, userId);
        }
        presenceRegistry.markOffline(userId);
    }

    public GameStateDto getState(String roomId, String playerId) {
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            return null;
        }
        synchronized (room.getLock()) {
            return buildState(room, playerId);
        }
    }

    public CommandResponseDto executeCommand(String roomId, String playerId,
                                             String commandWord, String secondWord) {
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            return null;
        }
        synchronized (room.getLock()) {
            GameCommandResult result = gameEngineFacade.executeCommand(
                room, playerId, commandWord, secondWord);
            CommandResponseDto dto = CommandResponseDto.from(result);
            dto.setState(buildState(room, playerId));
            dto.setNoticeMessage(MultiplayerViewMapper.buildNoticeMessage(
                commandWord, result.getMessages()));
            return dto;
        }
    }

    public RoomChatMessageDto sendChat(String roomId, String playerId, String text) {
        GameRoom room = requireRoom(roomId);
        synchronized (room.getLock()) {
            if (!room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            String displayName = room.getPlayerDisplayNames().get(playerId);
            RoomChatMessage message = room.addChatMessage(playerId, displayName, text);
            return RoomChatMessageDto.from(message);
        }
    }

    public LeaveRoomResult leaveRoom(String roomId, String playerId, LeaveRoomAction action,
                                     String newHostPlayerId, Long userId) {
        LeaveRoomResult result = roomRegistry.leaveRoom(roomId, playerId, action, newHostPlayerId);
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room != null) {
            syncRoomPresence(room);
        }
        if (userId != null) {
            presenceRegistry.update(userId, UserPresenceStatus.ONLINE, null);
        }
        if (result != null && result.isRoomRemoved()) {
            inviteRegistry.clearInvitesForRoom(roomId);
        }
        return result;
    }

    public GameRoom findRoom(String roomId) {
        return roomRegistry.findRoom(roomId);
    }

    public void updatePresence(long userId, UserPresenceStatus status, String roomId) {
        if (status == UserPresenceStatus.OFFLINE) {
            presenceRegistry.markOffline(userId);
            return;
        }
        presenceRegistry.update(userId, status, roomId);
    }

    public void clearAllRoomsForTest() {
        roomRegistry.clearAllForTest();
        inviteRegistry.clearForTest();
    }

    private GameRoom requireRoom(String roomId) {
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        return room;
    }

    private void syncRoomPresence(GameRoom room) {
        UserPresenceStatus status = room.isInGame()
            ? UserPresenceStatus.MULTIPLAYER_PLAYING
            : UserPresenceStatus.IN_ROOM;
        for (Map.Entry<String, Long> entry : room.getPlayerUserIds().entrySet()) {
            presenceRegistry.update(entry.getValue(), status, room.getRoomId());
        }
    }

    private String findPlayerIdForUser(GameRoom room, long userId) {
        for (Map.Entry<String, Long> entry : room.getPlayerUserIds().entrySet()) {
            if (entry.getValue() == userId) {
                return entry.getKey();
            }
        }
        return null;
    }

    private RoomSessionDto toSessionDto(GameRoom room, String playerId, long userId) {
        RoomSessionDto session = new RoomSessionDto();
        session.setRoomId(room.getRoomId());
        session.setRoomName(room.getRoomName());
        session.setPlayerId(playerId);
        session.setDisplayName(room.getPlayerDisplayNames().get(playerId));
        session.setHost(playerId.equals(room.getHostPlayerId()));
        session.setInGame(room.isInGame());
        session.setState(buildState(room, playerId));
        return session;
    }

    private TeamRoomDto toTeamRoomDto(GameRoom room, String playerId, long userId) {
        TeamRoomDto dto = new TeamRoomDto();
        dto.setRoomId(room.getRoomId());
        dto.setRoomName(room.getRoomName());
        dto.setPlayerId(playerId);
        dto.setHostPlayerId(room.getHostPlayerId());
        dto.setHost(playerId.equals(room.getHostPlayerId()));
        dto.setInGame(room.isInGame());
        List<RoomMemberDto> members = new ArrayList<>();
        for (Map.Entry<String, String> entry : room.getPlayerDisplayNames().entrySet()) {
            RoomMemberDto member = new RoomMemberDto();
            member.setPlayerId(entry.getKey());
            member.setDisplayName(entry.getValue());
            Long memberUserId = room.getPlayerUserIds().get(entry.getKey());
            member.setUserId(memberUserId == null ? 0L : memberUserId);
            member.setHost(entry.getKey().equals(room.getHostPlayerId()));
            members.add(member);
        }
        dto.setMembers(members);
        return dto;
    }

    private RoomInviteDto toInviteDto(RoomInvite invite) {
        RoomInviteDto dto = new RoomInviteDto();
        dto.setRoomId(invite.getRoomId());
        dto.setRoomName(invite.getRoomName());
        dto.setFromUserId(invite.getFromUserId());
        dto.setFromDisplayName(invite.getFromDisplayName());
        dto.setCreatedAtMs(invite.getCreatedAtMs());
        return dto;
    }

    private GameStateDto buildState(GameRoom room, String playerId) {
        GameStateSnapshot snapshot = gameEngineFacade.getState(room, playerId);
        return MultiplayerViewMapper.toDto(room, snapshot, playerId);
    }
}

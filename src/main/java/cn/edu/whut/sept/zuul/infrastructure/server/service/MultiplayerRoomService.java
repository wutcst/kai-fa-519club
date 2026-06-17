package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.CommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.GameStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomChatMessageDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInfoDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomSessionDto;
import cn.edu.whut.sept.zuul.multiplayer.GameCommandResult;
import cn.edu.whut.sept.zuul.multiplayer.GameEngineFacade;
import cn.edu.whut.sept.zuul.multiplayer.GameRoom;
import cn.edu.whut.sept.zuul.multiplayer.GameRoomRegistry;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.JoinRoomResult;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomResult;
import cn.edu.whut.sept.zuul.multiplayer.RoomChatMessage;

/**
 * 联机房间与命令业务服务。
 */
@Service
public class MultiplayerRoomService {

    private final GameRoomRegistry roomRegistry = new GameRoomRegistry();
    private final GameEngineFacade gameEngineFacade = new GameEngineFacade();

    public RoomSessionDto createRoom(String roomName, String hostName) {
        GameRoom room = roomRegistry.createRoom(roomName, hostName);
        RoomSessionDto session = new RoomSessionDto();
        session.setRoomId(room.getRoomId());
        session.setRoomName(room.getRoomName());
        session.setPlayerId(room.getHostPlayerId());
        session.setDisplayName(room.getPlayerDisplayNames().get(room.getHostPlayerId()));
        session.setState(buildState(room, room.getHostPlayerId()));
        return session;
    }

    public RoomSessionDto joinRoom(String roomId, String displayName) {
        JoinRoomResult result = roomRegistry.joinRoom(roomId, displayName);
        if (result == null) {
            return null;
        }
        GameRoom room = result.getRoom();
        RoomSessionDto session = new RoomSessionDto();
        session.setRoomId(room.getRoomId());
        session.setRoomName(room.getRoomName());
        session.setPlayerId(result.getPlayerId());
        session.setDisplayName(result.getDisplayName());
        session.setState(buildState(room, result.getPlayerId()));
        return session;
    }

    public List<RoomInfoDto> listRooms() {
        return roomRegistry.listRooms().stream()
            .map(RoomInfoDto::from)
            .collect(Collectors.toList());
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
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        synchronized (room.getLock()) {
            if (!room.hasPlayer(playerId)) {
                throw new IllegalArgumentException("玩家不在该房间中");
            }
            String displayName = room.getPlayerDisplayNames().get(playerId);
            RoomChatMessage message = room.addChatMessage(playerId, displayName, text);
            return RoomChatMessageDto.from(message);
        }
    }

    public LeaveRoomResult leaveRoom(String roomId, String playerId) {
        return roomRegistry.leaveRoom(roomId, playerId);
    }

    public GameRoom findRoom(String roomId) {
        return roomRegistry.findRoom(roomId);
    }

    /**
     * 测试专用：清空房间注册表。
     */
    public void clearAllRoomsForTest() {
        roomRegistry.clearAllForTest();
    }

    private GameStateDto buildState(GameRoom room, String playerId) {
        GameStateSnapshot snapshot = gameEngineFacade.getState(room, playerId);
        return MultiplayerViewMapper.toDto(room, snapshot, playerId);
    }
}

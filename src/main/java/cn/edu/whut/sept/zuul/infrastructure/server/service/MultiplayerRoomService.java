package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.CommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.GameStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInfoDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomSessionDto;
import cn.edu.whut.sept.zuul.multiplayer.GameCommandResult;
import cn.edu.whut.sept.zuul.multiplayer.GameEngineFacade;
import cn.edu.whut.sept.zuul.multiplayer.GameRoom;
import cn.edu.whut.sept.zuul.multiplayer.GameRoomRegistry;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.JoinRoomResult;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomResult;

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
        session.setState(GameStateDto.from(gameEngineFacade.getState(room, room.getHostPlayerId())));
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
        session.setState(GameStateDto.from(gameEngineFacade.getState(room, result.getPlayerId())));
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
        GameStateSnapshot snapshot = gameEngineFacade.getState(room, playerId);
        return GameStateDto.from(snapshot);
    }

    public CommandResponseDto executeCommand(String roomId, String playerId,
                                             String commandWord, String secondWord) {
        GameRoom room = roomRegistry.findRoom(roomId);
        if (room == null) {
            return null;
        }
        GameCommandResult result = gameEngineFacade.executeCommand(
            room, playerId, commandWord, secondWord);
        return CommandResponseDto.from(result);
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
}

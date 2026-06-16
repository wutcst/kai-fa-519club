package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CreateRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.JoinRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.LeaveRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInfoDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomSessionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;

/**
 * 联机房间 REST 接口。
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final MultiplayerRoomService multiplayerRoomService;

    public RoomController(MultiplayerRoomService multiplayerRoomService) {
        this.multiplayerRoomService = multiplayerRoomService;
    }

    @GetMapping
    public ApiResponse<java.util.List<RoomInfoDto>> listRooms() {
        return ApiResponse.ok(multiplayerRoomService.listRooms());
    }

    @PostMapping
    public ApiResponse<RoomSessionDto> createRoom(@RequestBody CreateRoomRequest request) {
        RoomSessionDto session = multiplayerRoomService.createRoom(
            request.getRoomName(), request.getHostName());
        return ApiResponse.ok(session);
    }

    @PostMapping("/{roomId}/join")
    public ApiResponse<RoomSessionDto> joinRoom(@PathVariable String roomId,
                                                  @RequestBody JoinRoomRequest request) {
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            RoomSessionDto session = multiplayerRoomService.joinRoom(roomId, request.getDisplayName());
            return ApiResponse.ok(session);
        } catch (IllegalStateException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<Boolean> leaveRoom(@PathVariable String roomId,
                                          @RequestBody LeaveRoomRequest request) {
        if (request.getPlayerId() == null || request.getPlayerId().trim().isEmpty()) {
            return ApiResponse.fail("playerId 不能为空");
        }
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            multiplayerRoomService.leaveRoom(roomId, request.getPlayerId());
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}

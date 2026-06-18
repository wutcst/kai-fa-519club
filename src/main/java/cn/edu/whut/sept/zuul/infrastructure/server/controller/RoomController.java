package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthSession;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CreateRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.JoinRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.LeaveRoomRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomChatMessageDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInfoDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomInviteDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomSessionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SendChatRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.TeamRoomDto;
import cn.edu.whut.sept.zuul.infrastructure.server.service.AuthServiceProvider;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomAction;
import cn.edu.whut.sept.zuul.multiplayer.LeaveRoomResult;

/**
 * 联机房间 REST 接口。
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final MultiplayerRoomService multiplayerRoomService;
    private final AuthService authService;

    public RoomController(MultiplayerRoomService multiplayerRoomService,
                          AuthServiceProvider authServiceProvider) {
        this.multiplayerRoomService = multiplayerRoomService;
        this.authService = authServiceProvider.getAuthService();
    }

    @GetMapping
    public ApiResponse<List<RoomInfoDto>> listRooms() {
        return ApiResponse.ok(multiplayerRoomService.listRooms());
    }

    @GetMapping("/mine")
    public ApiResponse<TeamRoomDto> myRoom(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(multiplayerRoomService.getMyTeamRoom(session.getUserId()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @GetMapping("/invites")
    public ApiResponse<List<RoomInviteDto>> listInvites(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(multiplayerRoomService.listInvites(session.getUserId()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/invites/{roomId}/reject")
    public ApiResponse<Boolean> rejectInvite(
            @PathVariable String roomId,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            multiplayerRoomService.rejectInvite(session.getUserId(), roomId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<RoomSessionDto> createRoom(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody CreateRoomRequest request) {
        try {
            AuthSession session = requireAuthSession(token);
            RoomSessionDto roomSession = multiplayerRoomService.createRoom(
                request.getRoomName(), session.getDisplayName(), session.getUserId());
            return ApiResponse.ok(roomSession);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/join")
    public ApiResponse<RoomSessionDto> joinRoom(
            @PathVariable String roomId,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody(required = false) JoinRoomRequest request) {
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            AuthSession session = requireAuthSession(token);
            RoomSessionDto roomSession = multiplayerRoomService.joinRoom(
                roomId, session.getDisplayName(), session.getUserId());
            return ApiResponse.ok(roomSession);
        } catch (IllegalStateException exception) {
            return ApiResponse.fail(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/invite")
    public ApiResponse<Boolean> inviteFriend(
            @PathVariable String roomId,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, Long> body) {
        try {
            AuthSession session = requireAuthSession(token);
            Long friendUserId = body.get("friendUserId");
            if (friendUserId == null) {
                return ApiResponse.fail("friendUserId 不能为空");
            }
            multiplayerRoomService.inviteFriend(
                roomId, session.getUserId(), session.getDisplayName(), friendUserId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/start")
    public ApiResponse<Boolean> startGame(
            @PathVariable String roomId,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody(required = false) java.util.Map<String, Integer> body) {
        try {
            AuthSession session = requireAuthSession(token);
            int levelNumber = body != null && body.get("levelNumber") != null
                ? body.get("levelNumber")
                : LevelConfig.MIN_LEVEL;
            multiplayerRoomService.startGame(roomId, session.getUserId(), levelNumber);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/end-round")
    public ApiResponse<Boolean> endRound(@PathVariable String roomId) {
        try {
            multiplayerRoomService.endRound(roomId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/abandon-lobby")
    public ApiResponse<Boolean> abandonLobby(
            @PathVariable String roomId,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            multiplayerRoomService.abandonLobby(roomId, session.getUserId());
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<Boolean> leaveRoom(@PathVariable String roomId,
                                          @RequestHeader(value = "X-Auth-Token", required = false) String token,
                                          @RequestBody LeaveRoomRequest request) {
        if (request.getPlayerId() == null || request.getPlayerId().trim().isEmpty()) {
            return ApiResponse.fail("playerId 不能为空");
        }
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            Long userId = authService.validateToken(token).map(AuthSession::getUserId).orElse(null);
            LeaveRoomAction action = parseLeaveAction(request.getAction());
            LeaveRoomResult result = multiplayerRoomService.leaveRoom(
                roomId, request.getPlayerId(), action, request.getNewHostPlayerId(), userId);
            return ApiResponse.ok(result != null && result.isRoomRemoved());
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/{roomId}/chat")
    public ApiResponse<RoomChatMessageDto> sendChat(@PathVariable String roomId,
                                                    @RequestBody SendChatRequest request) {
        if (request.getPlayerId() == null || request.getPlayerId().trim().isEmpty()) {
            return ApiResponse.fail("playerId 不能为空");
        }
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return ApiResponse.fail("消息不能为空");
        }
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            RoomChatMessageDto message = multiplayerRoomService.sendChat(
                roomId, request.getPlayerId(), request.getText());
            return ApiResponse.ok(message);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    private LeaveRoomAction parseLeaveAction(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return LeaveRoomAction.LEAVE;
        }
        return LeaveRoomAction.valueOf(raw.trim().toUpperCase());
    }

    private AuthSession requireAuthSession(String token) {
        return authService.validateToken(token)
            .orElseThrow(() -> new IllegalArgumentException("请先登录后再进入联机模式"));
    }
}

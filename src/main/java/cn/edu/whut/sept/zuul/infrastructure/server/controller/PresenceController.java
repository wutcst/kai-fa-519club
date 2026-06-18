package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthSession;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.service.AuthServiceProvider;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;
import cn.edu.whut.sept.zuul.infrastructure.social.UserPresenceStatus;

/**
 * 在线状态心跳。
 */
@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final MultiplayerRoomService multiplayerRoomService;
    private final AuthService authService;

    public PresenceController(MultiplayerRoomService multiplayerRoomService,
                              AuthServiceProvider authServiceProvider) {
        this.multiplayerRoomService = multiplayerRoomService;
        this.authService = authServiceProvider.getAuthService();
    }

    @PostMapping("/heartbeat")
    public ApiResponse<Boolean> heartbeat(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        try {
            AuthSession session = requireAuthSession(token);
            UserPresenceStatus status = parseStatus(body.get("status"));
            String roomId = body.get("roomId");
            multiplayerRoomService.updatePresence(session.getUserId(), status, roomId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    private UserPresenceStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UserPresenceStatus.ONLINE;
        }
        try {
            return UserPresenceStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return UserPresenceStatus.ONLINE;
        }
    }

    private AuthSession requireAuthSession(String token) {
        return authService.validateToken(token)
            .orElseThrow(() -> new IllegalArgumentException("请先登录"));
    }
}

package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
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
import cn.edu.whut.sept.zuul.infrastructure.server.dto.FriendRequestDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.FriendViewDto;
import cn.edu.whut.sept.zuul.infrastructure.server.service.AuthServiceProvider;
import cn.edu.whut.sept.zuul.infrastructure.server.service.FriendService;

/**
 * 好友 REST 接口。
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final AuthService authService;

    public FriendController(FriendService friendService, AuthServiceProvider authServiceProvider) {
        this.friendService = friendService;
        this.authService = authServiceProvider.getAuthService();
    }

    @GetMapping
    public ApiResponse<List<FriendViewDto>> listFriends(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(friendService.listFriends(session.getUserId()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @GetMapping("/requests/incoming")
    public ApiResponse<List<FriendRequestDto>> listIncomingFriendRequests(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(friendService.listIncomingFriendRequests(session.getUserId()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Object> sendFriendRequest(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(friendService.sendFriendRequest(session.getUserId(), body.get("username")));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/requests/{fromUserId}/accept")
    public ApiResponse<FriendViewDto> acceptFriendRequest(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable long fromUserId) {
        try {
            AuthSession session = requireAuthSession(token);
            return ApiResponse.ok(friendService.acceptFriendRequest(session.getUserId(), fromUserId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/requests/{fromUserId}/reject")
    public ApiResponse<Boolean> rejectFriendRequest(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable long fromUserId) {
        try {
            AuthSession session = requireAuthSession(token);
            friendService.rejectFriendRequest(session.getUserId(), fromUserId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @DeleteMapping("/{friendUserId}")
    public ApiResponse<Boolean> removeFriend(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable long friendUserId) {
        try {
            AuthSession session = requireAuthSession(token);
            friendService.removeFriend(session.getUserId(), friendUserId);
            return ApiResponse.ok(Boolean.TRUE);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    private AuthSession requireAuthSession(String token) {
        return authService.validateToken(token)
            .orElseThrow(() -> new IllegalArgumentException("请先登录"));
    }
}

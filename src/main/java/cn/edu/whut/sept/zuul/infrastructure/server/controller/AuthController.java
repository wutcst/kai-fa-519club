package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthResult;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;

/**
 * 认证 REST 接口（供联机大厅 / Vue 调用）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = AuthService.createDefault();

    @PostMapping("/signup")
    public ApiResponse<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        AuthResult result = authService.register(
            body.get("username"), body.get("password"), body.get("displayName"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/signin")
    public ApiResponse<Map<String, Object>> signin(@RequestBody Map<String, String> body) {
        AuthResult result = authService.login(body.get("username"), body.get("password"));
        if (!result.isSuccess()) {
            return ApiResponse.fail(result.getMessage());
        }
        return ApiResponse.ok(toSessionMap(result));
    }

    @PostMapping("/signout")
    public ApiResponse<String> signout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        authService.logout(token);
        return ApiResponse.ok("已登出");
    }

    private Map<String, Object> toSessionMap(AuthResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", result.getSession().getUserId());
        map.put("username", result.getSession().getUsername());
        map.put("displayName", result.getSession().getDisplayName());
        map.put("token", result.getSession().getToken());
        map.put("expiresAt", result.getSession().getExpiresAt().toString());
        return map;
    }
}

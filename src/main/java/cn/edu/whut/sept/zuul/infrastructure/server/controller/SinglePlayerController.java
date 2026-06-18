package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CreateSoloSessionRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloCommandRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloCommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloLevelSelectionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloSessionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloViewStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.service.SinglePlayerGuiService;

/**
 * 单机五关 Vue 客户端 REST 接口。
 */
@RestController
@RequestMapping("/api/solo")
public class SinglePlayerController {

    private final SinglePlayerGuiService singlePlayerGuiService;

    public SinglePlayerController(SinglePlayerGuiService singlePlayerGuiService) {
        this.singlePlayerGuiService = singlePlayerGuiService;
    }

    @GetMapping("/levels")
    public ApiResponse<SoloLevelSelectionDto> listLevels(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        return ApiResponse.ok(singlePlayerGuiService.getLevelSelection(token));
    }

    @PostMapping("/sessions")
    public ApiResponse<SoloSessionDto> createSession(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody(required = false) CreateSoloSessionRequest request) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.createSession(request, token));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/state")
    public ApiResponse<SoloViewStateDto> getState(@PathVariable String sessionId) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.getViewState(sessionId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/command")
    public ApiResponse<SoloCommandResponseDto> executeCommand(
        @PathVariable String sessionId,
        @RequestBody SoloCommandRequest request) {
        try {
            SoloCommandResponseDto response = singlePlayerGuiService.executeCommand(
                sessionId,
                request.getCommandWord(),
                request.getSecondWord());
            return ApiResponse.ok(response);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/look")
    public ApiResponse<SoloCommandResponseDto> look(@PathVariable String sessionId) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.performLook(sessionId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/talk")
    public ApiResponse<SoloCommandResponseDto> talk(@PathVariable String sessionId) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.performTalk(sessionId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/outcome/dismiss")
    public ApiResponse<SoloViewStateDto> dismissOutcome(@PathVariable String sessionId) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.dismissOutcome(sessionId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/locked/dismiss")
    public ApiResponse<SoloViewStateDto> dismissLocked(@PathVariable String sessionId) {
        try {
            return ApiResponse.ok(singlePlayerGuiService.dismissLockedOverlay(sessionId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Boolean> destroySession(@PathVariable String sessionId) {
        singlePlayerGuiService.destroySession(sessionId);
        return ApiResponse.ok(Boolean.TRUE);
    }
}

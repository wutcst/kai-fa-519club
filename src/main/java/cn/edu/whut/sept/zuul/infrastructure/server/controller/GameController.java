package cn.edu.whut.sept.zuul.infrastructure.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.whut.sept.zuul.infrastructure.server.dto.ApiResponse;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CommandRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.GameStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;

/**
 * 联机游戏命令与状态 REST 接口。
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final MultiplayerRoomService multiplayerRoomService;

    public GameController(MultiplayerRoomService multiplayerRoomService) {
        this.multiplayerRoomService = multiplayerRoomService;
    }

    @PostMapping("/command")
    public ApiResponse<CommandResponseDto> executeCommand(@RequestBody CommandRequest request) {
        if (request.getRoomId() == null || request.getPlayerId() == null
            || request.getCommandWord() == null) {
            return ApiResponse.fail("roomId、playerId、commandWord 不能为空");
        }
        if (multiplayerRoomService.findRoom(request.getRoomId()) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            CommandResponseDto response = multiplayerRoomService.executeCommand(
                request.getRoomId(),
                request.getPlayerId(),
                request.getCommandWord(),
                request.getSecondWord());
            return ApiResponse.ok(response);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }

    @GetMapping("/state")
    public ApiResponse<GameStateDto> getState(@RequestParam String roomId,
                                              @RequestParam String playerId) {
        if (multiplayerRoomService.findRoom(roomId) == null) {
            return ApiResponse.fail("房间不存在");
        }
        try {
            GameStateDto state = multiplayerRoomService.getState(roomId, playerId);
            return ApiResponse.ok(state);
        } catch (IllegalArgumentException exception) {
            return ApiResponse.fail(exception.getMessage());
        }
    }
}

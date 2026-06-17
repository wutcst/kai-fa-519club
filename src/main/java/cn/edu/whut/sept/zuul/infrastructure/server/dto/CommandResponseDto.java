package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.multiplayer.GameCommandResult;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;

/**
 * 命令执行响应 DTO。
 */
public class CommandResponseDto {

    private List<String> messages = new ArrayList<>();
    private boolean quitRequested;
    private GameStateDto state;
    private String noticeMessage;

    public static CommandResponseDto from(GameCommandResult result) {
        CommandResponseDto dto = new CommandResponseDto();
        dto.messages = result.getMessages();
        dto.quitRequested = result.isQuitRequested();
        GameStateSnapshot snapshot = result.getState();
        dto.state = snapshot == null ? null : GameStateDto.from(snapshot);
        return dto;
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    public GameStateDto getState() {
        return state;
    }

    public void setState(GameStateDto state) {
        this.state = state;
    }

    public String getNoticeMessage() {
        return noticeMessage;
    }

    public void setNoticeMessage(String noticeMessage) {
        this.noticeMessage = noticeMessage;
    }
}

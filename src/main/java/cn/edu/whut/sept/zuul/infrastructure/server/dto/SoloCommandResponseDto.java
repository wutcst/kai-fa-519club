package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 单机命令响应。
 */
public class SoloCommandResponseDto {

    private List<String> messages = new ArrayList<>();
    private String popupMessage;
    private boolean combinePrompt;
    private SoloViewStateDto state;

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getPopupMessage() {
        return popupMessage;
    }

    public void setPopupMessage(String popupMessage) {
        this.popupMessage = popupMessage;
    }

    public boolean isCombinePrompt() {
        return combinePrompt;
    }

    public void setCombinePrompt(boolean combinePrompt) {
        this.combinePrompt = combinePrompt;
    }

    public SoloViewStateDto getState() {
        return state;
    }

    public void setState(SoloViewStateDto state) {
        this.state = state;
    }
}

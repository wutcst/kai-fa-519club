package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 创建单机会话响应。
 */
public class SoloSessionDto {

    private String sessionId;
    private SoloViewStateDto state;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SoloViewStateDto getState() {
        return state;
    }

    public void setState(SoloViewStateDto state) {
        this.state = state;
    }
}

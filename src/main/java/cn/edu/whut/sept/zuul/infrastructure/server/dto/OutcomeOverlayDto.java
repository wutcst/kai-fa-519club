package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 结局弹层 DTO（超时 / 通关 / 全通）。
 */
public class OutcomeOverlayDto {

    private String type;
    private String title;
    private String message;
    private String actionLabel;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }
}

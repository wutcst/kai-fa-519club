package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 单机选关面板数据。
 */
public class SoloLevelSelectionDto {

    private List<SoloLevelOptionDto> levels = new ArrayList<>();
    private String comingSoonLabel = "…";
    private String comingSoonMessage = "关卡正在开发";

    public List<SoloLevelOptionDto> getLevels() {
        return levels;
    }

    public void setLevels(List<SoloLevelOptionDto> levels) {
        this.levels = levels;
    }

    public String getComingSoonLabel() {
        return comingSoonLabel;
    }

    public void setComingSoonLabel(String comingSoonLabel) {
        this.comingSoonLabel = comingSoonLabel;
    }

    public String getComingSoonMessage() {
        return comingSoonMessage;
    }

    public void setComingSoonMessage(String comingSoonMessage) {
        this.comingSoonMessage = comingSoonMessage;
    }
}

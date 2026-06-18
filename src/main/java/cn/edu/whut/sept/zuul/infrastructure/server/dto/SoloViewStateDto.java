package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 单机五关 Vue 客户端完整视图状态。
 */
public class SoloViewStateDto {

    private String sessionId;
    private int level;
    private String levelTitle;
    private String levelState;
    private int remainingSeconds;
    private String timerText;
    private String roomId;
    private String roomDescription;
    private String westTrapBanner;
    private List<ItemViewDto> roomItems = new ArrayList<>();
    private List<ItemViewDto> inventory = new ArrayList<>();
    private int inventoryWeight;
    private int maxInventoryWeight;
    private int remainingCapacity;
    private ExitAvailabilityDto exits = new ExitAvailabilityDto();
    private UiActionFlagsDto actions = new UiActionFlagsDto();
    private OutcomeOverlayDto outcome;
    private String lockedOverlayMessage;
    private boolean interactionBlocked;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    public String getLevelState() {
        return levelState;
    }

    public void setLevelState(String levelState) {
        this.levelState = levelState;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public String getTimerText() {
        return timerText;
    }

    public void setTimerText(String timerText) {
        this.timerText = timerText;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getWestTrapBanner() {
        return westTrapBanner;
    }

    public void setWestTrapBanner(String westTrapBanner) {
        this.westTrapBanner = westTrapBanner;
    }

    public List<ItemViewDto> getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(List<ItemViewDto> roomItems) {
        this.roomItems = roomItems;
    }

    public List<ItemViewDto> getInventory() {
        return inventory;
    }

    public void setInventory(List<ItemViewDto> inventory) {
        this.inventory = inventory;
    }

    public int getInventoryWeight() {
        return inventoryWeight;
    }

    public void setInventoryWeight(int inventoryWeight) {
        this.inventoryWeight = inventoryWeight;
    }

    public int getMaxInventoryWeight() {
        return maxInventoryWeight;
    }

    public void setMaxInventoryWeight(int maxInventoryWeight) {
        this.maxInventoryWeight = maxInventoryWeight;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public ExitAvailabilityDto getExits() {
        return exits;
    }

    public void setExits(ExitAvailabilityDto exits) {
        this.exits = exits;
    }

    public UiActionFlagsDto getActions() {
        return actions;
    }

    public void setActions(UiActionFlagsDto actions) {
        this.actions = actions;
    }

    public OutcomeOverlayDto getOutcome() {
        return outcome;
    }

    public void setOutcome(OutcomeOverlayDto outcome) {
        this.outcome = outcome;
    }

    public String getLockedOverlayMessage() {
        return lockedOverlayMessage;
    }

    public void setLockedOverlayMessage(String lockedOverlayMessage) {
        this.lockedOverlayMessage = lockedOverlayMessage;
    }

    public boolean isInteractionBlocked() {
        return interactionBlocked;
    }

    public void setInteractionBlocked(boolean interactionBlocked) {
        this.interactionBlocked = interactionBlocked;
    }
}

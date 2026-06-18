package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.PlayerStateSnapshot;

/**
 * 联机游戏状态 DTO。
 */
public class GameStateDto {

    private int level;
    private String levelState;
    private int remainingSeconds;
    private String timerText;
    private String activePlayerId;
    private String roomId;
    private String roomDescription;
    private String bulletin;
    private String westTrapBanner;
    private List<ItemViewDto> roomItems = new ArrayList<>();
    private List<ItemViewDto> inventory = new ArrayList<>();
    private int inventoryWeight;
    private int maxInventoryWeight;
    private int remainingCapacity;
    private ExitAvailabilityDto exits = new ExitAvailabilityDto();
    private UiActionFlagsDto actions = new UiActionFlagsDto();
    private List<PlayerStateDto> players = new ArrayList<>();
    private List<RoomChatMessageDto> chatMessages = new ArrayList<>();
    private boolean roomInGame;
    private String hostPlayerId;

    public static GameStateDto from(GameStateSnapshot snapshot) {
        GameStateDto dto = new GameStateDto();
        dto.level = snapshot.getLevel();
        dto.levelState = snapshot.getLevelState();
        dto.remainingSeconds = snapshot.getRemainingSeconds();
        dto.timerText = snapshot.getTimerText();
        dto.activePlayerId = snapshot.getActivePlayerId();
        dto.roomId = snapshot.getRoomId();
        dto.roomDescription = snapshot.getRoomDescription();
        for (PlayerStateSnapshot player : snapshot.getPlayers()) {
            dto.players.add(PlayerStateDto.from(player));
        }
        return dto;
    }

    public int getLevel() {
        return level;
    }

    public String getLevelState() {
        return levelState;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getTimerText() {
        return timerText;
    }

    public String getActivePlayerId() {
        return activePlayerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
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

    public List<PlayerStateDto> getPlayers() {
        return players;
    }

    public List<RoomChatMessageDto> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<RoomChatMessageDto> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public boolean isRoomInGame() {
        return roomInGame;
    }

    public void setRoomInGame(boolean roomInGame) {
        this.roomInGame = roomInGame;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }
}

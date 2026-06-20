package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.multiplayer.PlayerStateSnapshot;

/**
 * 玩家状态 DTO。
 */
public class PlayerStateDto {

    private String playerId;
    private String displayName;
    private String roomId;
    private String roomName;
    private List<String> inventory = new ArrayList<>();

    public static PlayerStateDto from(PlayerStateSnapshot snapshot) {
        PlayerStateDto dto = new PlayerStateDto();
        dto.playerId = snapshot.getPlayerId();
        dto.displayName = snapshot.getDisplayName();
        dto.roomId = snapshot.getRoomId();
        dto.roomName = snapshot.getRoomName();
        dto.inventory = snapshot.getInventory();
        return dto;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<String> getInventory() {
        return inventory;
    }
}

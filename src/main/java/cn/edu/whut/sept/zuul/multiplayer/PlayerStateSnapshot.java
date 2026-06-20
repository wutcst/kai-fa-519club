package cn.edu.whut.sept.zuul.multiplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;

/**
 * 联机玩家状态快照，用于同步给其他客户端。
 */
public class PlayerStateSnapshot {

    private final String playerId;
    private final String displayName;
    private final String roomId;
    private final String roomName;
    private final List<String> inventory;

    public PlayerStateSnapshot(String playerId, String displayName,
                               String roomId, String roomName, List<String> inventory) {
        this.playerId = playerId;
        this.displayName = displayName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.inventory = inventory == null ? new ArrayList<>() : new ArrayList<>(inventory);
    }

    public static PlayerStateSnapshot from(String playerId, Player player) {
        Room room = player.getCurrentRoom();
        List<String> items = new ArrayList<>();
        for (Item item : player.getInventory()) {
            items.add(item.getDescription());
        }
        String roomId = room == null ? "" : room.getRoomId();
        String roomName = room == null ? "" : room.getShortDescription();
        return new PlayerStateSnapshot(playerId, player.getName(), roomId, roomName, items);
    }

    @SuppressWarnings("unchecked")
    public static PlayerStateSnapshot fromApiMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        List<String> inventory = new ArrayList<>();
        Object inventoryObj = map.get("inventory");
        if (inventoryObj instanceof List) {
            for (Object item : (List<?>) inventoryObj) {
                inventory.add(String.valueOf(item));
            }
        }
        return new PlayerStateSnapshot(
            String.valueOf(map.getOrDefault("playerId", "")),
            String.valueOf(map.getOrDefault("displayName", "")),
            String.valueOf(map.getOrDefault("roomId", "")),
            String.valueOf(map.getOrDefault("roomName", "")),
            inventory
        );
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
        return new ArrayList<>(inventory);
    }
}

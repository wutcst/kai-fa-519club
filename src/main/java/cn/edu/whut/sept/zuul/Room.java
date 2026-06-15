/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.2
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 表示游戏中的房间，包含描述、公告、出口与物品。
 */
public class Room {

    private String roomId;
    private String description;
    private String bulletin;
    private HashMap<String, Room> exits;
    private List<Item> items;

    /**
     * 测试或临时房间用构造（无 ID）。
     *
     * @param description 房间描述
     */
    public Room(String description) {
        this(null, description, null);
    }

    /**
     * 完整构造：房间 ID、描述与进入公告。
     *
     * @param roomId 房间标识
     * @param description 房间描述
     * @param bulletin 进入时展示的公告
     */
    public Room(String roomId, String description, String bulletin) {
        this.roomId = roomId;
        this.description = description;
        this.bulletin = bulletin;
        this.exits = new HashMap<>();
        this.items = new ArrayList<>();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * 清空房间内所有物品（E16 按关刷新物品栏）。
     */
    public void clearItems() {
        items.clear();
    }

    /**
     * 房间中是否已有指定名称的物品。
     *
     * @param itemDescription 物品描述
     * @return 存在返回 true
     */
    public boolean containsItem(String itemDescription) {
        if (itemDescription == null) {
            return false;
        }
        for (Item item : items) {
            if (item.getDescription().equalsIgnoreCase(itemDescription.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按描述从房间移除物品。
     *
     * @param itemDescription 物品描述
     * @return 被移除的物品，未找到返回 null
     */
    public Item removeItemByDescription(String itemDescription) {
        if (itemDescription == null) {
            return null;
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getDescription().equalsIgnoreCase(itemDescription.trim())) {
                return items.remove(i);
            }
        }
        return null;
    }

    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public String getRoomId() {
        return roomId;
    }

    public String getShortDescription() {
        return description;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
    }

    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("你在").append(description).append("。\n");
        if (bulletin != null && !bulletin.isEmpty()) {
            sb.append("【公告】").append(bulletin).append("\n");
        }
        sb.append(getExitString());
        if (!items.isEmpty()) {
            sb.append("\n房间里有这些物品:");
            for (Item item : items) {
                sb.append("\n- ").append(item.getDetails());
            }
        } else {
            sb.append("\n这个房间里没有任何物品。");
        }
        return sb.toString();
    }

    private String getExitString() {
        StringBuilder returnString = new StringBuilder("出口:");
        Set<String> keys = exits.keySet();
        for (String exit : keys) {
            returnString.append(" ").append(exit);
        }
        return returnString.toString();
    }

    public Room getExit(String direction) {
        return exits.get(direction);
    }
}

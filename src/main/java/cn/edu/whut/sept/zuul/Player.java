/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示游戏中的玩家，包含玩家信息、当前位置和携带物品。
 * 新增：实现玩家物品管理功能，包括拾取、丢弃物品和负重限制。
 *
 * @author liujing
 * @version 1.5
 */
public class Player {
    private String name; // 玩家姓名
    private Room currentRoom; // 玩家当前所在房间
    private List<Item> inventory; // 玩家携带的物品列表
    private int maxWeight; // 玩家最大负重能力（单位：克）
    private int currentWeight; // 玩家当前负重

    /**
     * 初始化玩家实例，设置默认值
     *
     * @param name 玩家姓名
     * @param startRoom 初始房间
     */
    public Player(String name, Room startRoom) {
        this.name = name;
        this.currentRoom = startRoom;
        this.inventory = new ArrayList<>();
        this.maxWeight = 3000; // 默认最大负重为3000克
        this.currentWeight = 0; // 初始负重为0
    }

    /**
     * 获取玩家姓名
     *
     * @return 玩家姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取玩家当前所在房间
     *
     * @return 当前房间实例
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 设置玩家当前所在房间
     *
     * @param room 目标房间实例
     */
    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    /**
     * 获取玩家最大负重能力
     *
     * @return 最大负重（克）
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * 增加玩家的最大负重能力
     *
     * @param increment 增加的重量（克）
     */
    public void increaseMaxWeight(int increment) {
        this.maxWeight += increment;
    }

    /**
     * 获取玩家当前负重
     *
     * @return 当前负重（克）
     */
    public int getCurrentWeight() {
        return currentWeight;
    }

    /**
     * 计算剩余可携带重量
     *
     * @return 剩余可携带重量（克）
     */
    public int getRemainingCapacity() {
        return maxWeight - currentWeight;
    }

    /**
     * 获取玩家携带的所有物品
     *
     * @return 物品列表的副本
     */
    public List<Item> getInventory() {
        return new ArrayList<>(inventory);
    }

    /**
     * 玩家拾取指定物品
     *
     * @param item 要拾取的物品
     * @return 拾取成功返回true，否则返回false
     */
    public boolean takeItem(Item item) {
        if (item == null) {
            return false;
        }

        int itemWeight = item.getWeight();
        if (currentWeight + itemWeight > maxWeight) {
            return false; // 超过负重限制
        }

        inventory.add(item);
        currentWeight += itemWeight;
        return true;
    }

    /**
     * 玩家丢弃指定物品
     *
     * @param itemDescription 要丢弃的物品描述
     * @return 丢弃的物品，如果未找到则返回null
     */
    public Item dropItem(String itemDescription) {
        if (itemDescription == null || itemDescription.trim().isEmpty()) {
            return null;
        }

        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item.getDescription().equalsIgnoreCase(itemDescription.trim())) {
                inventory.remove(i);
                currentWeight -= item.getWeight();
                return item;
            }
        }

        return null; // 未找到指定物品
    }

    /**
     * 玩家丢弃所有物品
     *
     * @return 丢弃的物品列表
     */
    public List<Item> dropAllItems() {
        List<Item> droppedItems = new ArrayList<>(inventory);
        inventory.clear();
        currentWeight = 0;
        return droppedItems;
    }

    /**
     * 从玩家物品中查找指定物品
     *
     * @param itemDescription 物品描述
     * @return 找到的物品，未找到返回null
     */
    public Item findItemInInventory(String itemDescription) {
        if (itemDescription == null) {
            return null;
        }

        for (Item item : inventory) {
            if (item.getDescription().equalsIgnoreCase(itemDescription.trim())) {
                return item;
            }
        }

        return null;
    }

    /**
     * 从玩家物品中查找魔法饼干
     *
     * @return 魔法饼干物品，未找到返回null
     */
    public Item findMagicCookie() {
        for (Item item : inventory) {
            if (item.getDescription().equalsIgnoreCase("magic cookie")) {
                return item;
            }
        }
        return null;
    }

    /**
     * 从物品栏中移除指定物品（用于吃掉物品等场景）
     *
     * @param item 要移除的物品
     * @return 移除成功返回true，否则返回false
     */
    public boolean removeItemFromInventory(Item item) {
        if (item == null) {
            return false;
        }

        boolean removed = inventory.remove(item);
        if (removed) {
            currentWeight -= item.getWeight();
        }
        return removed;
    }

    /**
     * 获取玩家物品列表的格式化字符串
     *
     * @return 物品列表字符串
     */
    public String getInventoryDetails() {
        if (inventory.isEmpty()) {
            return "你没有携带任何物品。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("你携带的物品:\n");
        for (Item item : inventory) {
            sb.append("- ").append(item.getDetails()).append("\n");
        }
        sb.append("总重量: ").append(currentWeight).append("g / ").append(maxWeight).append("g");
        return sb.toString();
    }
}
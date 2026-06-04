/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 * 【新增】重构命令处理逻辑，采用命令模式实现命令模块化管理。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 表示游戏中的房间，包含描述信息、出口连接关系和物品列表。
 * 【修改】新增物品存储功能，支持添加和查看房间内物品。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.1
 */
public class Room
{
    private String description;// 房间的描述信息
    private HashMap<String, Room> exits;// 存储房间出口的映射
    private List<Item> items; // 存储房间内的物品列表【新增】

    /**
     * 初始化房间实例，设置描述并创建出口映射和物品列表。
     *
     * @param description 房间的文本描述
     */
    public Room(String description)
    {
        this.description = description;
        exits = new HashMap<>(); // 初始化出口映射表
        items = new ArrayList<>(); // 初始化物品列表【新增】
    }

    /**
     * 向房间添加物品
     * 【新增】
     *
     * @param item 要添加到房间的物品实例
     */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * 获取房间内的所有物品
     * 【新增】
     *
     * @return 物品列表
     */
    public List<Item> getItems() {
        return new ArrayList<>(items); // 返回副本防止外部修改
    }

    /**
     * 设置房间的出口方向和目标房间。
     *
     * @param direction 出口方向（如"east"）
     * @param neighbor 目标房间实例
     */
    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    /**
     * 获取房间的简短描述。
     *
     * @return 房间描述字符串
     */
    public String getShortDescription()
    {
        return description;
    }

    /**
     * 获取房间的详细描述，包含出口信息和物品信息。
     * 【修改】添加物品信息展示
     *
     * @return 包含描述、出口和物品的长字符串
     */
    public String getLongDescription()
    {
        String description = "You are " + this.description + ".\n" + getExitString();
        // 添加物品信息【新增】
        if (!items.isEmpty()) {
            description += "\n房间里有这些物品:";
            for (Item item : items) {
                description += "\n- " + item.getDetails();
            }
        } else {
            description += "\n这个房间里没有任何物品。";
        }
        return description;
    }

    /**
     * 获取房间出口的描述字符串。
     *
     * @return 格式为"Exits: east west"的字符串
     */
    private String getExitString()
    {
        String returnString = "Exits:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
    }

    /**
     * 根据方向获取目标房间。
     *
     * @param direction 出口方向
     * @return 目标房间实例，若无该方向出口则返回null
     */
    public Room getExit(String direction)
    {
        return exits.get(direction);
    }

}



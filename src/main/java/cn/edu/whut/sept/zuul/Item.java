/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 * 【新增】重构命令处理逻辑，采用命令模式实现命令模块化管理。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.2
 */
package cn.edu.whut.sept.zuul;

/**
 * 表示游戏中的物品，包含描述和重量属性。
 * 新增：用于实现房间物品存储功能，支持玩家查看物品信息。
 *
 * @author liujing
 * @version 1.2
 */
public class Item {
    private String description; // 物品描述
    private int weight; // 物品重量（单位：克）

    /**
     * 初始化物品实例
     *
     * @param description 物品的文本描述
     * @param weight 物品的重量值
     */
    public Item(String description, int weight) {
        this.description = description;
        this.weight = weight;
    }

    /**
     * 获取物品的描述信息
     *
     * @return 物品描述字符串
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取物品的重量
     *
     * @return 重量数值
     */
    public int getWeight() {
        return weight;
    }

    /**
     * 获取物品的详细信息（包含描述和重量）
     *
     * @return 格式化的物品信息字符串
     */
    public String getDetails() {
        return description + " (重量: " + weight + "g)";
    }
}
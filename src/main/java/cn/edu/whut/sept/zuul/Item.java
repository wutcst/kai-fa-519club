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
 * 表示游戏中的物品，包含短名、重量与详细介绍。
 *
 * @author liujing
 * @version 1.3
 */
public class Item {
    private final String description;
    private final int weight;
    private final String longDescription;

    /**
     * 初始化物品实例（自动从 ItemCatalog 补全详细介绍）。
     *
     * @param description 物品短名
     * @param weight 物品重量（克）
     */
    public Item(String description, int weight) {
        this(description, weight, ItemCatalog.getLongDescription(description));
    }

    /**
     * 初始化物品实例并指定详细介绍。
     *
     * @param description 物品短名
     * @param weight 物品重量（克）
     * @param longDescription 详细介绍
     */
    public Item(String description, int weight, String longDescription) {
        this.description = description;
        this.weight = weight;
        this.longDescription = longDescription;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * 获取物品详细介绍（供 inspect 命令使用）。
     *
     * @return 详细介绍文本
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * 获取物品列表行摘要（短名 + 重量）。
     *
     * @return 格式化的物品信息字符串
     */
    public String getDetails() {
        return description + " (重量: " + weight + "g)";
    }
}

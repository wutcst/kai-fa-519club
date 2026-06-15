/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.6
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 查看背包物品的命令类（仅显示玩家携带物，不含房间地上物品）。
 *
 * @author liujing
 * @version 1.6
 */
public class ItemsCommand implements CommandInterface {

    @Override
    public boolean execute(Game game, String secondWord) {
        System.out.println("\n=== 你的背包 ===");
        System.out.println(game.getPlayer().getInventoryDetails());
        System.out.println("提示：使用 inspect <物品名> 查看详细介绍。");
        return false;
    }

    @Override
    public String getCommandName() {
        return "items";
    }

    /**
     * help 中展示的 items 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "items - 查看背包中的物品";
    }
}

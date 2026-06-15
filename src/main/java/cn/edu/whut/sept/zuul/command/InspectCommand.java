/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;

/**
 * 查看背包中某件物品详细介绍的命令。
 */
public class InspectCommand implements CommandInterface {

    /**
     * help 中展示的 inspect 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "inspect <物品> - 查看背包中物品的详细介绍";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (secondWord == null || secondWord.trim().isEmpty()) {
            System.out.println("请指定要查看的物品，例如：inspect 湿漉漉的三十元钱");
            return false;
        }

        Item item = game.getPlayer().findItemInInventory(secondWord);
        if (item == null) {
            System.out.println("你的背包里没有「" + secondWord.trim() + "」。");
            System.out.println("先用 items 查看背包，再 inspect 物品全名。");
            return false;
        }

        System.out.println();
        System.out.println("=== " + item.getDescription() + " ===");
        System.out.println(item.getLongDescription());
        System.out.println("重量: " + item.getWeight() + "g");
        System.out.println();
        return false;
    }

    @Override
    public String getCommandName() {
        return "inspect";
    }
}

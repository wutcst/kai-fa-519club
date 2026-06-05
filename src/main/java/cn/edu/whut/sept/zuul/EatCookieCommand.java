/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;
/**
 * 处理吃掉魔法饼干的命令类
 * 新增：实现eat cookie命令，吃掉魔法饼干增加负重能力
 *
 * @author liujing
 * @version 1.5
 */
public class EatCookieCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();

        // 检查命令格式
        if (secondWord == null) {
            System.out.println("Eat what? 请使用 'eat cookie' 命令。");
            return false;
        }

        // 只检查第一个单词是否为"cookie"，忽略可能的额外单词
        String[] words = secondWord.split(" ");
        if (!words[0].equalsIgnoreCase("cookie")) {
            System.out.println("Eat " + words[0] + "? 请使用 'eat cookie' 命令。");
            return false;
        }

        // 查找魔法饼干
        Item cookie = player.findMagicCookie();
        if (cookie == null) {
            System.out.println("你没有magic cookie可以吃！");
            System.out.println("提示：magic cookie可能藏在某些房间里，使用 'take magic cookie' 命令拾取它。");
            return false;
        }

        // 吃掉饼干
        player.removeItemFromInventory(cookie);
        int weightIncrease = 1000; // 饼干增加1000克负重
        player.increaseMaxWeight(weightIncrease);

        System.out.println("你吃掉了 " + cookie.getDescription() + "！");
        System.out.println("感觉力量增强了！最大负重增加了 " + weightIncrease + "g。");
        System.out.println("当前最大负重: " + player.getMaxWeight() + "g");
        System.out.println("当前负重: " + player.getCurrentWeight() + "g");
        System.out.println("剩余负重: " + player.getRemainingCapacity() + "g");

        return false;
    }

    @Override
    public String getCommandName() {
        return "eat";
    }
}
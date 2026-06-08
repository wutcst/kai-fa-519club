/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelManager;

/**
 * 处理吃掉魔法饼干的命令类（F5 增负重 + E7 每关首次加时 300 秒）。
 *
 * @author liujing
 * @version 1.5
 */
public class EatCookieCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();

        if (secondWord == null) {
            System.out.println("Eat what? 请使用 'eat cookie' 命令。");
            return false;
        }

        String[] words = secondWord.split(" ");
        if (!words[0].equalsIgnoreCase("cookie")) {
            System.out.println("Eat " + words[0] + "? 请使用 'eat cookie' 命令。");
            return false;
        }

        Item cookie = player.findMagicCookie();
        if (cookie == null) {
            System.out.println("你没有magic cookie可以吃！");
            System.out.println("提示：magic cookie可能藏在某些房间里，使用 'take magic cookie' 命令拾取它。");
            return false;
        }

        player.removeItemFromInventory(cookie);
        int weightIncrease = 1000;
        player.increaseMaxWeight(weightIncrease);

        LevelManager levelManager = game.getLevelManager();
        System.out.println("你吃掉了 " + cookie.getDescription() + "！");
        if (levelManager.isMagicCookieBonusAvailable()) {
            game.getLevelTimer().addSeconds(ActionTimeCost.COOKIE_BONUS);
            levelManager.markMagicCookieBonusUsed();
            System.out.println("魔法饼干让你多争取了 " + ActionTimeCost.COOKIE_BONUS + " 秒！");
            System.out.println(game.getLevelTimer().getDisplayText());
        } else {
            System.out.println("本关魔法饼干加时效果已用完，但你仍感到力量增强了。");
        }
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

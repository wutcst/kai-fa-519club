/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.6
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelManager;

/**
 * 处理 eat 命令：食用背包中的食物（F5 魔法饼干增负重 + E7 食用时扣时并加时 300 秒）。
 *
 * @author liujing
 * @version 1.6
 */
public class EatCookieCommand implements CommandInterface {

    /**
     * help 中展示的 eat 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "eat - 食用背包中的食物（耗时 " + ActionTimeCost.EAT + " 秒）；"
                + "magic cookie 食用时同步 +" + ActionTimeCost.COOKIE_BONUS + " 秒并增负重";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        Player player = game.getPlayer();
        Item food = FoodItems.findFirstEdible(player);

        if (food == null) {
            System.out.println("背包里没有可以吃的食物！");
            System.out.println("提示：先把食物 take 进背包，再使用 eat 命令。");
            return false;
        }

        player.removeItemFromInventory(food);
        ActionTimeCost.deduct(game, ActionTimeCost.EAT);

        if (FoodItems.isMagicCookie(food.getDescription())) {
            eatMagicCookie(game, player, food.getDescription());
        } else if (FoodItems.isMilkTea(food.getDescription())) {
            eatMilkTea(game, food.getDescription());
        } else {
            System.out.println("你吃掉了 " + food.getDescription() + "！（耗时 "
                    + ActionTimeCost.EAT + " 秒）");
            System.out.println("除了填饱肚子，似乎没什么特别的效果……");
        }

        return false;
    }

    private void eatMilkTea(Game game, String foodName) {
        ActionTimeCost.deduct(game, ActionTimeCost.MILK_TEA_DIARRHEA);
        System.out.println("你喝掉了 " + foodName + "！（食用耗时 " + ActionTimeCost.EAT
                + " 秒）");
        System.out.println("好像拉肚子了，又在厕所多耗了 " + ActionTimeCost.MILK_TEA_DIARRHEA + " 秒……");
        System.out.println(game.getLevelTimer().getDisplayText());
    }

    private void eatMagicCookie(Game game, Player player, String foodName) {
        int weightIncrease = 1000;
        player.increaseMaxWeight(weightIncrease);

        LevelManager levelManager = game.getLevelManager();
        if (levelManager.isMagicCookieBonusAvailable()) {
            game.getLevelTimer().addSeconds(ActionTimeCost.COOKIE_BONUS);
            levelManager.markMagicCookieBonusUsed();
            System.out.println("你吃掉了 " + foodName + "！食用耗时 " + ActionTimeCost.EAT
                    + " 秒的同时，魔法饼干让你多争取了 " + ActionTimeCost.COOKIE_BONUS + " 秒！");
            System.out.println(game.getLevelTimer().getDisplayText());
        } else {
            System.out.println("你吃掉了 " + foodName + "！（耗时 " + ActionTimeCost.EAT + " 秒）");
            System.out.println("本关魔法饼干加时已获得，但你仍感到力量增强了。");
        }
        System.out.println("感觉力量增强了！最大负重增加了 " + weightIncrease + "g。");
        System.out.println("当前最大负重: " + player.getMaxWeight() + "g");
        System.out.println("当前负重: " + player.getCurrentWeight() + "g");
        System.out.println("剩余负重: " + player.getRemainingCapacity() + "g");
    }

    @Override
    public String getCommandName() {
        return "eat";
    }
}

/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理寝室门口提交物品的命令类（E2）：提交归寝单或退寝条。
 *
 * @author liujing
 * @version 1.0
 */
public class SubmitCommand implements CommandInterface {

    public static final String WITHDRAWAL_SLIP_ITEM = "退寝条";
    public static final String WRONG_MEAL_CARD_ITEM = "别人饭卡";

    /**
     * help 中展示的 submit 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "submit <物品> - 在寝室门口（教育超市）提交归寝单或退寝条";
    }

    @Override
    public boolean execute(Game game, String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Submit what? 请指定要提交的物品。");
            return false;
        }

        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || !UseCommand.SUPERMARKET_ROOM_ID.equals(room.getRoomId())) {
            System.out.println("请前往教育超市寝室门口再提交。");
            return false;
        }

        if (!game.getLevelManager().getCurrentLevelConfig().requiresDormitorySubmit()) {
            System.out.println("本关无需提交归寝单。");
            return false;
        }

        if (game.getLevelManager().isDormitorySubmitCompleted()) {
            System.out.println("你已经提交过归寝单了。");
            return false;
        }

        Player player = game.getPlayer();
        String trimmedName = itemName.trim();
        Item item = player.findItemInInventory(trimmedName);
        if (item == null) {
            System.out.println("你没有 '" + trimmedName + "'。");
            return false;
        }

        if (isValidSubmitItem(trimmedName)) {
            player.removeItemFromInventory(item);
            game.getLevelManager().markDormitorySubmitCompleted();
            System.out.println("你在寝室门口提交了" + trimmedName + "，可以进入寝室了。");
            ActionTimeCost.deduct(game, ActionTimeCost.SUBMIT);
            return false;
        }

        System.out.println("提交错误！宿管收下了错误的物品，本关失败。");
        game.getLevelManager().failCurrentLevel();
        return false;
    }

    private boolean isValidSubmitItem(String itemName) {
        return UseCommand.DORM_FORM_ITEM.equalsIgnoreCase(itemName)
            || WITHDRAWAL_SLIP_ITEM.equalsIgnoreCase(itemName);
    }

    @Override
    public String getCommandName() {
        return "submit";
    }
}

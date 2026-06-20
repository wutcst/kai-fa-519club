/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelManager;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 处理在寝室睡觉过关的命令类（E6）：检查当关一卡通、归寝单、密码等条件。
 */
public class SleepCommand implements CommandInterface {

    public static final String PASSWORD_REQUIRED_MESSAGE = "请先解开寝室智能锁";
    private static final int DORMITORY_PASSWORD_LEVEL = 5;

    /**
     * help 中展示的 sleep 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "sleep - 在寝室睡觉过关（须满足当关一卡通、归寝单、密码等条件）";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        Room room = game.getPlayer().getCurrentRoom();
        if (room == null || !UnlockService.DORMITORY_ROOM_ID.equals(room.getRoomId())) {
            System.out.println("请回到寝室再睡觉。");
            return false;
        }

        String missingRequirement = getMissingRequirementMessage(game);
        if (missingRequirement != null) {
            System.out.println(missingRequirement);
            return false;
        }

        game.getLevelManager().completeCurrentLevel();
        return false;
    }

    private String getMissingRequirementMessage(Game game) {
        Player player = game.getPlayer();
        LevelManager levelManager = game.getLevelManager();
        LevelConfig config = levelManager.getCurrentLevelConfig();

        if (player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM) == null) {
            return "你还缺一卡通，无法睡觉。";
        }
        if (config.requiresDormitorySubmit() && !levelManager.isDormitorySubmitCompleted()) {
            return "你还缺已提交的归寝单，无法睡觉。";
        }
        if (config.getLevelNumber() == DORMITORY_PASSWORD_LEVEL
            && !levelManager.isDormitoryPasswordUnlocked()) {
            return PASSWORD_REQUIRED_MESSAGE + "。";
        }
        return null;
    }

    @Override
    public String getCommandName() {
        return "sleep";
    }
}

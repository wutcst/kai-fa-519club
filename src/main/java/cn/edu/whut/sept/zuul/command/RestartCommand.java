package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelState;

/**
 * 关卡通关失败后重开本关。
 */
public class RestartCommand implements CommandInterface {

    public static String getUsageDescription() {
        return "restart - 熄灯失败后重新挑战当前关";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (game.getLevelManager().getState() != LevelState.FAILED) {
            System.out.println("当前无需重开，仅在关卡通关失败后可 restart。");
            return false;
        }
        game.getLevelManager().restartCurrentLevel();
        return false;
    }

    @Override
    public String getCommandName() {
        return "restart";
    }
}

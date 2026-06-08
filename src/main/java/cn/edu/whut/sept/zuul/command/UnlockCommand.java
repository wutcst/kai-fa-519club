/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 处理密码解锁的命令类（E4）：体育馆器材室、第五关寝室智能锁。
 *
 * @author liujing
 * @version 1.0
 */
public class UnlockCommand implements CommandInterface {

    /**
     * help 中展示的 unlock 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "unlock <密码> - 解锁密码门（体育馆值班室、第五关寝室智能锁）";
    }

    @Override
    public boolean execute(Game game, String password) {
        UnlockService.unlock(game, password);
        return false;
    }

    @Override
    public String getCommandName() {
        return "unlock";
    }
}

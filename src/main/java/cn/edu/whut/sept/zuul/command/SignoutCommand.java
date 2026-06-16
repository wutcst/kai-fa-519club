package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 用户登出命令。
 */
public class SignoutCommand implements CommandInterface {

    public static String getUsageDescription() {
        return "signout - 登出当前游戏账号";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (!game.isLoggedIn()) {
            System.out.println("当前未登录。");
            return false;
        }
        game.getAuthService().logout(game.getAuthSession().getToken());
        game.bindAuthSession(null);
        System.out.println("已登出。");
        return false;
    }

    @Override
    public String getCommandName() {
        return "signout";
    }
}

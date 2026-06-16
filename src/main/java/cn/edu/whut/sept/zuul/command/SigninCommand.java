package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthResult;

/**
 * 用户登录命令。
 */
public class SigninCommand implements CommandInterface {

    public static String getUsageDescription() {
        return "signin <用户名> <密码> - 登录游戏账号";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (secondWord == null || secondWord.trim().isEmpty()) {
            System.out.println("用法: signin <用户名> <密码>");
            return false;
        }
        String[] parts = secondWord.trim().split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("用法: signin <用户名> <密码>");
            return false;
        }
        AuthResult result = game.getAuthService().login(parts[0], parts[1]);
        if (result.isSuccess()) {
            game.bindAuthSession(result.getSession());
            System.out.println("登录成功，欢迎回来 " + result.getSession().getDisplayName() + "！");
        } else {
            System.out.println("登录失败: " + result.getMessage());
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "signin";
    }
}

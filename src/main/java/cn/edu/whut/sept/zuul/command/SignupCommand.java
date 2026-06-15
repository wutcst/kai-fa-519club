package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthResult;

/**
 * 用户账号注册命令（与游戏内 {@code use} 物品区分）。
 */
public class SignupCommand implements CommandInterface {

    public static String getUsageDescription() {
        return "signup <用户名> <密码> <昵称> - 注册游戏账号";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (secondWord == null || secondWord.trim().isEmpty()) {
            System.out.println("用法: signup <用户名> <密码> <昵称>");
            return false;
        }
        String[] parts = secondWord.trim().split("\\s+", 3);
        if (parts.length < 3) {
            System.out.println("用法: signup <用户名> <密码> <昵称>");
            return false;
        }
        AuthResult result = game.getAuthService().register(parts[0], parts[1], parts[2]);
        if (result.isSuccess()) {
            game.bindAuthSession(result.getSession());
            System.out.println("注册成功，欢迎 " + parts[2] + "！");
        } else {
            System.out.println("注册失败: " + result.getMessage());
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "signup";
    }
}

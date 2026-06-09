/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 处理帮助信息的命令类
 *
 * @author liujing
 * @version 1.1
 */
public class HelpCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        System.out.println("You are lost. You are alone. You wander");
        System.out.println("around at the university.");
        System.out.println();
        System.out.println("Your command words are:");
        game.getParser().showCommands();
        System.out.println();
        System.out.println(UseCommand.getUsageDescription());
        System.out.println(TalkCommand.getUsageDescription());
        System.out.println(RegisterCommand.getUsageDescription());
        System.out.println(CombineCommand.getUsageDescription());
        System.out.println(UnlockCommand.getUsageDescription());
        System.out.println(SubmitCommand.getUsageDescription());
        System.out.println(FeedCommand.getUsageDescription());
        System.out.println(SleepCommand.getUsageDescription());
        return false;
    }

    @Override
    public String getCommandName() {
        return "help";
    }
}

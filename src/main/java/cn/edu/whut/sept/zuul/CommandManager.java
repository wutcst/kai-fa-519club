/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 * 【新增】重构命令处理逻辑，采用命令模式实现命令模块化管理。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul;

import java.util.HashMap;
import java.util.Map;

import cn.edu.whut.sept.zuul.command.BackCommand;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.CommandInterface;
import cn.edu.whut.sept.zuul.command.DropCommand;
import cn.edu.whut.sept.zuul.command.EatCookieCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.command.HelpCommand;
import cn.edu.whut.sept.zuul.command.ItemsCommand;
import cn.edu.whut.sept.zuul.command.LookCommand;
import cn.edu.whut.sept.zuul.command.QuitCommand;
import cn.edu.whut.sept.zuul.command.RegisterCommand;
import cn.edu.whut.sept.zuul.command.SleepCommand;
import cn.edu.whut.sept.zuul.command.SubmitCommand;
import cn.edu.whut.sept.zuul.command.TakeCommand;
import cn.edu.whut.sept.zuul.command.TalkCommand;
import cn.edu.whut.sept.zuul.command.UnlockCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

/**
 * 命令管理器，负责注册、存储和分发命令
 * 新增：核心管理类，实现命令的动态注册与分发，
 * 解决原硬编码命令分支的扩展性问题。
 *
 * @author liujing
 * @version 1.3
 */
public class CommandManager {
    private Map<String, CommandInterface> commandMap; // 命令映射表
    private final FeedCommand feedCommand = new FeedCommand();

    /**
     * 初始化命令管理器并注册默认命令
     */
    public CommandManager() {
        commandMap = new HashMap<>();
        // 注册默认命令
        registerCommand(new GoCommand());
        registerCommand(new QuitCommand());
        registerCommand(new HelpCommand());
        registerCommand(new LookCommand()); // 注册look新命令
        registerCommand(new BackCommand()); // 注册back命令
        registerCommand(new TakeCommand());
        registerCommand(new DropCommand());
        registerCommand(new ItemsCommand());
        registerCommand(new EatCookieCommand());
        registerCommand(new UseCommand());
        registerCommand(new TalkCommand());
        registerCommand(new RegisterCommand());
        registerCommand(new CombineCommand());
        registerCommand(new UnlockCommand());
        registerCommand(new SubmitCommand());
        registerCommand(new SleepCommand());
    }

    /**
     * 注册新命令
     * @param command 实现CommandInterface的命令实例
     */
    public void registerCommand(CommandInterface command) {
        commandMap.put(command.getCommandName(), command);
    }

    /**
     * 按关卡启用或禁用 feed（猫学长第四关起现身）。
     *
     * @param level 当前关卡号
     */
    public void updateFeedCommandAvailability(int level) {
        if (level >= FeedCommand.MIN_FEED_LEVEL) {
            registerCommand(feedCommand);
        } else {
            commandMap.remove(feedCommand.getCommandName());
        }
    }

    /**
     * 当前关卡是否已开放 feed 命令。
     */
    public boolean isFeedCommandAvailable() {
        return commandMap.containsKey(feedCommand.getCommandName());
    }

    /**
     * 执行指定命令
     * @param commandWord 命令词（如"go"）
     * @param secondWord 命令参数
     * @param game 游戏上下文
     * @return 是否退出游戏
     */
    public boolean executeCommand(String commandWord, String secondWord, Game game) {
        CommandInterface command = commandMap.get(commandWord);
        if (command == null) {
            System.out.println("I don't know what you mean...");
            return false;
        }
        return command.execute(game, secondWord);
    }

    /**
     * 获取所有已注册的命令词
     * @return 命令词数组
     */
    public String[] getCommandWords() {
        return commandMap.keySet().toArray(new String[0]);
    }
}
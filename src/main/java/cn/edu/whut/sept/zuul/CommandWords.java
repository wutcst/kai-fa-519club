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

/**
 * 管理游戏中所有有效的命令词，提供命令合法性校验和列表展示。
 * 重构说明：移除硬编码的validCommands数组，改为从CommandManager
 * 动态获取命令词，实现命令词与命令实例的统一管理。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.1
 */
public class CommandWords
{

    private CommandManager commandManager; // 关联命令管理器

    /**
     * 初始化命令词管理器，关联命令管理器实例
     *
     * @param commandManager 命令管理器实例
     */
    public CommandWords(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * 检查输入字符串是否为有效命令词。
     *
     * @param aString 需要校验的字符串
     * @return 若为有效命令则返回true，否则返回false
     */
    public boolean isCommand(String aString) {
        for (String command : commandManager.getCommandWords()) {
            if (command.equals(aString)) { // 匹配有效命令
                return true;
            }
        }
        return false; // 无匹配命令
    }

    /**
     * 打印所有有效的命令词。
     */
    public void showAll() {
        for (String command : commandManager.getCommandWords()) {
            System.out.print(command + " "); // 逐个输出命令词
        }
        System.out.println();
    }
}

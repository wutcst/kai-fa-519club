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

import java.util.Scanner;

/**
 * 负责解析用户输入的命令，将输入字符串转换为Command对象。
 * 重构说明：构造方法新增CommandManager参数，关联动态命令词管理，
 * 移除与硬编码命令词的耦合。
 * 【修改】支持多单词参数，特别是物品名称包含空格的情况
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */
public class Parser
{
    private CommandWords commands;// 命令词管理实例
    private Scanner reader;// 用户输入读取器

    /**
     * 初始化解析器，关联命令管理器创建命令词实例
     *
     * @param commandManager 命令管理器实例
     */
    public Parser(CommandManager commandManager) {
        commands = new CommandWords(commandManager); // 初始化命令词列表
        reader = new Scanner(System.in); // 初始化输入读取器
    }

    /**
     * 读取用户输入并解析为Command对象。
     * 【修改】支持多单词参数，正确处理包含空格的物品名称
     *
     * @return 解析后的Command对象，包含命令词和参数
     */
    public Command getCommand()
    {
        String inputLine;// 存储用户输入的整行字符串
        String word1 = null;
        String word2 = null;

        System.out.print("> ");// 命令提示符

        inputLine = reader.nextLine();// 读取用户输入

        // 分割输入字符串为单词
        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next(); // 第一个单词（命令词）
            if(tokenizer.hasNext()) {
                word2 = tokenizer.nextLine().trim();// 第二个单词（参数）
            }
        }

        // 检查命令词是否有效
        if(commands.isCommand(word1)) {
            return new Command(word1, word2);// 返回有效命令
        }
        else {
            return new Command(null, word2);// 返回未知命令
        }
    }

    /**
     * 显示所有可用的命令词。
     */
    public void showCommands()
    {
        commands.showAll();
    }
}

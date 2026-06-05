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
 * 封装用户输入的命令信息，包含命令词和可选参数。
 *
 * @author Michael Kölling and David J. Barnes
 * @version 1.1
 */
public class Command
{
    private String commandWord;// 命令动词
    private String secondWord;// 命令参数


    /**
     * 初始化Command对象，存储命令词和参数。
     *
     * @param firstWord 命令动词，若为null则表示未知命令
     * @param secondWord 命令参数，可选
     */
    public Command(String firstWord, String secondWord)
    {
        commandWord = firstWord;
        this.secondWord = secondWord;
    }

    /**
     * 获取命令动词。
     *
     * @return 命令词字符串，若为未知命令则返回null
     */
    public String getCommandWord()
    {
        return commandWord;
    }

    /**
     * 获取命令参数。
     *
     * @return 命令参数字符串，若无则返回null
     */
    public String getSecondWord()
    {
        return secondWord;
    }

    /**
     * 判断命令是否为未知命令。
     *
     * @return 若命令词为null则返回true，否则返回false
     */
    public boolean isUnknown()
    {
        return (commandWord == null);
    }

    /**
     * 判断命令是否包含参数。
     *
     * @return 若有参数则返回true，否则返回false
     */
    public boolean hasSecondWord()
    {
        return (secondWord != null);
    }
}

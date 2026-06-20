/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 命令接口，定义所有命令的执行行为.
 *
 * @author liujing
 * @version 1.1
 */
public interface CommandInterface {
    /**
     * 执行命令的核心方法
     * @param game 游戏实例，提供命令执行所需的上下文
     * @param secondWord 命令参数（如方向、目标等）
     * @return 命令执行是否导致游戏退出（仅QuitCommand返回true）
     */
    boolean execute(Game game, String secondWord);

    /**
     * 获取命令名称（如"go"、"quit"）
     * @return 命令词
     */
    String getCommandName();
}

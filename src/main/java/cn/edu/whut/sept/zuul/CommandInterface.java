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
 * 命令接口，定义所有命令的执行行为.
 * 新增：为实现命令模式新增的抽象接口，统一所有命令的执行逻辑。
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
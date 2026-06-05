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
 * 游戏程序的入口类，负责启动游戏实例并开始游戏流程。
 *
 * @author Michael Kölling and David J. Barnes
 * @version 1.1
 */
public class Main {
    /**
     * 程序主方法，创建Game实例并启动游戏。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        Game game = new Game();// 创建游戏实例
        game.play();// 启动游戏主循环

    }
}

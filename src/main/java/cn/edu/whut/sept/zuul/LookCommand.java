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
 * 查看当前房间详情的命令类
 * 新增：扩展命令示例，演示新增命令无需修改核心代码，
 * 仅需实现CommandInterface并注册即可。
 *
 * @author liujing
 * @version 1.1
 */
public class LookCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        System.out.println("You look around...");
        System.out.println(game.getCurrentRoom().getLongDescription());
        return false;
    }

    @Override
    public String getCommandName() {
        return "look";
    }
}
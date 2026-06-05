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
 * 处理退出游戏的命令类
 * 新增：将原Game类中的quit逻辑拆分至此，
 * 遵循单一职责原则，实现命令逻辑模块化。
 *
 * @author liujing
 * @version 1.1
 */
public class QuitCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        if (secondWord != null) {
            System.out.println("Quit what?");
            return false;
        }
        return true; // 触发游戏退出
    }

    @Override
    public String getCommandName() {
        return "quit";
    }
}
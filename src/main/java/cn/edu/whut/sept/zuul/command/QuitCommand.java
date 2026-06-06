/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 处理退出游戏的命令类
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
        return true;
    }

    @Override
    public String getCommandName() {
        return "quit";
    }
}

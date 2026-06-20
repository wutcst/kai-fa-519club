/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.1
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 查看当前房间详情的命令类
 *
 * @author liujing
 * @version 1.1
 */
public class LookCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        System.out.println("You look around...");
        System.out.println(game.getCurrentRoom().getLongDescription());
        ActionTimeCost.deduct(game, ActionTimeCost.LOOK);
        return false;
    }

    @Override
    public String getCommandName() {
        return "look";
    }
}

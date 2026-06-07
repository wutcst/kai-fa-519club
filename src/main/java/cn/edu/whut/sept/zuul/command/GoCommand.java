/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

/**
 * 处理玩家移动的命令类
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.5
 */
public class GoCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String direction) {
        if (direction == null) {
            System.out.println("Go where?");
            return false;
        }

        String[] words = direction.split(" ");
        String actualDirection = words[0];

        Room nextRoom = game.getCurrentRoom().getExit(actualDirection);
        if (nextRoom == null) {
            System.out.println("There is no door!");
        } else {
            game.setCurrentRoom(nextRoom);
            game.printLocationInfo();
            ActionTimeCost.deduct(game, ActionTimeCost.GO);
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "go";
    }
}

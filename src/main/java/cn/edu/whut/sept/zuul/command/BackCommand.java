/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.3
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;

/**
 * 处理返回上一个房间的命令类
 *
 * @author liujing
 * @version 1.3
 */
public class BackCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        if (secondWord != null) {
            System.out.println("Back what? 请仅输入 'back' 命令");
            return false;
        }

        if (game.goBack()) {
            System.out.println("你回到了上一个房间。");
            game.printLocationInfo();
        } else {
            System.out.println("无法返回，这是你的起始房间！");
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "back";
    }
}

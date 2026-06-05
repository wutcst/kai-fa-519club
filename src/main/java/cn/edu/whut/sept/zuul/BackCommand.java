/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 1.3
 */
package cn.edu.whut.sept.zuul;

/**
 * 处理返回上一个房间的命令类
 * 新增：实现back命令，允许玩家返回上一个访问的房间
 *
 * @author liujing
 * @version 1.3
 */
public class BackCommand implements CommandInterface {
    @Override
    public boolean execute(Game game, String secondWord) {
        // 检查是否有多余参数
        if (secondWord != null) {
            System.out.println("Back what? 请仅输入 'back' 命令");
            return false;
        }

        // 尝试返回上一个房间
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
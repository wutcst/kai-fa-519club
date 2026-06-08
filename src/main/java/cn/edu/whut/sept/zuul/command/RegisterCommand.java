/**
 * 命令包：各游戏命令的实现类，采用命令模式扩展。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.command;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.npc.NpcService;

/**
 * 处理 NPC 登记业务的命令类（E8）：换一卡通、领取归寝单。
 *
 * @author liujing
 * @version 1.0
 */
public class RegisterCommand implements CommandInterface {

    /**
     * help 中展示的 register 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "register - 在 NPC 处登记（超市宿管换一卡通，北楼/图书馆领归寝单）";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (NpcService.register(game)) {
            ActionTimeCost.deduct(game, ActionTimeCost.NPC);
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "register";
    }
}

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
 * 处理 NPC 对话的命令类（E8）。
 *
 * @author liujing
 * @version 1.0
 */
public class TalkCommand implements CommandInterface {

    /**
     * help 中展示的 talk 命令说明。
     *
     * @return 用法说明文本
     */
    public static String getUsageDescription() {
        return "talk - 与当前房间 NPC 对话（超市宿管、北楼志愿者、图书馆工作人员）";
    }

    @Override
    public boolean execute(Game game, String secondWord) {
        if (NpcService.talk(game)) {
            ActionTimeCost.deduct(game, ActionTimeCost.NPC);
        }
        return false;
    }

    @Override
    public String getCommandName() {
        return "talk";
    }
}

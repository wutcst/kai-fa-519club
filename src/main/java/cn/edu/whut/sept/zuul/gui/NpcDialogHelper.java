package cn.edu.whut.sept.zuul.gui;

import java.util.Collections;
import java.util.List;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.multiplayer.OutputCapture;
import cn.edu.whut.sept.zuul.npc.NpcService;

/**
 * GUI 专用 NPC 对话：调用 {@link NpcService}，不经过 TalkCommand 的额外罚时（F7 阶段 2）。
 */
public final class NpcDialogHelper {

    private NpcDialogHelper() {
    }

    /**
     * 当前房间是否应显示 NPC 立绘与 talk 按钮。
     *
     * @param roomId 房间 ID
     * @param level 当前关卡
     * @return 是否显示
     */
    public static boolean shouldShowNpc(String roomId, int level) {
        if (roomId == null) {
            return false;
        }
        if (UseCommand.SUPERMARKET_ROOM_ID.equals(roomId)) {
            return true;
        }
        if (NpcService.NORTH_BUILDING_ROOM_ID.equals(roomId)) {
            return true;
        }
        if (UseCommand.LIBRARY_ROOM_ID.equals(roomId)) {
            return level >= 4;
        }
        return false;
    }

    /**
     * 执行对话并返回输出文案（不扣 NPC 30 秒罚时）。
     *
     * @param game 游戏实例
     * @return 对话行列表
     */
    public static List<String> performTalk(Game game) {
        if (game == null) {
            return Collections.singletonList("无法对话。");
        }
        try (OutputCapture capture = OutputCapture.start()) {
            NpcService.talk(game);
            List<String> lines = capture.getLines();
            if (lines.isEmpty()) {
                return Collections.singletonList("（对方没有说话。）");
            }
            return lines;
        }
    }

    /**
     * 是否可在教育超市提交归寝单。
     *
     * @param game 游戏实例
     * @return 是否显示 submit
     */
    public static boolean canSubmitAtSupermarket(Game game) {
        if (game == null || game.getCurrentRoom() == null) {
            return false;
        }
        if (!UseCommand.SUPERMARKET_ROOM_ID.equals(game.getCurrentRoom().getRoomId())) {
            return false;
        }
        return game.getLevelManager().getCurrentLevelConfig().requiresDormitorySubmit()
            && !game.getLevelManager().isDormitorySubmitCompleted();
    }
}

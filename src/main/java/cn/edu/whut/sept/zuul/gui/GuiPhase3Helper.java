package cn.edu.whut.sept.zuul.gui;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.LevelManager;
import cn.edu.whut.sept.zuul.npc.NpcService;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * F7 阶段 3：黑暗主楼、西楼困锁、喂猫、门禁与密码、传送等 GUI 规则。
 */
public final class GuiPhase3Helper {

    private static final String TELEPORT_SNIPPET = "突然被传送";

    private GuiPhase3Helper() {
    }

    public static boolean hasAllCombineMaterials(Player player) {
        if (player == null) {
            return false;
        }
        return player.findItemInInventory(CombineCommand.STICK_ITEM) != null
            && player.findItemInInventory(CombineCommand.STONE_ITEM) != null
            && player.findItemInInventory(CombineCommand.ROPE_ITEM) != null
            && player.findItemInInventory(UseCommand.HAMMER_ITEM) == null;
    }

    public static boolean shouldOfferCombinePrompt(Game game) {
        return hasAllCombineMaterials(game.getPlayer());
    }

    public static boolean shouldShowCombineButton(Game game) {
        if (game == null || game.getCurrentRoom() == null) {
            return false;
        }
        return UseCommand.WEST_BUILDING_ROOM_ID.equals(game.getCurrentRoom().getRoomId())
            && hasAllCombineMaterials(game.getPlayer());
    }

    public static boolean shouldShowFeedButton(Game game) {
        if (game == null || game.getCurrentRoom() == null) {
            return false;
        }
        return game.getLevelManager().getCurrentLevel() >= FeedCommand.MIN_FEED_LEVEL
            && NpcService.NORTH_BUILDING_ROOM_ID.equals(game.getCurrentRoom().getRoomId());
    }

    public static boolean shouldShowUnlockButton(Game game) {
        if (game == null || game.getCurrentRoom() == null) {
            return false;
        }
        return game.getLevelManager().getCurrentLevel() == 5
            && UnlockService.DORMITORY_ROOM_ID.equals(game.getCurrentRoom().getRoomId())
            && !game.getLevelManager().isDormitoryPasswordUnlocked();
    }

    public static boolean isWestBuildingTrapped(Game game) {
        return game != null && game.isTrappedInWestBuilding();
    }

    public static String westTrapBannerText(Game game) {
        if (!isWestBuildingTrapped(game)) {
            return null;
        }
        return LevelManager.WEST_BUILDING_TRAP_MESSAGE;
    }

    public static boolean outputIndicatesDarkPenalty(Iterable<String> lines) {
        return containsSnippet(lines, DarkRoom.PENALTY_MESSAGE);
    }

    public static boolean outputIndicatesTeleport(Iterable<String> lines) {
        return containsSnippet(lines, TELEPORT_SNIPPET);
    }

    public static String findGatedDenialMessage(Iterable<String> lines) {
        if (lines == null) {
            return null;
        }
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            if (line.contains(GatedRoom.LIBRARY_CARD_DENIED_MESSAGE)
                || line.contains(GatedRoom.CARD_DENIED_MESSAGE)
                || line.contains(GatedRoom.SUBMIT_REQUIRED_MESSAGE)) {
                return line;
            }
        }
        return null;
    }

    /**
     * 三材齐全时弹窗询问是否合成。
     *
     * @param dialogs 玻璃模态层
     * @param game 游戏实例
     * @param combineAction 确认后执行合成
     */
    public static void promptCombineIfReady(GlassModalLayer dialogs, Game game, Runnable combineAction) {
        if (dialogs == null || !shouldOfferCombinePrompt(game)) {
            return;
        }
        Room room = game.getCurrentRoom();
        boolean inWest = room != null
            && UseCommand.WEST_BUILDING_ROOM_ID.equals(room.getRoomId());
        String message = inWest
            ? "背包已有棍子、石头、绳子，是否合成锤子？"
            : "背包已有棍子、石头、绳子。须在西楼内才能合成，现在尝试合成吗？";
        dialogs.showConfirm(
            "合成锤子",
            message,
            "合成",
            "稍后",
            combineAction,
            null
        );
    }

    private static boolean containsSnippet(Iterable<String> lines, String snippet) {
        if (lines == null || snippet == null) {
            return false;
        }
        for (String line : lines) {
            if (line != null && line.contains(snippet)) {
                return true;
            }
        }
        return false;
    }
}

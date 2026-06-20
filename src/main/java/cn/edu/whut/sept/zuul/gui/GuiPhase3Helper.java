package cn.edu.whut.sept.zuul.gui;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.LevelManager;
import cn.edu.whut.sept.zuul.npc.NpcService;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 黑暗主楼、西楼困锁、喂猫、门禁与密码、传送等 Vue 界面规则辅助。
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

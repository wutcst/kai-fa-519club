package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.List;
import java.util.stream.Collectors;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.gui.GuiPhase3Helper;
import cn.edu.whut.sept.zuul.gui.NpcDialogHelper;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ExitAvailabilityDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.GameStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ItemViewDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.RoomChatMessageDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.UiActionFlagsDto;
import cn.edu.whut.sept.zuul.multiplayer.GameRoom;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;

/**
 * 联机 Vue 视图状态映射（对齐单机 HUD 字段）。
 */
public final class MultiplayerViewMapper {

    private static final String CAT_PHOTO_ITEM = "一张猫学长的照片";
    private static final GameGuiController GUI_CONTROLLER = new GameGuiController();

    private MultiplayerViewMapper() {
    }

    public static GameStateDto toDto(GameRoom room, GameStateSnapshot snapshot, String playerId) {
        GameStateDto dto = GameStateDto.from(snapshot);
        if (room == null || snapshot == null) {
            return dto;
        }
        Game game = room.getGame();
        game.setActiveOnlinePlayer(playerId);
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();
        dto.setRoomDescription(currentRoom == null ? "" : currentRoom.getShortDescription());
        dto.setBulletin(GUI_CONTROLLER.buildBulletinText(game));
        dto.setWestTrapBanner(GuiPhase3Helper.westTrapBannerText(game));
        dto.setRoomItems(mapRoomItems(currentRoom == null ? List.of() : currentRoom.getItems()));
        dto.setInventory(mapItems(player.getInventory()));
        dto.setInventoryWeight(player.getCurrentWeight());
        dto.setMaxInventoryWeight(player.getMaxWeight());
        dto.setRemainingCapacity(player.getRemainingCapacity());
        dto.setExits(buildExits(game, currentRoom));
        dto.setActions(buildActions(game, currentRoom));
        dto.setChatMessages(room.getChatMessages().stream()
            .map(RoomChatMessageDto::from)
            .collect(Collectors.toList()));
        return dto;
    }

    public static String buildNoticeMessage(String commandWord, List<String> lines) {
        if (!shouldShowCommandPopup(commandWord, lines)) {
            return null;
        }
        String popup = buildPopupMessage(lines);
        return popup.isEmpty() ? null : popup;
    }

    private static List<ItemViewDto> mapRoomItems(List<Item> items) {
        return items.stream()
            .filter(item -> !CAT_PHOTO_ITEM.equals(item.getDescription()))
            .map(MultiplayerViewMapper::toItemView)
            .collect(Collectors.toList());
    }

    private static List<ItemViewDto> mapItems(List<Item> items) {
        return items.stream()
            .map(MultiplayerViewMapper::toItemView)
            .collect(Collectors.toList());
    }

    private static ItemViewDto toItemView(Item item) {
        return new ItemViewDto(
            item.getDescription(),
            item.getWeight(),
            item.getLongDescription(),
            FoodItems.isEdible(item.getDescription()));
    }

    private static ExitAvailabilityDto buildExits(Game game, Room room) {
        ExitAvailabilityDto exits = new ExitAvailabilityDto();
        if (room == null) {
            return exits;
        }
        boolean trapped = game.isTrappedInWestBuilding();
        exits.setNorth(hasExit(room, "north"));
        exits.setSouth(hasExit(room, "south"));
        exits.setEast(hasExit(room, "east") && !trapped);
        exits.setWest(hasExit(room, "west"));
        exits.setBack(!trapped);
        return exits;
    }

    private static UiActionFlagsDto buildActions(Game game, Room room) {
        UiActionFlagsDto actions = new UiActionFlagsDto();
        if (room == null) {
            return actions;
        }
        int level = game.getLevelManager().getCurrentLevel();
        actions.setShowNpc(NpcDialogHelper.shouldShowNpc(room.getRoomId(), level));
        actions.setShowFeed(GuiPhase3Helper.shouldShowFeedButton(game));
        actions.setShowCombine(GuiPhase3Helper.shouldShowCombineButton(game));
        actions.setShowUnlock(GuiPhase3Helper.shouldShowUnlockButton(game));
        actions.setShowSleep(cn.edu.whut.sept.zuul.unlock.UnlockService.DORMITORY_ROOM_ID
            .equals(room.getRoomId()));
        actions.setShowSubmit(NpcDialogHelper.canSubmitAtSupermarket(game));
        return actions;
    }

    private static boolean hasExit(Room room, String direction) {
        return room.getExit(direction) != null;
    }

    private static boolean shouldShowCommandPopup(String commandWord, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        String cmd = commandWord == null ? "" : commandWord.trim().toLowerCase();
        if ("go".equals(cmd) || "back".equals(cmd) || "look".equals(cmd)) {
            return false;
        }
        if ("take".equals(cmd)) {
            return lines.stream().anyMatch(MultiplayerViewMapper::isTakeFailureLine);
        }
        if ("drop".equals(cmd)) {
            return false;
        }
        return true;
    }

    private static boolean isTakeFailureLine(String line) {
        if (line == null) {
            return false;
        }
        return line.contains("无法拾取")
            || line.contains("这个房间里没有")
            || line.contains("Take what?");
    }

    private static String buildPopupMessage(List<String> lines) {
        return lines.stream()
            .filter(line -> line != null && !line.isBlank())
            .filter(line -> !isWeightStatusLine(line))
            .filter(line -> !line.startsWith("你拾取了:"))
            .filter(line -> !line.startsWith("You look around"))
            .collect(Collectors.joining("\n"));
    }

    private static boolean isWeightStatusLine(String line) {
        return line.contains("剩余负重:")
            || line.contains("当前负重:")
            || line.contains("当前最大负重:");
    }
}

package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.gui.GuiOutcomeHelper;
import cn.edu.whut.sept.zuul.gui.GuiPhase3Helper;
import cn.edu.whut.sept.zuul.gui.NpcDialogHelper;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.CreateSoloSessionRequest;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ExitAvailabilityDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.ItemViewDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.OutcomeOverlayDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloCommandResponseDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloSessionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloViewStateDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.UiActionFlagsDto;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 单机五关 GUI 门面：供 Vue 客户端 REST 调用。
 */
@Service
public class SinglePlayerGuiService {

    /** 猫学长照片为场景装饰，不在 Vue 中作为可拾取物品展示 */
    private static final String CAT_PHOTO_ITEM = "一张猫学长的照片";

    private final SinglePlayerSessionRegistry sessionRegistry;

    public SinglePlayerGuiService(SinglePlayerSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public SoloSessionDto createSession(CreateSoloSessionRequest request) {
        String playerName = request == null ? null : request.getPlayerName();
        SinglePlayerSession session = sessionRegistry.createSession(playerName);
        SoloSessionDto dto = new SoloSessionDto();
        dto.setSessionId(session.getSessionId());
        dto.setState(buildViewState(session));
        return dto;
    }

    public SoloViewStateDto getViewState(String sessionId) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            detectTimeoutOutcome(session);
            return buildViewState(session);
        }
    }

    public SoloCommandResponseDto executeCommand(String sessionId, String commandWord, String secondWord) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            if (session.getPendingOutcome() != null) {
                if ("restart".equalsIgnoreCase(commandWord)) {
                    session.getController().execute(session.getGame(), "restart", null);
                    session.setPendingOutcome(null);
                    session.setPendingLockedOverlay(null);
                    session.setTrackedLevelState(session.getGame().getLevelManager().getState());
                } else {
                    SoloCommandResponseDto blocked = new SoloCommandResponseDto();
                    blocked.setPopupMessage("请先处理当前结局弹层。");
                    blocked.setState(buildViewState(session));
                    return blocked;
                }
            }

            GameGuiController.CommandResult result = session.getController().execute(
                session.getGame(), commandWord, secondWord);

            SoloCommandResponseDto response = new SoloCommandResponseDto();
            response.setMessages(result.getOutputLines());

            detectOutcomeFromOutput(session, result.getOutputLines());
            if (session.getPendingOutcome() == null) {
                applyCommandEffects(session, result, response, commandWord);
            } else {
                session.setPendingLockedOverlay(null);
            }
            detectTimeoutOutcome(session);
            response.setState(buildViewState(session));
            return response;
        }
    }

    public SoloCommandResponseDto performLook(String sessionId) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            Game game = session.getGame();
            GameGuiController controller = session.getController();
            String bulletin = controller.buildBulletinText(game);
            GameGuiController.CommandResult result = controller.execute(game, "look", null);

            SoloCommandResponseDto response = new SoloCommandResponseDto();
            response.setMessages(result.getOutputLines());
            response.setPopupMessage(bulletin);
            detectTimeoutOutcome(session);
            response.setState(buildViewState(session));
            return response;
        }
    }

    public SoloCommandResponseDto performTalk(String sessionId) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            List<String> lines = NpcDialogHelper.performTalk(session.getGame());
            SoloCommandResponseDto response = new SoloCommandResponseDto();
            response.setMessages(lines);
            response.setPopupMessage(String.join("\n", lines));
            detectTimeoutOutcome(session);
            response.setState(buildViewState(session));
            return response;
        }
    }

    public SoloViewStateDto dismissOutcome(String sessionId) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            OutcomeOverlayDto outcome = session.getPendingOutcome();
            if (outcome != null && "LEVEL_FAILED".equals(outcome.getType())) {
                session.getController().execute(session.getGame(), "restart", null);
            }
            session.setPendingOutcome(null);
            session.setTrackedLevelState(session.getGame().getLevelManager().getState());
            return buildViewState(session);
        }
    }

    public SoloViewStateDto dismissLockedOverlay(String sessionId) {
        SinglePlayerSession session = requireSession(sessionId);
        synchronized (session) {
            session.setPendingLockedOverlay(null);
            return buildViewState(session);
        }
    }

    public void destroySession(String sessionId) {
        sessionRegistry.removeSession(sessionId);
    }

    public void clearAllSessionsForTest() {
        sessionRegistry.clearAllForTest();
    }

    private void applyCommandEffects(
        SinglePlayerSession session,
        GameGuiController.CommandResult result,
        SoloCommandResponseDto response,
        String commandWord) {
        if (result.isLockedExitAttempt()) {
            session.setPendingLockedOverlay(LevelConfig.LOCKED_EXIT_MESSAGE);
        } else if ("go".equalsIgnoreCase(commandWord)) {
            session.setPendingLockedOverlay(null);
        }

        if (result.isDarkPenaltyTriggered()) {
            response.setPopupMessage(DarkRoom.PENALTY_MESSAGE);
        } else if (result.getGatedDenialMessage() != null) {
            response.setPopupMessage(result.getGatedDenialMessage());
        } else if (result.isTeleported()) {
            response.setPopupMessage("你进入体育馆后被传送到校园另一处！");
        } else if (shouldShowCommandPopup(commandWord, result.getOutputLines())) {
            String popup = buildPopupMessage(result.getOutputLines());
            if (!popup.isEmpty()) {
                response.setPopupMessage(popup);
            }
        }

        if ("take".equalsIgnoreCase(commandWord)
            && GuiPhase3Helper.shouldOfferCombinePrompt(session.getGame())) {
            response.setCombinePrompt(true);
        }
    }

    private void detectOutcomeFromOutput(SinglePlayerSession session, List<String> lines) {
        GuiOutcomeHelper.OutcomeType type = GuiOutcomeHelper.detectFromOutput(lines);
        if (type == GuiOutcomeHelper.OutcomeType.NONE) {
            return;
        }
        session.setPendingOutcome(buildOutcomeDto(session, type, lines));
        session.setPendingLockedOverlay(null);
    }

    private void detectTimeoutOutcome(SinglePlayerSession session) {
        LevelState current = session.getGame().getLevelManager().getState();
        GuiOutcomeHelper.OutcomeType transition = GuiOutcomeHelper.detectFromStateTransition(
            session.getTrackedLevelState(), current);
        if (transition != GuiOutcomeHelper.OutcomeType.NONE
            && session.getPendingOutcome() == null) {
            session.setPendingOutcome(buildOutcomeDto(
                session, transition, List.of(GuiOutcomeHelper.FAIL_SNIPPET + "。")));
        }
        session.setTrackedLevelState(current);
    }

    private OutcomeOverlayDto buildOutcomeDto(
        SinglePlayerSession session,
        GuiOutcomeHelper.OutcomeType type,
        List<String> lines) {
        OutcomeOverlayDto dto = new OutcomeOverlayDto();
        dto.setType(type.name());
        dto.setTitle(GuiOutcomeHelper.buildTitle(type));
        dto.setMessage(GuiOutcomeHelper.buildMessage(type, session.getGame(), lines));
        dto.setActionLabel(GuiOutcomeHelper.buildActionLabel(type));
        return dto;
    }

    private SoloViewStateDto buildViewState(SinglePlayerSession session) {
        Game game = session.getGame();
        Room room = game.getCurrentRoom();
        SoloViewStateDto dto = new SoloViewStateDto();
        dto.setSessionId(session.getSessionId());
        dto.setLevel(game.getLevelManager().getCurrentLevel());
        dto.setLevelTitle(session.getController().buildLevelTitle(game));
        dto.setLevelState(game.getLevelManager().getState().name());
        dto.setRemainingSeconds(game.getLevelTimer().getRemainingSeconds());
        dto.setTimerText(game.getLevelTimer().getDisplayText());
        dto.setRoomId(room == null ? "gate" : room.getRoomId());
        dto.setRoomDescription(room == null ? "" : room.getShortDescription());
        dto.setWestTrapBanner(GuiPhase3Helper.westTrapBannerText(game));
        dto.setRoomItems(mapRoomItems(room == null ? List.of() : room.getItems()));
        Player player = game.getPlayer();
        dto.setInventory(mapItems(player.getInventory()));
        dto.setInventoryWeight(player.getCurrentWeight());
        dto.setMaxInventoryWeight(player.getMaxWeight());
        dto.setRemainingCapacity(player.getRemainingCapacity());
        dto.setExits(buildExits(game, room));
        dto.setActions(buildActions(game, room));
        dto.setOutcome(session.getPendingOutcome());
        dto.setLockedOverlayMessage(session.getPendingLockedOverlay());
        dto.setInteractionBlocked(session.getPendingOutcome() != null);
        return dto;
    }

    private List<ItemViewDto> mapRoomItems(List<Item> items) {
        return items.stream()
            .filter(item -> !CAT_PHOTO_ITEM.equals(item.getDescription()))
            .map(this::toItemView)
            .collect(Collectors.toList());
    }

    private List<ItemViewDto> mapItems(List<Item> items) {
        return items.stream()
            .map(this::toItemView)
            .collect(Collectors.toList());
    }

    private ItemViewDto toItemView(Item item) {
        return new ItemViewDto(
            item.getDescription(),
            item.getWeight(),
            item.getLongDescription(),
            FoodItems.isEdible(item.getDescription()));
    }

    private boolean shouldShowCommandPopup(String commandWord, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        String cmd = commandWord == null ? "" : commandWord.trim().toLowerCase();
        if ("go".equals(cmd) || "back".equals(cmd)) {
            return false;
        }
        if ("take".equals(cmd)) {
            return lines.stream().anyMatch(this::isTakeFailureLine);
        }
        if ("drop".equals(cmd)) {
            return false;
        }
        return true;
    }

    private boolean isTakeFailureLine(String line) {
        if (line == null) {
            return false;
        }
        return line.contains("无法拾取")
            || line.contains("这个房间里没有")
            || line.contains("Take what?");
    }

    private String buildPopupMessage(List<String> lines) {
        return lines.stream()
            .filter(line -> line != null && !line.isBlank())
            .filter(line -> !isWeightStatusLine(line))
            .filter(line -> !line.startsWith("你拾取了:"))
            .collect(Collectors.joining("\n"));
    }

    private boolean isWeightStatusLine(String line) {
        return line.contains("剩余负重:")
            || line.contains("当前负重:")
            || line.contains("当前最大负重:");
    }

    private ExitAvailabilityDto buildExits(Game game, Room room) {
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

    private UiActionFlagsDto buildActions(Game game, Room room) {
        UiActionFlagsDto actions = new UiActionFlagsDto();
        if (room == null) {
            return actions;
        }
        int level = game.getLevelManager().getCurrentLevel();
        actions.setShowNpc(NpcDialogHelper.shouldShowNpc(room.getRoomId(), level));
        actions.setShowFeed(GuiPhase3Helper.shouldShowFeedButton(game));
        actions.setShowCombine(GuiPhase3Helper.shouldShowCombineButton(game));
        actions.setShowUnlock(GuiPhase3Helper.shouldShowUnlockButton(game));
        actions.setShowSleep(UnlockService.DORMITORY_ROOM_ID.equals(room.getRoomId()));
        actions.setShowSubmit(NpcDialogHelper.canSubmitAtSupermarket(game));
        return actions;
    }

    private boolean hasExit(Room room, String direction) {
        return room.getExit(direction) != null;
    }

    private SinglePlayerSession requireSession(String sessionId) {
        SinglePlayerSession session = sessionRegistry.findSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("单机会话不存在或已过期");
        }
        return session;
    }
}

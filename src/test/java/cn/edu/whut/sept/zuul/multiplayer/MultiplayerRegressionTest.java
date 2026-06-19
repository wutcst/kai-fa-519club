package cn.edu.whut.sept.zuul.multiplayer;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.gui.GameGuiController;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.level.TimerAuthority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * T6 联机与回归测试（自动化）：双人在线、移动同步、服务端计时、单机第 1 关回归。
 */
public class MultiplayerRegressionTest {

    private GameRoomRegistry registry;
    private GameEngineFacade facade;

    @Before
    public void setUp() {
        registry = new GameRoomRegistry();
        facade = new GameEngineFacade();
    }

    /**
     * T6-REG-01：联机改动后，单机第 1 关主路径仍可通关。
     */
    @Test
    public void regressionSoloLevelOneStillPassable() {
        Game game = new Game();
        GameGuiController controller = new GameGuiController();

        controller.execute(game, "go", "north");
        controller.execute(game, "go", "north");
        controller.execute(game, "take", UseCommand.MONEY_ITEM);
        controller.execute(game, "go", "west");
        controller.execute(game, "use", UseCommand.MONEY_ITEM);
        assertNotNull(game.getPlayer().findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
        controller.execute(game, "go", "north");
        controller.execute(game, "sleep", null);

        assertEquals(2, game.getLevelManager().getCurrentLevel());
        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        controller.shutdownGuiSession(game);
    }

    /**
     * T6-REG-02：≥2 客户端同时在线，房主移动后位置独立、计时以服务端为准。
     */
    @Test
    public void regressionTwoClientsOnlineWithIndependentPositions() {
        GameRoom room = registry.createRoom("T6回归房", "房主", 1L);
        String hostId = room.getHostPlayerId();
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "队员", 2L);
        room.setInGame(true);
        room.getGame().getLevelManager().startLevel(1);

        assertEquals(2, room.getPlayerCount());
        assertEquals(TimerAuthority.SERVER_HOST, room.getGame().getLevelTimer().getTimerAuthority());

        int sharedBefore = facade.getState(room, hostId).getRemainingSeconds();
        facade.executeCommand(room, hostId, "go", "north");

        GameStateSnapshot hostState = facade.getState(room, hostId);
        GameStateSnapshot guestState = facade.getState(room, guest.getPlayerId());

        assertEquals("boxue_main", hostState.getRoomId());
        assertEquals("gate", guestState.getRoomId());
        assertEquals(sharedBefore - ActionTimeCost.GO, hostState.getRemainingSeconds());
        assertEquals(hostState.getRemainingSeconds(), guestState.getRemainingSeconds());
        assertNotEquals(hostState.getRoomId(), guestState.getRoomId());
    }

    /**
     * T6-REG-03：联机客户端应用服务端秒数，本地 tick 不覆盖权威计时。
     */
    @Test
    public void regressionClientUsesServerTimerAuthority() {
        Game clientGame = new Game();
        clientGame.startMultiplayerSession();
        clientGame.addOnlinePlayer("客户端");
        clientGame.useServerTimerAuthority();
        clientGame.getLevelTimer().setAutoTickEnabled(true);

        clientGame.applyServerRemainingSeconds(188);

        assertEquals(TimerAuthority.SERVER_CLIENT, clientGame.getLevelTimer().getTimerAuthority());
        assertEquals(188, clientGame.getLevelTimer().getRemainingSeconds());
        assertTrue(clientGame.getLevelTimer().isAutoTickEnabled());
    }

    /**
     * T6-REG-04：联机房间开局后，共享世界状态可通过快照同步到客户端视图。
     */
    @Test
    public void regressionClientViewSyncFromMultiplayerSnapshot() {
        GameRoom room = registry.createRoom("快照同步", "展示者", 1L);
        registry.joinRoom(room.getRoomId(), "队友", 2L);
        room.setInGame(true);
        String hostId = room.getHostPlayerId();

        facade.executeCommand(room, hostId, "go", "north");
        GameStateSnapshot snapshot = facade.getState(room, hostId);

        Game clientGame = new Game();
        clientGame.syncClientViewFromSnapshot(snapshot, hostId);

        assertEquals(snapshot.getRoomId(), clientGame.getCurrentRoom().getRoomId());
        assertEquals(snapshot.getRemainingSeconds(), clientGame.getLevelTimer().getRemainingSeconds());
        assertEquals(snapshot.getLevel(), clientGame.getLevelManager().getCurrentLevel());
    }

    /**
     * T6-REG-05：联机房间内房主跑通第 1 关，全员共享关卡与计时。
     */
    @Test
    public void regressionMultiplayerLevelOneCompleteInSharedRoom() {
        GameRoom room = registry.createRoom("L1通关房", "房主", 1L);
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "队员", 2L);
        room.setInGame(true);
        String hostId = room.getHostPlayerId();

        facade.executeCommand(room, hostId, "go", "north");
        facade.executeCommand(room, hostId, "go", "north");
        facade.executeCommand(room, hostId, "take", UseCommand.MONEY_ITEM);
        facade.executeCommand(room, hostId, "go", "west");
        facade.executeCommand(room, hostId, "use", UseCommand.MONEY_ITEM);
        facade.executeCommand(room, hostId, "go", "north");
        facade.executeCommand(room, hostId, "sleep", null);

        GameStateSnapshot hostState = facade.getState(room, hostId);
        GameStateSnapshot guestState = facade.getState(room, guest.getPlayerId());

        assertEquals(2, hostState.getLevel());
        assertEquals(LevelState.IN_PROGRESS.name(), hostState.getLevelState());
        assertEquals(hostState.getLevel(), guestState.getLevel());
        assertEquals(hostState.getRemainingSeconds(), guestState.getRemainingSeconds());
    }
}

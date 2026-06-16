package cn.edu.whut.sept.zuul.multiplayer;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.TimerAuthority;
import cn.edu.whut.sept.zuul.multiplayer.MultiplayerConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * F6 联机核心逻辑单元测试（不启动 HTTP 服务端）。
 */
public class MultiplayerGameTest {

    private GameRoomRegistry registry;
    private GameEngineFacade facade;

    @Before
    public void setUp() {
        registry = new GameRoomRegistry();
        facade = new GameEngineFacade();
    }

    @Test
    public void testTwoPlayersCanJoinSameRoom() {
        GameRoom room = registry.createRoom("测试房", "玩家A");
        String hostId = room.getHostPlayerId();
        JoinRoomResult joinResult = registry.joinRoom(room.getRoomId(), "玩家B");
        assertEquals(2, room.getPlayerCount());
        assertNotEquals(hostId, joinResult.getPlayerId());
    }

    @Test
    public void testPlayersHaveIndependentPositions() {
        GameRoom room = registry.createRoom("位置测试", "甲");
        String hostId = room.getHostPlayerId();
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "乙");

        facade.executeCommand(room, hostId, "go", "north");
        GameStateSnapshot hostView = facade.getState(room, hostId);
        GameStateSnapshot guestView = facade.getState(room, guest.getPlayerId());

        assertEquals("boxue_main", hostView.getRoomId());
        assertEquals("gate", guestView.getRoomId());
    }

    @Test
    public void testServerHostTimerAuthorityEnabled() {
        GameRoom room = registry.createRoom("计时测试", "房主");
        assertEquals(TimerAuthority.SERVER_HOST, room.getGame().getLevelTimer().getTimerAuthority());
        assertTrue(room.getGame().getLevelTimer().isAutoTickEnabled());
    }

    @Test
    public void testSharedWorldItemTakenByOnePlayer() {
        GameRoom room = registry.createRoom("物品测试", "拾取者");
        JoinRoomResult other = registry.joinRoom(room.getRoomId(), "旁观者");
        String takerId = room.getHostPlayerId();

        facade.executeCommand(room, takerId, "go", "north");
        facade.executeCommand(room, takerId, "go", "north");
        facade.executeCommand(room, takerId, "take", "湿漉漉的三十元钱");

        GameStateSnapshot takerState = facade.getState(room, takerId);
        PlayerStateSnapshot taker = findPlayer(takerState, takerId);
        assertTrue(taker.getInventory().stream()
            .anyMatch(name -> name.contains("三十元") || name.contains("湿漉漉")));

        GameStateSnapshot otherState = facade.getState(room, other.getPlayerId());
        assertEquals("gate", otherState.getRoomId());
    }

    @Test
    public void testApplyServerRemainingSecondsOnClientGame() {
        Game clientGame = new Game();
        clientGame.startMultiplayerSession();
        clientGame.addOnlinePlayer("客户端");
        clientGame.useServerTimerAuthority();
        clientGame.getLevelTimer().setAutoTickEnabled(true);
        clientGame.applyServerRemainingSeconds(100);
        assertEquals(100, clientGame.getLevelTimer().getRemainingSeconds());
        assertEquals(TimerAuthority.SERVER_CLIENT, clientGame.getLevelTimer().getTimerAuthority());
    }

    @Test
    public void testLeaveRoomRemovesPlayer() {
        GameRoom room = registry.createRoom("离开测试", "房主");
        String hostId = room.getHostPlayerId();
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "客人");

        LeaveRoomResult leaveGuest = registry.leaveRoom(room.getRoomId(), guest.getPlayerId());
        assertEquals(room.getRoomId(), leaveGuest.getRoomId());
        assertTrue(!leaveGuest.isRoomRemoved());
        assertEquals(1, room.getPlayerCount());

        LeaveRoomResult leaveHost = registry.leaveRoom(room.getRoomId(), hostId);
        assertTrue(leaveHost.isRoomRemoved());
        assertTrue(registry.listRooms().isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testJoinRoomRejectsWhenFull() {
        GameRoom room = registry.createRoom("满员测试", "玩家1");
        for (int index = 2; index <= MultiplayerConfig.MAX_PLAYERS_PER_ROOM; index++) {
            registry.joinRoom(room.getRoomId(), "玩家" + index);
        }
        registry.joinRoom(room.getRoomId(), "多余玩家");
    }

    @Test
    public void testSyncClientViewFromSnapshot() {
        GameRoom room = registry.createRoom("同步测试", "展示者");
        String playerId = room.getHostPlayerId();
        facade.executeCommand(room, playerId, "go", "north");
        GameStateSnapshot snapshot = facade.getState(room, playerId);

        Game clientGame = new Game();
        clientGame.syncClientViewFromSnapshot(snapshot, playerId);
        assertEquals("boxue_main", clientGame.getCurrentRoom().getRoomId());
        assertEquals(snapshot.getRemainingSeconds(), clientGame.getLevelTimer().getRemainingSeconds());
        assertEquals(snapshot.getLevel(), clientGame.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testServerTimerDecreasesOverTime() throws InterruptedException {
        GameRoom room = registry.createRoom("计时递减", "房主");
        int before = facade.getState(room, room.getHostPlayerId()).getRemainingSeconds();
        Thread.sleep(1500);
        int after = facade.getState(room, room.getHostPlayerId()).getRemainingSeconds();
        assertTrue("服务端计时应递减", after < before);
    }

    @Test
    public void testMultiplayerGoCommandDeductsActionTime() {
        GameRoom room = registry.createRoom("联机罚时", "房主");
        String hostId = room.getHostPlayerId();
        int before = facade.getState(room, hostId).getRemainingSeconds();
        facade.executeCommand(room, hostId, "go", "north");
        int after = facade.getState(room, hostId).getRemainingSeconds();
        assertEquals(before - ActionTimeCost.GO, after);
    }

    private PlayerStateSnapshot findPlayer(GameStateSnapshot state, String playerId) {
        for (PlayerStateSnapshot player : state.getPlayers()) {
            if (playerId.equals(player.getPlayerId())) {
                return player;
            }
        }
        throw new AssertionError("未找到玩家: " + playerId);
    }
}

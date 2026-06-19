package cn.edu.whut.sept.zuul.multiplayer;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.TimerAuthority;
import cn.edu.whut.sept.zuul.multiplayer.MultiplayerConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        GameRoom room = registry.createRoom("测试房", "玩家A", 1L);
        String hostId = room.getHostPlayerId();
        JoinRoomResult joinResult = registry.joinRoom(room.getRoomId(), "玩家B", 2L);
        assertEquals(2, room.getPlayerCount());
        assertNotEquals(hostId, joinResult.getPlayerId());
    }

    @Test
    public void testPlayersHaveIndependentPositions() {
        GameRoom room = registry.createRoom("位置测试", "甲", 1L);
        String hostId = room.getHostPlayerId();
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "乙", 2L);

        facade.executeCommand(room, hostId, "go", "north");
        GameStateSnapshot hostView = facade.getState(room, hostId);
        GameStateSnapshot guestView = facade.getState(room, guest.getPlayerId());

        assertEquals("boxue_main", hostView.getRoomId());
        assertEquals("gate", guestView.getRoomId());
    }

    @Test
    public void testServerHostTimerAuthorityEnabled() {
        GameRoom room = registry.createRoom("计时测试", "房主", 1L);
        assertEquals(TimerAuthority.SERVER_HOST, room.getGame().getLevelTimer().getTimerAuthority());
        assertTrue(room.getGame().getLevelTimer().isAutoTickEnabled());
    }

    @Test
    public void testRestartSameLevelClearsAllPlayersInventory() {
        GameRoom room = registry.createRoom("同关重开", "玩家A", 1L);
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "玩家B", 2L);
        Game game = room.getGame();
        String hostId = room.getHostPlayerId();

        game.setActiveOnlinePlayer(hostId);
        game.getPlayer().takeItem(new Item("房主物品", 10));
        game.setActiveOnlinePlayer(guest.getPlayerId());
        game.getPlayer().takeItem(new Item("队员物品", 10));

        game.getLevelManager().startLevel(1);

        for (Player player : game.getOnlinePlayers().values()) {
            assertTrue("同关重开后背包应清空: " + player.getName(),
                player.getInventory().isEmpty());
        }
    }

    @Test
    public void testCompleteLevelClearsAllPlayersInventory() {
        GameRoom room = registry.createRoom("背包清空", "玩家A", 1L);
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "玩家B", 2L);
        Game game = room.getGame();
        String hostId = room.getHostPlayerId();

        game.setActiveOnlinePlayer(hostId);
        game.getPlayer().takeItem(new Item("房主物品", 10));
        game.setActiveOnlinePlayer(guest.getPlayerId());
        game.getPlayer().takeItem(new Item("队员物品", 10));

        game.getLevelManager().completeCurrentLevel();

        for (Player player : game.getOnlinePlayers().values()) {
            assertTrue("通关进下一关后背包应清空: " + player.getName(),
                player.getInventory().isEmpty());
        }
        assertEquals(2, game.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testSharedWorldItemTakenByOnePlayer() {
        GameRoom room = registry.createRoom("物品测试", "拾取者", 1L);
        JoinRoomResult other = registry.joinRoom(room.getRoomId(), "旁观者", 2L);
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
        GameRoom room = registry.createRoom("离开测试", "房主", 1L);
        String hostId = room.getHostPlayerId();
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "客人", 2L);

        LeaveRoomResult leaveGuest = registry.leaveRoom(
            room.getRoomId(), guest.getPlayerId(), LeaveRoomAction.LEAVE, null);
        assertEquals(room.getRoomId(), leaveGuest.getRoomId());
        assertTrue(!leaveGuest.isRoomRemoved());
        assertEquals(1, room.getPlayerCount());

        LeaveRoomResult leaveHost = registry.leaveRoom(
            room.getRoomId(), hostId, LeaveRoomAction.LEAVE, null);
        assertTrue(leaveHost.isRoomRemoved());
        assertTrue(registry.listRooms().isEmpty());
    }

    @Test
    public void testJoinRoomRejectsWhenFull() {
        GameRoom room = registry.createRoom("满员测试", "玩家1", 1L);
        for (int index = 2; index <= MultiplayerConfig.MAX_PLAYERS_PER_ROOM; index++) {
            registry.joinRoom(room.getRoomId(), "玩家" + index, index);
        }
        try {
            registry.joinRoom(room.getRoomId(), "多余玩家", 99L);
            fail("满员房间应拒绝新玩家");
        } catch (IllegalStateException exception) {
            assertTrue(exception.getMessage().contains("已满"));
        }
    }

    @Test
    public void testExistingMemberCanRejoinWhileInGame() {
        GameRoom room = registry.createRoom("游戏中", "房主", 1L);
        registry.joinRoom(room.getRoomId(), "队员", 2L);
        room.setInGame(true);

        JoinRoomResult hostRejoin = registry.joinRoom(room.getRoomId(), "房主", 1L);
        JoinRoomResult guestRejoin = registry.joinRoom(room.getRoomId(), "队员", 2L);

        assertEquals(room.getHostPlayerId(), hostRejoin.getPlayerId());
        assertNotEquals(room.getHostPlayerId(), guestRejoin.getPlayerId());
    }

    @Test(expected = IllegalStateException.class)
    public void testJoinRoomRejectsNewPlayerWhileInGame() {
        GameRoom room = registry.createRoom("游戏中", "房主", 1L);
        room.setInGame(true);
        registry.joinRoom(room.getRoomId(), "路人", 99L);
    }

    @Test
    public void testSyncClientViewFromSnapshot() {
        GameRoom room = registry.createRoom("同步测试", "展示者", 1L);
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
        GameRoom room = registry.createRoom("计时递减", "房主", 1L);
        int before = facade.getState(room, room.getHostPlayerId()).getRemainingSeconds();
        Thread.sleep(1500);
        int after = facade.getState(room, room.getHostPlayerId()).getRemainingSeconds();
        assertTrue("服务端计时应递减", after < before);
    }

    @Test
    public void testMultiplayerGoCommandDeductsActionTime() {
        GameRoom room = registry.createRoom("联机罚时", "房主", 1L);
        String hostId = room.getHostPlayerId();
        int before = facade.getState(room, hostId).getRemainingSeconds();
        facade.executeCommand(room, hostId, "go", "north");
        int after = facade.getState(room, hostId).getRemainingSeconds();
        assertEquals(before - ActionTimeCost.GO, after);
    }

    @Test
    public void testLevel3PlayerWithFlashlightCanEnterMainBuilding() {
        GameRoom room = registry.createRoom("手电进主楼", "带灯者", 1L);
        room.getGame().getLevelManager().setHighestUnlockedLevel(3);
        room.getGame().getLevelManager().startLevel(3);
        String hostId = room.getHostPlayerId();
        room.getGame().getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));

        facade.executeCommand(room, hostId, "go", "north");

        assertEquals("boxue_main", facade.getState(room, hostId).getRoomId());
    }

    @Test
    public void testLevel3TeammateCanEnterAfterMainBuildingIlluminated() {
        GameRoom room = registry.createRoom("照亮主楼", "带灯者", 1L);
        JoinRoomResult guest = registry.joinRoom(room.getRoomId(), "队友", 2L);
        room.getGame().getLevelManager().setHighestUnlockedLevel(3);
        room.getGame().getLevelManager().startLevel(3);
        String hostId = room.getHostPlayerId();
        String guestId = guest.getPlayerId();

        room.getGame().setActiveOnlinePlayer(hostId);
        room.getGame().getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        facade.executeCommand(room, hostId, "go", "north");
        assertEquals("boxue_main", facade.getState(room, hostId).getRoomId());

        facade.executeCommand(room, hostId, "go", "south");
        assertEquals("gate", facade.getState(room, hostId).getRoomId());

        facade.executeCommand(room, guestId, "go", "north");
        assertEquals("boxue_main", facade.getState(room, guestId).getRoomId());
    }

    @Test
    public void testLevel3WithoutFlashlightOrIlluminationBlocked() {
        GameRoom room = registry.createRoom("无手电", "摸索者", 1L);
        room.getGame().getLevelManager().setHighestUnlockedLevel(3);
        room.getGame().getLevelManager().startLevel(3);
        String hostId = room.getHostPlayerId();
        int before = facade.getState(room, hostId).getRemainingSeconds();

        GameCommandResult result = facade.executeCommand(room, hostId, "go", "north");

        assertEquals("gate", facade.getState(room, hostId).getRoomId());
        assertTrue(result.getMessages().stream()
            .anyMatch(line -> line.contains(DarkRoom.PENALTY_MESSAGE)));
        assertEquals(before - ActionTimeCost.DARK_PENALTY,
            facade.getState(room, hostId).getRemainingSeconds());
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

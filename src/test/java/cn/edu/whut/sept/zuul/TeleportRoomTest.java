/**
 * 测试具有传输功能的房间（TeleportRoom）的核心功能，
 * 验证玩家进入后是否能正确触发随机传输机制，以及相关边界情况。
 *
 * @author liujing
 * @version 1.4
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.LookCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TeleportRoomTest {
    private TeleportRoom teleportRoom;
    private Room target1;
    private Room target2;
    private Room target3;
    private Game game;
    private List<Room> targetRooms;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * 初始化测试环境，创建传输房间、目标房间及游戏实例
     */
    @Before
    public void setUp() throws Exception {
        // 初始化目标房间
        target1 = new Room("目标房间1");
        target2 = new Room("目标房间2");
        target3 = new Room("目标房间3");
        targetRooms = Arrays.asList(target1, target2, target3);

        // 创建传输房间
        teleportRoom = new TeleportRoom("测试传输房间", targetRooms);

        // 初始化游戏实例并设置初始房间
        game = new Game();
        Room initialRoom = new Room("初始房间");
        game.setCurrentRoom(initialRoom); // 重置初始房间

        // 关键修复：清空房间历史记录，避免初始化过程中残留记录
        java.lang.reflect.Field historyField = Game.class.getDeclaredField("roomHistory");
        historyField.setAccessible(true);
        List<Room> roomHistory = (List<Room>) historyField.get(game);
        roomHistory.clear(); // 清空历史记录

        // 重定向系统输出以捕获打印信息
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 测试结束后恢复系统输出流，避免影响其他测试
     */
    @After
    public void tearDown() {
        // 恢复系统默认输出流
        System.setOut(originalOut);
    }

    /**
     * 测试传输房间的teleport()方法在有多个目标时能随机返回目标房间
     * 验证：1. 返回的房间必在目标列表中；2. 多次调用有概率返回不同房间（提高随机性验证可信度）
     */
    @Test
    public void testTeleportWithMultipleTargets() {
        // 多次调用传输方法验证随机性
        boolean hasTarget1 = false;
        boolean hasTarget2 = false;
        boolean hasTarget3 = false;

        // 执行足够多次以覆盖概率
        for (int i = 0; i < 100; i++) {
            Room result = teleportRoom.teleport();
            assertTrue("传输结果必须在目标房间列表中", targetRooms.contains(result));

            if (result == target1) {
                hasTarget1 = true;
            }
            if (result == target2) {
                hasTarget2 = true;
            }
            if (result == target3) {
                hasTarget3 = true;
            }
        }

        // 验证所有目标都有被选中的可能（在大样本下应成立）
        assertTrue("传输机制应能选中目标1", hasTarget1);
        assertTrue("传输机制应能选中目标2", hasTarget2);
        assertTrue("传输机制应能选中目标3", hasTarget3);
    }

    /**
     * 测试传输房间在目标列表为空时不进行传输（返回自身）
     */
    @Test
    public void testTeleportWithEmptyTargets() {
        TeleportRoom emptyTeleportRoom = new TeleportRoom("无目标传输房间", null);
        Room result = emptyTeleportRoom.teleport();
        assertSame("目标列表为空时应返回自身", emptyTeleportRoom, result);

        // 测试空列表情况
        emptyTeleportRoom = new TeleportRoom("空列表传输房间", Arrays.asList());
        result = emptyTeleportRoom.teleport();
        assertSame("目标列表为空时应返回自身", emptyTeleportRoom, result);
    }

    /**
     * 测试传输房间在目标列表只有一个房间时，固定传输到该房间
     */
    @Test
    public void testTeleportWithSingleTarget() {
        Room singleTarget = new Room("唯一目标房间");
        TeleportRoom singleTeleportRoom = new TeleportRoom("单目标传输房间", Arrays.asList(singleTarget));

        // 多次调用验证结果一致性
        for (int i = 0; i < 10; i++) {
            Room result = singleTeleportRoom.teleport();
            assertSame("单目标时应固定传输到该目标", singleTarget, result);
        }
    }

    /**
     * 测试玩家进入传输房间后，游戏能正确触发传输并更新当前房间
     * 验证：1. 当前房间变为随机目标；2. 输出正确的提示信息；3. 房间历史记录正确
     */
    @Test
    public void testGameTeleportTrigger() throws Exception {
        // 保存进入传输房间前的当前房间
        Room preTeleportRoom = game.getCurrentRoom();

        // 进入传输房间（触发传输）
        game.setCurrentRoom(teleportRoom);

        // 验证当前房间已变更为目标房间之一
        Room current = game.getCurrentRoom();
        assertTrue("进入传输房间后应切换到目标房间", targetRooms.contains(current));

        // 验证提示信息正确
        String output = outContent.toString();
        assertTrue("应输出传输提示信息", output.contains("你进入了一个神秘的房间，突然被传送到了其他地方！"));

        // 验证房间历史记录（仅记录传输前的房间）
        java.lang.reflect.Field historyField = Game.class.getDeclaredField("roomHistory");
        historyField.setAccessible(true);
        List<Room> roomHistory = (List<Room>) historyField.get(game);
        assertEquals("历史记录应包含传输前的房间", 1, roomHistory.size()); // 此时应为1条
        assertSame("历史记录应正确记录传输前房间", preTeleportRoom, roomHistory.get(0));
    }

    /**
     * 测试传输后调用look命令能正确显示目标房间信息
     */
    @Test
    public void testLookAfterTeleport() {
        // 进入传输房间触发传输
        game.setCurrentRoom(teleportRoom);
        Room teleportedRoom = game.getCurrentRoom();

        // 执行look命令
        outContent.reset();
        new LookCommand().execute(game, null);

        // 验证look命令显示的是目标房间信息
        String output = outContent.toString();
        assertTrue("look命令应显示传输后的房间描述", output.contains(teleportedRoom.getShortDescription()));
    }
}
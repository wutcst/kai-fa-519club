package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.BackCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BackCommandTest {
    private Game game;
    private BackCommand backCommand;
    private Room startRoom;
    private Room theater;
    private Room lab;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        backCommand = new BackCommand();

        // 获取游戏初始化的房间（根据Game类的createRooms()方法）
        startRoom = game.getCurrentRoom(); // outside
        theater = startRoom.getExit("east"); // theater
        lab = startRoom.getExit("south"); // lab

        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testBackMultipleSteps() {
        // 移动路径：startRoom(outside) → theater（东）→ lab（从theater先回outside再南移到lab，或直接从outside南移）
        // 正确路径应为：outside → theater（东），再回到outside，再南移到lab
        game.setCurrentRoom(theater);  // 第一次移动：outside → theater（历史记录：[outside]）
        backCommand.execute(game, null); // 回退到outside（历史记录清空）
        game.setCurrentRoom(lab);      // 第二次移动：outside → lab（历史记录：[outside]）

        // 第一次back：从lab回到outside
        outContent.reset();
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("你回到了上一个房间。"));

        // 第二次back：已在起始房间，无法再回退
        outContent.reset();
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("无法返回，这是你的起始房间！"));
    }

    @Test
    public void testRoomHistoryManagement() {
        try {
            java.lang.reflect.Field historyField = Game.class.getDeclaredField("roomHistory");
            historyField.setAccessible(true);
            List<Room> roomHistory = (List<Room>) historyField.get(game);

            // 初始状态历史为空
            assertTrue(roomHistory.isEmpty());

            // 第一次移动：startRoom → theater
            game.setCurrentRoom(theater);
            assertEquals(1, roomHistory.size());
            assertEquals(startRoom.getShortDescription(), roomHistory.get(0).getShortDescription());

            // 第二次移动：theater → startRoom（回退）
            game.goBack();
            assertTrue(roomHistory.isEmpty());

            // 第三次移动：startRoom → lab
            game.setCurrentRoom(lab);
            assertEquals(1, roomHistory.size());
            assertEquals(startRoom.getShortDescription(), roomHistory.get(0).getShortDescription());

            // 第三次back：lab → startRoom
            game.goBack();
            assertTrue(roomHistory.isEmpty());

        } catch (Exception e) {
            fail("反射获取roomHistory失败：" + e.getMessage());
        }
    }

    @Test
    public void testBackAtStartRoom() {
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("无法返回，这是你的起始房间！"));
    }

    @Test
    public void testBackAfterOneMove() {
        game.setCurrentRoom(theater);
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("你回到了上一个房间。"));
    }

    @Test
    public void testBackWithParameters() {
        game.setCurrentRoom(theater);
        Room current = game.getCurrentRoom();
        outContent.reset();
        backCommand.execute(game, "参数");
        assertSame(current, game.getCurrentRoom());
        assertTrue(outContent.toString().contains("Back what? 请仅输入 'back' 命令"));
    }
}
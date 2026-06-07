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
    private Room boxueMain;
    private Room boxueNorth;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        backCommand = new BackCommand();

        startRoom = game.getCurrentRoom();
        boxueMain = startRoom.getExit("north");
        boxueNorth = boxueMain.getExit("north");

        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testBackMultipleSteps() {
        game.setCurrentRoom(boxueMain);
        backCommand.execute(game, null);
        game.setCurrentRoom(boxueNorth);

        outContent.reset();
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("你回到了上一个房间。"));

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

            assertTrue(roomHistory.isEmpty());

            game.setCurrentRoom(boxueMain);
            assertEquals(1, roomHistory.size());
            assertEquals(startRoom.getShortDescription(), roomHistory.get(0).getShortDescription());

            game.goBack();
            assertTrue(roomHistory.isEmpty());

            game.setCurrentRoom(boxueNorth);
            assertEquals(1, roomHistory.size());
            assertEquals(startRoom.getShortDescription(), roomHistory.get(0).getShortDescription());

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
        game.setCurrentRoom(boxueMain);
        backCommand.execute(game, null);
        assertEquals(startRoom.getShortDescription(), game.getCurrentRoom().getShortDescription());
        assertTrue(outContent.toString().contains("你回到了上一个房间。"));
    }

    @Test
    public void testBackWithParameters() {
        game.setCurrentRoom(boxueMain);
        Room current = game.getCurrentRoom();
        outContent.reset();
        backCommand.execute(game, "参数");
        assertSame(current, game.getCurrentRoom());
        assertTrue(outContent.toString().contains("Back what? 请仅输入 'back' 命令"));
    }
}

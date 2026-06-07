package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E12 黑暗罚时：博学主楼无手电筒进入罚 1 分钟并退回。
 */
public class DarkRoomTest {

    private Game game;
    private Room theater;
    private DarkRoom boxueMain;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        theater = game.getRoomById("theater");
        boxueMain = (DarkRoom) game.getRoomById("boxue_main");
        game.resetPlayerPosition(theater);
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testCannotEnterDarkRoomWithoutFlashlight() {
        int before = game.getLevelTimer().getRemainingSeconds();

        assertFalse(game.setCurrentRoom(boxueMain));
        assertEquals(theater, game.getCurrentRoom());
        assertEquals(before - ActionTimeCost.DARK_PENALTY, game.getLevelTimer().getRemainingSeconds());
        assertTrue(out.toString().contains(DarkRoom.PENALTY_MESSAGE));
    }

    @Test
    public void testCanEnterDarkRoomWithFlashlight() {
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));

        assertTrue(game.setCurrentRoom(boxueMain));
        assertEquals(boxueMain, game.getCurrentRoom());
    }

    @Test
    public void testGoCommandBlockedWithoutFlashlight() {
        int before = game.getLevelTimer().getRemainingSeconds();
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "east");

        assertEquals(theater, game.getCurrentRoom());
        assertEquals(before - ActionTimeCost.DARK_PENALTY, game.getLevelTimer().getRemainingSeconds());
        assertFalse(out.toString().contains("You are " + boxueMain.getShortDescription()));
    }

    @Test
    public void testGoCommandPassesWithFlashlight() {
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "east");

        assertEquals(boxueMain, game.getCurrentRoom());
    }

    @Test
    public void testDarkPenaltyDoesNotChargeGoTime() {
        int before = game.getLevelTimer().getRemainingSeconds();

        game.setCurrentRoom(boxueMain);

        assertEquals(before - ActionTimeCost.DARK_PENALTY, game.getLevelTimer().getRemainingSeconds());
    }
}

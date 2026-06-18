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
 * E12 黑暗罚时：第三、四关博学主楼无手电筒罚 1 分钟并退回。
 */
public class DarkRoomTest {

    private Game game;
    private Room gate;
    private DarkRoom boxueMain;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        gate = game.getRoomById("gate");
        boxueMain = (DarkRoom) game.getRoomById("boxue_main");
        enterLevelThree();
        game.resetPlayerPosition(gate);
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
        assertEquals(gate, game.getCurrentRoom());
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

        goCommand.execute(game, "north");

        assertEquals(gate, game.getCurrentRoom());
        assertEquals(before - ActionTimeCost.DARK_PENALTY, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testGoCommandPassesWithFlashlight() {
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "north");

        assertEquals(boxueMain, game.getCurrentRoom());
    }

    @Test
    public void testNoDarkPenaltyOnLevelOne() {
        game.getLevelManager().restartCurrentLevel();
        game.getLevelManager().startLevel(1);
        game.resetPlayerPosition(gate);
        int before = game.getLevelTimer().getRemainingSeconds();

        assertTrue(game.setCurrentRoom(boxueMain));
        assertEquals(boxueMain, game.getCurrentRoom());
        assertEquals(before, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testReenterMainBuildingWithoutFlashlightAfterIlluminated() {
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        assertTrue(game.setCurrentRoom(boxueMain));
        assertTrue(game.isMainBuildingIlluminated());

        game.setCurrentRoom(gate);
        game.getPlayer().dropItem(DarkRoom.FLASHLIGHT_ITEM);

        assertTrue(game.setCurrentRoom(boxueMain));
        assertEquals(boxueMain, game.getCurrentRoom());
    }

    private void enterLevelThree() {
        game.getLevelManager().completeCurrentLevel();
        game.getLevelManager().completeCurrentLevel();
        assertEquals(3, game.getLevelManager().getCurrentLevel());
    }
}

package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.level.LevelConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E15 按关卡解锁房间：Game 与 GoCommand 拦截未开放出口。
 */
public class RoomUnlockTest {

    private Game game;
    private Room gate;
    private Room gymnasium;
    private Room library;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        gate = game.getRoomById("gate");
        gymnasium = game.getRoomById("gymnasium");
        library = game.getRoomById("library");
        game.resetPlayerPosition(gate);
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testLevelOneGoCommandBlocksLockedExit() {
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "west");

        assertEquals(gate, game.getCurrentRoom());
        assertTrue(out.toString().contains(LevelConfig.LOCKED_EXIT_MESSAGE));
    }

    @Test
    public void testLevelOneSetCurrentRoomBlocksLockedRoom() {
        assertFalse(game.isRoomAccessible(gymnasium));
        assertFalse(game.setCurrentRoom(gymnasium));
        assertEquals(gate, game.getCurrentRoom());
        assertTrue(out.toString().contains(LevelConfig.LOCKED_EXIT_MESSAGE));
    }

    @Test
    public void testLevelOneCanEnterUnlockedRoom() {
        Room boxueMain = game.getRoomById("boxue_main");

        assertTrue(game.isRoomAccessible(boxueMain));
        assertTrue(game.setCurrentRoom(boxueMain));
        assertEquals(boxueMain, game.getCurrentRoom());
    }

    @Test
    public void testLevelTwoGoCommandAllowsGymnasium() {
        advanceToLevel(2);
        out.reset();
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "west");

        assertEquals(gymnasium, game.getCurrentRoom());
    }

    @Test
    public void testLevelThreeUnlocksWestBuilding() {
        advanceToLevel(3);
        Room boxueWest = game.getRoomById("boxue_west");

        assertTrue(game.isRoomAccessible(boxueWest));
    }

    @Test
    public void testLevelFourUnlocksLibrary() {
        advanceToLevel(4);

        assertTrue(game.isRoomAccessible(library));
    }

    @Test
    public void testIsRoomAccessibleRejectsNull() {
        assertFalse(game.isRoomAccessible(null));
    }

    @Test
    public void testRoomAccessibilityFollowsLevelManager() {
        assertFalse(game.isRoomAccessible(gymnasium));

        advanceToLevel(2);
        assertTrue(game.isRoomAccessible(gymnasium));

        advanceToLevel(4);
        assertTrue(game.isRoomAccessible(library));
    }

    private void advanceToLevel(int targetLevel) {
        while (game.getLevelManager().getCurrentLevel() < targetLevel) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.resetPlayerPosition(gate);
        out.reset();
    }
}

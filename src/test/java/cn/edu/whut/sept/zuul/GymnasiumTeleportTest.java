/**
 * E17 第五关体育馆随机传送：按关启用、back 纠错、扣时不判负。
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.BackCommand;
import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * 体育馆传送关卡行为测试。
 */
public class GymnasiumTeleportTest {

    private Game game;
    private Room gate;
    private TeleportRoom gymnasium;
    private GoCommand goCommand;
    private BackCommand backCommand;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        gate = game.getRoomById("gate");
        gymnasium = (TeleportRoom) game.getRoomById(UnlockService.GYM_ROOM_ID);
        goCommand = new GoCommand();
        backCommand = new BackCommand();
        game.resetPlayerPosition(gate);
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    private void advanceToLevel(int targetLevel) {
        while (game.getLevelManager().getCurrentLevel() < targetLevel) {
            game.getLevelManager().completeCurrentLevel();
        }
        game.resetPlayerPosition(gate);
        out.reset();
    }

    @Test
    public void testGymnasiumTeleportDisabledBeforeLevelFive() {
        advanceToLevel(4);
        assertFalse(gymnasium.isTeleportEnabled());

        goCommand.execute(game, "west");

        assertEquals(gymnasium, game.getCurrentRoom());
        assertFalse(out.toString().contains("突然被传送"));
    }

    @Test
    public void testGymnasiumTeleportEnabledOnLevelFive() {
        advanceToLevel(5);
        assertTrue(gymnasium.isTeleportEnabled());
    }

    @Test
    public void testLevelFiveEnterGymnasiumTeleportsAwayFromDormitory() {
        advanceToLevel(5);
        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();

        goCommand.execute(game, "west");

        Room current = game.getCurrentRoom();
        assertNotEquals(UnlockService.DORMITORY_ROOM_ID, current.getRoomId());
        assertTrue(out.toString().contains("突然被传送"));
        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        assertEquals(beforeSeconds - ActionTimeCost.GO, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testBackAfterTeleportReturnsToPreviousRoom() {
        advanceToLevel(5);

        goCommand.execute(game, "west");
        out.reset();

        backCommand.execute(game, null);

        assertEquals(gate, game.getCurrentRoom());
        assertTrue(out.toString().contains("回到了上一个房间"));
    }
}

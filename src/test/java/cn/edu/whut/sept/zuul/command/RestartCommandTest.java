package cn.edu.whut.sept.zuul.command;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * restart 命令单元测试：失败关重开。
 */
public class RestartCommandTest {

    private Game game;
    private RestartCommand restartCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() {
        game = new Game();
        restartCommand = new RestartCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testRestartAfterFailureRestartsCurrentLevel() {
        int level = game.getLevelManager().getCurrentLevel();
        game.getLevelManager().failCurrentLevel();
        assertEquals(LevelState.FAILED, game.getLevelManager().getState());

        restartCommand.execute(game, null);

        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        assertEquals(level, game.getLevelManager().getCurrentLevel());
        assertTrue(outContent.toString().contains("重新挑战"));
    }

    @Test
    public void testRestartWhenNotFailedShowsHint() {
        restartCommand.execute(game, null);

        assertEquals(LevelState.IN_PROGRESS, game.getLevelManager().getState());
        assertTrue(outContent.toString().contains("当前无需重开"));
    }
}

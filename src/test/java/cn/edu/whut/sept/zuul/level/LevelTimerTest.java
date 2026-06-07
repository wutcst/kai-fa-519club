package cn.edu.whut.sept.zuul.level;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LevelTimer 单元测试：倒计时、扣减、失败联动、加时与关卡重置。
 */
public class LevelTimerTest {

    private Game game;
    private LevelTimer timer;
    private LevelManager levelManager;

    @Before
    public void setUp() {
        game = new Game();
        timer = game.getLevelTimer();
        levelManager = game.getLevelManager();
    }

    @Test
    public void testInitialTimerMatchesLevelOneLimit() {
        assertEquals(240, timer.getRemainingSeconds());
        assertTrue(timer.isActive());
    }

    @Test
    public void testDisplayTextFormat() {
        timer.deduct(40);
        assertEquals("距熄灯（23:00）还有 200 秒", timer.getDisplayText());
    }

    @Test
    public void testDeductReducesRemainingSeconds() {
        assertTrue(timer.deduct(10));
        assertEquals(230, timer.getRemainingSeconds());
    }

    @Test
    public void testDeductToZeroTriggersLevelFailure() {
        assertTrue(timer.deduct(240));
        assertEquals(0, timer.getRemainingSeconds());
        assertEquals(LevelState.FAILED, levelManager.getState());
        assertFalse(timer.isActive());
    }

    @Test
    public void testDeductBeyondRemainingTriggersFailure() {
        assertTrue(timer.deduct(300));
        assertEquals(0, timer.getRemainingSeconds());
        assertEquals(LevelState.FAILED, levelManager.getState());
    }

    @Test
    public void testNoDeductAfterFailure() {
        timer.deduct(240);
        assertFalse(timer.deduct(10));
        assertEquals(0, timer.getRemainingSeconds());
    }

    @Test
    public void testAddSecondsExtendsTime() {
        timer.deduct(40);
        timer.addSeconds(300);
        assertEquals(500, timer.getRemainingSeconds());
    }

    @Test
    public void testRestartLevelResetsTimer() {
        timer.deduct(100);
        levelManager.restartCurrentLevel();
        assertEquals(240, timer.getRemainingSeconds());
        assertTrue(timer.isActive());
    }

    @Test
    public void testCompleteLevelResetsTimerForNextLevel() {
        levelManager.completeCurrentLevel();
        assertEquals(2, levelManager.getCurrentLevel());
        assertEquals(300, timer.getRemainingSeconds());
        assertTrue(timer.isActive());
    }

    @Test
    public void testGameWonStopsTimer() {
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        assertEquals(LevelState.GAME_WON, levelManager.getState());
        assertFalse(timer.isActive());
    }

    @Test
    public void testManualFailStopsTimer() {
        levelManager.failCurrentLevel();
        assertEquals(LevelState.FAILED, levelManager.getState());
        assertFalse(timer.isActive());
    }

    @Test
    public void testResetForLevelUsesConfigLimit() {
        timer.resetForLevel(LevelConfig.forLevel(3));
        assertEquals(420, timer.getRemainingSeconds());
    }
}

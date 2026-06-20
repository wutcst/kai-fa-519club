package cn.edu.whut.sept.zuul.level;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Room;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LevelManager 单元测试：关卡切换、通关解锁、失败重开、背包清空。
 */
public class LevelManagerTest {

    private Game game;
    private LevelManager levelManager;
    private Room gateRoom;

    @Before
    public void setUp() {
        game = new Game();
        levelManager = game.getLevelManager();
        gateRoom = game.getRoomById("gate");
    }

    @Test
    public void testInitialLevelIsOne() {
        assertEquals(1, levelManager.getCurrentLevel());
        assertEquals(1, levelManager.getHighestUnlockedLevel());
        assertEquals(LevelState.IN_PROGRESS, levelManager.getState());
        assertEquals(gateRoom, game.getCurrentRoom());
    }

    @Test
    public void testStartLevelClearsInventory() {
        game.getPlayer().takeItem(new Item("测试物品", 100));
        assertEquals(1, game.getPlayer().getInventory().size());

        levelManager.restartCurrentLevel();

        assertTrue(game.getPlayer().getInventory().isEmpty());
        assertEquals(0, game.getPlayer().getCurrentWeight());
    }

    @Test
    public void testStartLevelResetsPositionAndHistory() {
        Room boxueMain = game.getRoomById("boxue_main");
        game.setCurrentRoom(boxueMain);
        assertEquals(boxueMain, game.getCurrentRoom());

        levelManager.restartCurrentLevel();

        assertEquals(gateRoom, game.getCurrentRoom());
    }

    @Test
    public void testCompleteLevelUnlocksAndAdvances() {
        assertFalse(levelManager.completeCurrentLevel());

        assertEquals(2, levelManager.getCurrentLevel());
        assertEquals(2, levelManager.getHighestUnlockedLevel());
        assertEquals(LevelState.IN_PROGRESS, levelManager.getState());
        assertEquals(gateRoom, game.getCurrentRoom());
        assertTrue(game.getPlayer().getInventory().isEmpty());
    }

    @Test
    public void testCompleteAllLevelsWinsGame() {
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        levelManager.completeCurrentLevel();
        assertTrue(levelManager.completeCurrentLevel());

        assertEquals(5, levelManager.getCurrentLevel());
        assertEquals(LevelState.GAME_WON, levelManager.getState());
        assertTrue(levelManager.isGameWon());
    }

    @Test
    public void testFailAndRestartCurrentLevel() {
        game.getPlayer().takeItem(new Item("临时物品", 50));
        levelManager.failCurrentLevel();

        assertEquals(LevelState.FAILED, levelManager.getState());
        assertEquals(1, levelManager.getCurrentLevel());

        levelManager.restartCurrentLevel();

        assertEquals(LevelState.IN_PROGRESS, levelManager.getState());
        assertEquals(1, levelManager.getCurrentLevel());
        assertTrue(game.getPlayer().getInventory().isEmpty());
        assertEquals(gateRoom, game.getCurrentRoom());
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotStartLockedLevel() {
        levelManager.startLevel(3);
    }

    @Test
    public void testLevelConfigMatchesCurrentLevel() {
        LevelConfig config = levelManager.getCurrentLevelConfig();
        assertEquals(1, config.getLevelNumber());
        assertEquals(240, config.getTimeLimitSeconds());
        assertEquals("gate", config.getStartRoomId());
    }

    @Test
    public void testLevelConfigForEachLevel() {
        for (int level = 1; level <= LevelConfig.MAX_LEVEL; level++) {
            LevelConfig config = LevelConfig.forLevel(level);
            assertEquals(level, config.getLevelNumber());
            assertEquals("gate", config.getStartRoomId());
        }
        assertEquals(720, LevelConfig.forLevel(5).getTimeLimitSeconds());
    }
}

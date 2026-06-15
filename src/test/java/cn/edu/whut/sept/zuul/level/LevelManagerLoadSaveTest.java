package cn.edu.whut.sept.zuul.level;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Room;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * LevelManager 读档恢复单元测试（F8）。
 */
public class LevelManagerLoadSaveTest {

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
    public void testLoadSavedProgressRestoresLevelAndTimer() {
        levelManager.completeCurrentLevel();
        levelManager.loadSavedProgress(2, 187);

        assertEquals(2, levelManager.getCurrentLevel());
        assertEquals(187, game.getLevelTimer().getRemainingSeconds());
        assertEquals(LevelState.IN_PROGRESS, levelManager.getState());
        assertEquals(gateRoom, game.getCurrentRoom());
    }

    @Test
    public void testLoadSavedProgressClearsInventory() {
        game.getPlayer().takeItem(new Item("测试物品", 100));
        levelManager.loadSavedProgress(1, 120);

        assertTrue(game.getPlayer().getInventory().isEmpty());
        assertEquals(120, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testLoadSavedProgressUnlocksLevelIfNeeded() {
        levelManager.loadSavedProgress(4, 400);

        assertEquals(4, levelManager.getHighestUnlockedLevel());
        assertEquals(4, levelManager.getCurrentLevel());
    }
}

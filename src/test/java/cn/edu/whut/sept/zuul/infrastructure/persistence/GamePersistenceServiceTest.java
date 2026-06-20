package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * F8 H2 存档与读档单元测试（内存库，不污染仓库）。
 */
public class GamePersistenceServiceTest {

    private H2Database database;
    private GamePersistenceService persistenceService;

    @Before
    public void setUp() {
        String dbName = "zuul_persist_test_" + UUID.randomUUID().toString().replace("-", "");
        database = H2Database.createInMemoryDatabase(dbName);
        persistenceService = GamePersistenceService.create(database);
    }

    @After
    public void tearDown() {
        database = null;
        persistenceService = null;
    }

    @Test
    public void testSaveAndLoadProgress() {
        Game game = createGameWithPersistence();
        game.getLevelManager().completeCurrentLevel();
        game.getLevelTimer().deduct(50);

        long saveId = persistenceService.saveProgress(game);
        assertTrue(saveId > 0);

        Game loadedGame = createGameWithPersistence();
        assertTrue(persistenceService.loadProgress(loadedGame, saveId));

        assertEquals(2, loadedGame.getLevelManager().getCurrentLevel());
        assertEquals(250, loadedGame.getLevelTimer().getRemainingSeconds());
        assertEquals("测试玩家", loadedGame.getPlayer().getName());
        assertEquals("gate", loadedGame.getCurrentRoom().getRoomId());
        assertTrue(loadedGame.getPlayer().getInventory().isEmpty());
        assertEquals(LevelState.IN_PROGRESS, loadedGame.getLevelManager().getState());
    }

    @Test
    public void testLoadNonExistentSaveReturnsFalse() {
        Game game = createGameWithPersistence();
        assertFalse(persistenceService.loadProgress(game, 9999L));
    }

    @Test
    public void testListSavesOrderedByTime() {
        Game game = createGameWithPersistence();
        long first = persistenceService.saveProgress(game);
        game.getLevelTimer().deduct(10);
        long second = persistenceService.saveProgress(game);

        List<GameSaveRecord> saves = persistenceService.listSaves();
        assertEquals(2, saves.size());
        assertEquals(second, saves.get(0).getId());
        assertEquals(first, saves.get(1).getId());
    }

    @Test
    public void testRecordClearOnAllLevelsCompleted() {
        Game game = createGameWithPersistence();
        game.getPlayer().setName("通关玩家");

        for (int i = 0; i < LevelConfig.MAX_LEVEL; i++) {
            game.getLevelManager().completeCurrentLevel();
        }

        List<ClearRecord> records = persistenceService.listClearRecords();
        assertEquals(1, records.size());
        assertEquals("通关玩家", records.get(0).getPlayerName());
    }

    @Test
    public void testLoadDoesNotRestoreInventoryEvenWhenSaveHadItems() {
        Game game = createGameWithPersistence();
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));
        long saveId = persistenceService.saveProgress(game);

        Game loadedGame = createGameWithPersistence();
        assertTrue(persistenceService.loadProgress(loadedGame, saveId));

        assertTrue(loadedGame.getPlayer().getInventory().isEmpty());
        assertEquals("gate", loadedGame.getCurrentRoom().getRoomId());
    }

    @Test
    public void testListSavesByUserId() {
        AuthService authService = AuthService.create(database);
        Game gameA = createGameWithPersistence();
        Game gameB = createGameWithPersistence();

        gameA.bindAuthSession(authService.registerConsole("user_a", "secret12", "玩家A").getSession());
        gameB.bindAuthSession(authService.registerConsole("user_b", "secret12", "玩家B").getSession());

        long saveA = persistenceService.saveProgress(gameA);
        persistenceService.saveProgress(gameB);

        List<GameSaveRecord> savesForA = persistenceService.listSavesByUserId(
            gameA.getAuthSession().getUserId());
        assertEquals(1, savesForA.size());
        assertEquals(saveA, savesForA.get(0).getId());
    }

    @Test(expected = PersistenceException.class)
    public void testCannotSaveWhenLevelFailed() {
        Game game = createGameWithPersistence();
        game.getLevelManager().failCurrentLevel();
        persistenceService.saveProgress(game);
    }

    @Test
    public void testSimulateRestartRestoresLevelAndTimer() {
        Game original = createGameWithPersistence();
        original.getLevelManager().completeCurrentLevel();
        original.getLevelTimer().deduct(50);

        long saveId = persistenceService.saveProgress(original);

        Game restarted = new Game();
        restarted.setPersistenceService(persistenceService);
        assertTrue(persistenceService.loadProgress(restarted, saveId));

        assertEquals(2, restarted.getLevelManager().getCurrentLevel());
        assertEquals(250, restarted.getLevelTimer().getRemainingSeconds());
        assertEquals(LevelState.IN_PROGRESS, restarted.getLevelManager().getState());
        assertEquals("gate", restarted.getCurrentRoom().getRoomId());
        assertTrue(restarted.getPlayer().getInventory().isEmpty());
    }

    private Game createGameWithPersistence() {
        Game game = new Game();
        game.setPersistenceService(persistenceService);
        game.getPlayer().setName("测试玩家");
        return game;
    }
}

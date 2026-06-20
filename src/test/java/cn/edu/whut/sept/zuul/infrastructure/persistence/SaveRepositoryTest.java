package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.Game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * T3/F8：存档 DAO 层单元测试（内存 H2）。
 */
public class SaveRepositoryTest {

    private H2Database database;
    private SaveRepository saveRepository;

    @Before
    public void setUp() {
        String dbName = "zuul_save_repo_" + UUID.randomUUID().toString().replace("-", "");
        database = H2Database.createInMemoryDatabase(dbName);
        database.initializeSchema();
        saveRepository = new SaveRepository(database);
    }

    @After
    public void tearDown() {
        database = null;
        saveRepository = null;
    }

    @Test
    public void testInsertAndFindSnapshotById() {
        Game game = new Game();
        game.getPlayer().setName("DAO测试");
        game.getLevelManager().completeCurrentLevel();
        game.getLevelTimer().deduct(42);

        GameSaveSnapshot snapshot = GamePersistenceService.captureSnapshot(game);
        long saveId = saveRepository.insertSnapshot(snapshot);

        Optional<GameSaveSnapshot> loaded = saveRepository.findSnapshotById(saveId);
        assertTrue(loaded.isPresent());
        assertEquals("DAO测试", loaded.get().getPlayerName());
        assertEquals(2, loaded.get().getLevelNumber());
        assertEquals(258, loaded.get().getRemainingSeconds());
        assertEquals("gate", loaded.get().getCurrentRoomId());
        assertEquals(
            game.getLevelManager().captureProgressSnapshot().isDormitorySubmitCompleted(),
            loaded.get().getProgress().isDormitorySubmitCompleted());
    }

    @Test
    public void testFindSnapshotByIdReturnsEmptyForMissing() {
        assertFalse(saveRepository.findSnapshotById(99999L).isPresent());
    }

    @Test
    public void testFindAllOrderBySavedAtDesc() {
        Game game = new Game();
        game.getPlayer().setName("排序测试");
        GameSaveSnapshot first = GamePersistenceService.captureSnapshot(game);
        long firstId = saveRepository.insertSnapshot(first);

        game.getLevelTimer().deduct(5);
        GameSaveSnapshot second = GamePersistenceService.captureSnapshot(game);
        long secondId = saveRepository.insertSnapshot(second);

        assertEquals(2, saveRepository.findAllOrderBySavedAtDesc().size());
        assertEquals(secondId, saveRepository.findAllOrderBySavedAtDesc().get(0).getId());
        assertEquals(firstId, saveRepository.findAllOrderBySavedAtDesc().get(1).getId());
    }
}

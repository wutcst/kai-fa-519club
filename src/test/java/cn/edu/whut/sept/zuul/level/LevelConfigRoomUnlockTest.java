package cn.edu.whut.sept.zuul.level;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E15 按关卡解锁房间：LevelConfig 各关开放房间集合与策划一致。
 */
public class LevelConfigRoomUnlockTest {

    private static final String[] ALL_ROOM_IDS = {
        "gate", "boxue_main", "boxue_north", "supermarket", "dormitory",
        "gymnasium", "canteen", "boxue_west", "boxue_east", "library"
    };

    private static final int[] EXPECTED_UNLOCKED_COUNTS = {5, 7, 9, 10, 10};

    @Test
    public void testUnlockedRoomCountPerLevel() {
        for (int level = 1; level <= LevelConfig.MAX_LEVEL; level++) {
            LevelConfig config = LevelConfig.forLevel(level);
            int unlocked = countUnlockedRooms(config);
            assertEquals("第 " + level + " 关开放房间数", EXPECTED_UNLOCKED_COUNTS[level - 1], unlocked);
        }
    }

    @Test
    public void testLevelOneOnlyCoreFiveRooms() {
        LevelConfig config = LevelConfig.forLevel(1);
        assertTrue(config.isRoomUnlocked("gate"));
        assertTrue(config.isRoomUnlocked("boxue_main"));
        assertTrue(config.isRoomUnlocked("boxue_north"));
        assertTrue(config.isRoomUnlocked("supermarket"));
        assertTrue(config.isRoomUnlocked("dormitory"));
        assertFalse(config.isRoomUnlocked("gymnasium"));
        assertFalse(config.isRoomUnlocked("canteen"));
        assertFalse(config.isRoomUnlocked("boxue_west"));
        assertFalse(config.isRoomUnlocked("boxue_east"));
        assertFalse(config.isRoomUnlocked("library"));
    }

    @Test
    public void testLevelTwoAddsGymnasiumAndCanteen() {
        LevelConfig config = LevelConfig.forLevel(2);
        assertTrue(config.isRoomUnlocked("gymnasium"));
        assertTrue(config.isRoomUnlocked("canteen"));
        assertFalse(config.isRoomUnlocked("boxue_west"));
        assertFalse(config.isRoomUnlocked("boxue_east"));
        assertFalse(config.isRoomUnlocked("library"));
    }

    @Test
    public void testLevelThreeAddsWestAndEastBuildings() {
        LevelConfig config = LevelConfig.forLevel(3);
        assertTrue(config.isRoomUnlocked("boxue_west"));
        assertTrue(config.isRoomUnlocked("boxue_east"));
        assertFalse(config.isRoomUnlocked("library"));
    }

    @Test
    public void testLevelFourAndFiveUnlockFullMap() {
        for (int level = 4; level <= 5; level++) {
            LevelConfig config = LevelConfig.forLevel(level);
            for (String roomId : ALL_ROOM_IDS) {
                assertTrue("第 " + level + " 关应开放 " + roomId, config.isRoomUnlocked(roomId));
            }
        }
    }

    @Test
    public void testLockedExitMessageIsDefined() {
        assertEquals("夜色中，这个方向暂未开放。", LevelConfig.LOCKED_EXIT_MESSAGE);
    }

    private int countUnlockedRooms(LevelConfig config) {
        int count = 0;
        for (String roomId : ALL_ROOM_IDS) {
            if (config.isRoomUnlocked(roomId)) {
                count++;
            }
        }
        return count;
    }
}

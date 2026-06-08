/**
 * UnlockCommand 单元测试：体育馆与寝室密码解锁（E4 #21）。
 *
 * @author liujing
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.TakeCommand;
import cn.edu.whut.sept.zuul.command.UnlockCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * UnlockCommand 功能测试。
 */
public class UnlockCommandTest {

    private Game game;
    private Player player;
    private UnlockCommand unlockCommand;
    private TakeCommand takeCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        unlockCommand = new UnlockCommand();
        takeCommand = new TakeCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    private void setCurrentRoom(Room room) throws Exception {
        player.setCurrentRoom(room);
        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, room);
    }

    private void advanceToLevel(int level) {
        while (game.getLevelManager().getCurrentLevel() < level) {
            game.getLevelManager().completeCurrentLevel();
        }
    }

    @Test
    public void testGymUnlockPlacesFlashlightInRoom() throws Exception {
        advanceToLevel(3);
        Room gym = game.getRoomById(UnlockService.GYM_ROOM_ID);
        setCurrentRoom(gym);

        unlockCommand.execute(game, UnlockService.GYM_PASSWORD);

        assertTrue(game.getLevelManager().isGymStorageUnlocked());
        assertTrue(gym.containsItem(DarkRoom.FLASHLIGHT_ITEM));
        assertNull(player.findItemInInventory(DarkRoom.FLASHLIGHT_ITEM));
    }

    @Test
    public void testTakeFlashlightAfterGymUnlock() throws Exception {
        advanceToLevel(3);
        Room gym = game.getRoomById(UnlockService.GYM_ROOM_ID);
        setCurrentRoom(gym);

        unlockCommand.execute(game, UnlockService.GYM_PASSWORD);
        takeCommand.execute(game, DarkRoom.FLASHLIGHT_ITEM);

        assertNotNull(player.findItemInInventory(DarkRoom.FLASHLIGHT_ITEM));
        assertFalse(gym.containsItem(DarkRoom.FLASHLIGHT_ITEM));
    }

    @Test
    public void testGymUnlockWrongPasswordDeductsTime() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById(UnlockService.GYM_ROOM_ID));
        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();

        unlockCommand.execute(game, "wrong");

        assertFalse(game.getLevelManager().isGymStorageUnlocked());
        assertEquals(beforeSeconds - ActionTimeCost.WRONG_PASSWORD,
            game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("密码错误"));
    }

    @Test
    public void testDistractionPasswordFromCanteenNote() throws Exception {
        advanceToLevel(3);
        setCurrentRoom(game.getRoomById(UnlockService.GYM_ROOM_ID));
        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();

        unlockCommand.execute(game, UnlockService.DISTRACTION_DATE_PASSWORD);

        assertFalse(game.getLevelManager().isGymStorageUnlocked());
        assertEquals(beforeSeconds - ActionTimeCost.WRONG_PASSWORD,
            game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("食堂纸条"));
    }

    @Test
    public void testCanteenHasDistractionNote() {
        Room canteen = game.getRoomById("canteen");
        assertTrue(canteen.containsItem(UnlockService.CANTEEN_NOTE_ITEM));
    }

    @Test
    public void testGymUnlockNotAvailableOnLevelTwo() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UnlockService.GYM_ROOM_ID));

        unlockCommand.execute(game, UnlockService.GYM_PASSWORD);

        assertFalse(game.getLevelManager().isGymStorageUnlocked());
        assertTrue(outContent.toString().contains("无需解锁"));
    }

    @Test
    public void testDormitoryUnlockLevelFive() throws Exception {
        advanceToLevel(5);
        setCurrentRoom(game.getRoomById(UnlockService.DORMITORY_ROOM_ID));

        unlockCommand.execute(game, UnlockService.DORMITORY_PASSWORD);

        assertTrue(game.getLevelManager().isDormitoryPasswordUnlocked());
        assertTrue(outContent.toString().contains("智能锁"));
    }

    @Test
    public void testDormitoryUnlockWrongPassword() throws Exception {
        advanceToLevel(5);
        setCurrentRoom(game.getRoomById(UnlockService.DORMITORY_ROOM_ID));
        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();

        unlockCommand.execute(game, "12345678");

        assertFalse(game.getLevelManager().isDormitoryPasswordUnlocked());
        assertEquals(beforeSeconds - ActionTimeCost.WRONG_PASSWORD,
            game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testUnlockInWrongRoom() throws Exception {
        setCurrentRoom(game.getRoomById("gate"));

        unlockCommand.execute(game, UnlockService.GYM_PASSWORD);

        assertFalse(game.getLevelManager().isGymStorageUnlocked());
        assertTrue(outContent.toString().contains("无法输入密码"));
    }

    @Test
    public void testUnlockWithoutPassword() throws Exception {
        unlockCommand.execute(game, null);
        assertTrue(outContent.toString().contains("请输入密码"));
    }

    @Test
    public void testUnlockCommandRegistered() {
        assertTrue(containsCommand(game.getCommandManager().getCommandWords(), "unlock"));
    }

    @Test
    public void testHelpShowsUnlockDescription() {
        outContent.reset();
        game.processCommand(new Command("help", null));
        assertTrue(outContent.toString().contains(UnlockCommand.getUsageDescription()));
    }

    private boolean containsCommand(String[] commandWords, String target) {
        for (String word : commandWords) {
            if (word.equals(target)) {
                return true;
            }
        }
        return false;
    }
}

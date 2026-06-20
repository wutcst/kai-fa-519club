/**
 * SleepCommand 单元测试：寝室过关条件校验与通关逻辑（E6 #23）。
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.SleepCommand;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SleepCommand 功能测试。
 */
public class SleepCommandTest {

    private Game game;
    private Player player;
    private SleepCommand sleepCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        sleepCommand = new SleepCommand();
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

    private Room dormitory() {
        return game.getRoomById(UnlockService.DORMITORY_ROOM_ID);
    }

    @Test
    public void testSleepOutsideDormitory() throws Exception {
        setCurrentRoom(game.getRoomById("gate"));
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));

        sleepCommand.execute(game, null);

        assertTrue(outContent.toString().contains("请回到寝室"));
        assertEquals(1, game.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testSleepWithoutCampusCardLevel1() throws Exception {
        setCurrentRoom(dormitory());

        sleepCommand.execute(game, null);

        assertTrue(outContent.toString().contains("一卡通"));
        assertEquals(1, game.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testSleepSuccessLevel1() throws Exception {
        setCurrentRoom(dormitory());
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));

        sleepCommand.execute(game, null);

        assertEquals(2, game.getLevelManager().getCurrentLevel());
        assertTrue(outContent.toString().contains("恭喜通关"));
    }

    @Test
    public void testSleepWithoutSubmitLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(dormitory());
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));

        sleepCommand.execute(game, null);

        assertTrue(outContent.toString().contains("归寝单"));
        assertEquals(2, game.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testSleepSuccessLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(dormitory());
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));
        game.getLevelManager().markDormitorySubmitCompleted();

        sleepCommand.execute(game, null);

        assertEquals(3, game.getLevelManager().getCurrentLevel());
        assertTrue(outContent.toString().contains("恭喜通关"));
    }

    @Test
    public void testSleepWithoutPasswordLevel5() throws Exception {
        advanceToLevel(5);
        setCurrentRoom(dormitory());
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));
        game.getLevelManager().markDormitorySubmitCompleted();

        sleepCommand.execute(game, null);

        assertTrue(outContent.toString().contains(SleepCommand.PASSWORD_REQUIRED_MESSAGE));
        assertEquals(5, game.getLevelManager().getCurrentLevel());
    }

    @Test
    public void testSleepSuccessLevel5WinsGame() throws Exception {
        advanceToLevel(5);
        setCurrentRoom(dormitory());
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));
        game.getLevelManager().markDormitorySubmitCompleted();
        game.getLevelManager().markDormitoryPasswordUnlocked();

        sleepCommand.execute(game, null);

        assertEquals(LevelState.GAME_WON, game.getLevelManager().getState());
        assertTrue(game.getLevelManager().isGameWon());
        assertTrue(outContent.toString().contains("五关全部通关"));
    }
}

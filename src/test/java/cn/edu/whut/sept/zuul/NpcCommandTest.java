/**
 * NPC 命令单元测试：talk 对话与扣时（E8 #25）。
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

import cn.edu.whut.sept.zuul.command.TalkCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * TalkCommand 功能测试。
 */
public class NpcCommandTest {

    private Game game;
    private Player player;
    private TalkCommand talkCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        talkCommand = new TalkCommand();
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
    public void testTalkWithSupermarketNpc() throws Exception {
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        talkCommand.execute(game, null);

        assertEquals(beforeSeconds - 30, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("宿管阿姨"));
        assertTrue(outContent.toString().contains("use"));
    }

    @Test
    public void testTalkWithVolunteerLevel1() throws Exception {
        setCurrentRoom(game.getRoomById(UseCommand.NORTH_BUILDING_ROOM_ID));

        talkCommand.execute(game, null);

        assertTrue(outContent.toString().contains("归寝单下周才查"));
    }

    @Test
    public void testTalkWithVolunteerLevel2IssuesDormForm() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.NORTH_BUILDING_ROOM_ID));

        talkCommand.execute(game, null);

        assertTrue(outContent.toString().contains("归寝单"));
        assertNotNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
    }

    @Test
    public void testTalkWithVolunteerLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(UseCommand.NORTH_BUILDING_ROOM_ID));

        talkCommand.execute(game, null);

        assertTrue(outContent.toString().contains("志愿者"));
    }

    @Test
    public void testTalkInRoomWithoutNpc() throws Exception {
        setCurrentRoom(game.getRoomById("gate"));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        talkCommand.execute(game, null);

        assertEquals(beforeSeconds, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("没有可以对话"));
    }

    @Test
    public void testTalkCommandRegistered() {
        String[] words = game.getCommandManager().getCommandWords();
        assertTrue(containsCommand(words, "talk"));
    }

    @Test
    public void testCheckinCommandRemoved() {
        String[] words = game.getCommandManager().getCommandWords();
        assertTrue(!containsCommand(words, "checkin"));
    }

    @Test
    public void testHelpShowsTalkDescription() {
        outContent.reset();
        game.processCommand(new Command("help", null));

        String output = outContent.toString();
        assertTrue(output.contains(TalkCommand.getUsageDescription()));
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

/**
 * NPC 命令单元测试：talk/register 对话、登记与扣时（E8 #25）。
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

import cn.edu.whut.sept.zuul.command.RegisterCommand;
import cn.edu.whut.sept.zuul.command.TalkCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.npc.NpcService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TalkCommand 与 RegisterCommand 功能测试。
 */
public class NpcCommandTest {

    private Game game;
    private Player player;
    private TalkCommand talkCommand;
    private RegisterCommand registerCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        talkCommand = new TalkCommand();
        registerCommand = new RegisterCommand();
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
    public void testRegisterCampusCardInSupermarketLevel1() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        registerCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
        assertEquals(beforeSeconds - 30, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("宿管阿姨"));
    }

    @Test
    public void testRegisterCampusCardWhenAlreadyHas() throws Exception {
        Room supermarket = game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID);
        setCurrentRoom(supermarket);
        player.takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 5));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        registerCommand.execute(game, null);

        assertEquals(beforeSeconds, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("已经有一卡通"));
    }

    @Test
    public void testRegisterCampusCardInWrongRoom() throws Exception {
        setCurrentRoom(game.getRoomById("gate"));

        registerCommand.execute(game, null);

        assertNull(player.findItemInInventory(GatedRoom.CAMPUS_CARD_ITEM));
        assertTrue(outContent.toString().contains("没有可以办理登记"));
    }

    @Test
    public void testRegisterDormFormAtVolunteerLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));

        registerCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertTrue(outContent.toString().contains("志愿者"));
    }

    @Test
    public void testRegisterDormFormAtVolunteerLevel1Fails() throws Exception {
        setCurrentRoom(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));

        registerCommand.execute(game, null);

        assertNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertTrue(outContent.toString().contains("暂无归寝单登记服务"));
    }

    @Test
    public void testRegisterDormFormAtVolunteerLevel4Redirects() throws Exception {
        advanceToLevel(4);
        setCurrentRoom(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));

        registerCommand.execute(game, null);

        assertNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertTrue(outContent.toString().contains("图书馆"));
    }

    @Test
    public void testRegisterDormFormAtLibraryLevel4() throws Exception {
        advanceToLevel(4);
        setCurrentRoom(game.getRoomById(UseCommand.LIBRARY_ROOM_ID));

        registerCommand.execute(game, null);

        assertNotNull(player.findItemInInventory(UseCommand.DORM_FORM_ITEM));
        assertTrue(outContent.toString().contains("图书馆工作人员"));
    }

    @Test
    public void testRegisterDormFormWhenAlreadyHas() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));
        player.takeItem(new Item(UseCommand.DORM_FORM_ITEM, 5));

        registerCommand.execute(game, null);

        assertTrue(outContent.toString().contains("已经领过归寝单"));
    }

    @Test
    public void testTalkWithSupermarketNpc() throws Exception {
        setCurrentRoom(game.getRoomById(UseCommand.SUPERMARKET_ROOM_ID));

        int beforeSeconds = game.getLevelTimer().getRemainingSeconds();
        talkCommand.execute(game, null);

        assertEquals(beforeSeconds - 30, game.getLevelTimer().getRemainingSeconds());
        assertTrue(outContent.toString().contains("宿管阿姨"));
    }

    @Test
    public void testTalkWithVolunteerLevel2() throws Exception {
        advanceToLevel(2);
        setCurrentRoom(game.getRoomById(NpcService.NORTH_BUILDING_ROOM_ID));

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
    public void testNpcCommandsRegistered() {
        String[] words = game.getCommandManager().getCommandWords();
        assertTrue(containsCommand(words, "talk"));
        assertTrue(containsCommand(words, "register"));
    }

    @Test
    public void testHelpShowsNpcDescriptions() {
        outContent.reset();
        game.processCommand(new Command("help", null));

        String output = outContent.toString();
        assertTrue(output.contains(TalkCommand.getUsageDescription()));
        assertTrue(output.contains(RegisterCommand.getUsageDescription()));
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

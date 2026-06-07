package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.GoCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E13 条件门：图书馆/寝室须持一卡通；部分关卡寝室须先 submit。
 */
public class GatedRoomTest {

    private Game game;
    private Room pub;
    private Room office;
    private GatedRoom library;
    private GatedRoom dormitory;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        pub = game.getRoomById("pub");
        office = game.getRoomById("office");
        library = (GatedRoom) game.getRoomById("library");
        dormitory = (GatedRoom) game.getRoomById("dormitory");
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testLibraryDeniedWithoutCard() {
        game.resetPlayerPosition(pub);

        assertFalse(game.setCurrentRoom(library));
        assertEquals(pub, game.getCurrentRoom());
        assertTrue(out.toString().contains(GatedRoom.CARD_DENIED_MESSAGE));
    }

    @Test
    public void testLibraryAllowedWithCard() {
        game.resetPlayerPosition(pub);
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 50));

        assertTrue(game.setCurrentRoom(library));
        assertEquals(library, game.getCurrentRoom());
    }

    @Test
    public void testDormitoryDeniedWithoutCard() {
        game.resetPlayerPosition(office);

        assertFalse(game.setCurrentRoom(dormitory));
        assertEquals(office, game.getCurrentRoom());
        assertTrue(out.toString().contains(GatedRoom.CARD_DENIED_MESSAGE));
    }

    @Test
    public void testDormitoryAllowedOnLevelOneWithoutSubmit() {
        game.resetPlayerPosition(office);
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 50));

        assertTrue(game.setCurrentRoom(dormitory));
        assertEquals(dormitory, game.getCurrentRoom());
    }

    @Test
    public void testDormitoryDeniedOnLevelTwoWithoutSubmit() {
        enterLevelTwo();
        game.resetPlayerPosition(office);
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 50));

        assertFalse(game.setCurrentRoom(dormitory));
        assertEquals(office, game.getCurrentRoom());
        assertTrue(out.toString().contains(GatedRoom.SUBMIT_REQUIRED_MESSAGE));
    }

    @Test
    public void testDormitoryAllowedOnLevelTwoAfterSubmit() {
        enterLevelTwo();
        game.getLevelManager().markDormitorySubmitCompleted();
        game.resetPlayerPosition(office);
        game.getPlayer().takeItem(new Item(GatedRoom.CAMPUS_CARD_ITEM, 50));

        assertTrue(game.setCurrentRoom(dormitory));
        assertEquals(dormitory, game.getCurrentRoom());
    }

    @Test
    public void testGoCommandToLibraryRequiresCard() {
        game.resetPlayerPosition(pub);
        GoCommand goCommand = new GoCommand();

        goCommand.execute(game, "north");

        assertEquals(pub, game.getCurrentRoom());
        assertTrue(out.toString().contains(GatedRoom.CARD_DENIED_MESSAGE));
    }

    @Test
    public void testSubmitFlagResetsOnLevelRestart() {
        enterLevelTwo();
        game.getLevelManager().markDormitorySubmitCompleted();
        game.getLevelManager().restartCurrentLevel();

        assertFalse(game.getLevelManager().isDormitorySubmitCompleted());
    }

    private void enterLevelTwo() {
        game.getLevelManager().completeCurrentLevel();
        assertEquals(2, game.getLevelManager().getCurrentLevel());
    }
}

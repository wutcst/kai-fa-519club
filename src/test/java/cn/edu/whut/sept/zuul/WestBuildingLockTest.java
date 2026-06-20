package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.BackCommand;
import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.LevelManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E14 博学西楼困锁：进楼触发、无锤子无法离开、第三关起生效。
 */
public class WestBuildingLockTest {

    private Game game;
    private Room westBuilding;
    private Room boxueMain;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        game = new Game();
        westBuilding = game.getRoomById(UseCommand.WEST_BUILDING_ROOM_ID);
        boxueMain = game.getRoomById("boxue_main");
        System.setOut(new PrintStream(out));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testNoLockBelowLevelThree() {
        assertEquals(1, game.getLevelManager().getCurrentLevel());

        game.setCurrentRoom(westBuilding);

        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
    }

    @Test
    public void testEnterWestBuildingTriggersLockFromLevelThree() {
        advanceToLevel(3);
        out.reset();

        game.setCurrentRoom(westBuilding);

        assertTrue(game.getLevelManager().isWestBuildingExitLocked());
        assertTrue(out.toString().contains("门从身后锁上了"));
    }

    @Test
    public void testGoEastBlockedWithoutHammer() {
        advanceToLevel(3);
        game.setCurrentRoom(westBuilding);
        out.reset();

        GoCommand goCommand = new GoCommand();
        goCommand.execute(game, "east");

        assertEquals(westBuilding, game.getCurrentRoom());
        assertTrue(out.toString().contains("门被从内侧锁死了"));
    }

    private void enterWestFromMain() {
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        game.setCurrentRoom(boxueMain);
        game.setCurrentRoom(westBuilding);
    }

    @Test
    public void testBackBlockedWithoutHammer() {
        advanceToLevel(3);
        enterWestFromMain();
        out.reset();

        BackCommand backCommand = new BackCommand();
        backCommand.execute(game, null);

        assertEquals(westBuilding, game.getCurrentRoom());
        assertTrue(out.toString().contains(LevelManager.WEST_BUILDING_TRAP_MESSAGE));
        assertFalse(out.toString().contains("无法返回，这是你的起始房间"));
    }

    @Test
    public void testUseHammerUnlocksExitAndAllowsGoEast() {
        advanceToLevel(3);
        game.setCurrentRoom(westBuilding);
        game.getPlayer().takeItem(new Item(UseCommand.HAMMER_ITEM, 200));

        UseCommand useCommand = new UseCommand();
        useCommand.execute(game, UseCommand.HAMMER_ITEM);
        game.getPlayer().takeItem(new Item(DarkRoom.FLASHLIGHT_ITEM, 200));
        out.reset();

        GoCommand goCommand = new GoCommand();
        goCommand.execute(game, "east");

        assertEquals(boxueMain, game.getCurrentRoom());
        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
    }

    @Test
    public void testRestartLevelResetsWestLock() {
        advanceToLevel(3);
        game.setCurrentRoom(westBuilding);
        assertTrue(game.getLevelManager().isWestBuildingExitLocked());

        game.getLevelManager().restartCurrentLevel();

        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
    }

    @Test
    public void testBackAllowedAfterHammerUnlock() {
        advanceToLevel(3);
        enterWestFromMain();
        game.getPlayer().takeItem(new Item(UseCommand.HAMMER_ITEM, 200));
        new UseCommand().execute(game, UseCommand.HAMMER_ITEM);
        out.reset();

        new BackCommand().execute(game, null);

        assertEquals(boxueMain, game.getCurrentRoom());
        assertTrue(out.toString().contains("你回到了上一个房间"));
    }

    @Test
    public void testUnlockedWestDoesNotRelockOnReentry() {
        advanceToLevel(3);
        game.setCurrentRoom(westBuilding);
        game.getPlayer().takeItem(new Item(UseCommand.HAMMER_ITEM, 200));
        new UseCommand().execute(game, UseCommand.HAMMER_ITEM);

        game.setCurrentRoom(boxueMain);
        out.reset();
        game.setCurrentRoom(westBuilding);

        assertFalse(game.getLevelManager().isWestBuildingExitLocked());
        assertFalse(out.toString().contains("门从身后锁上了"));
    }

    private void advanceToLevel(int targetLevel) {
        while (game.getLevelManager().getCurrentLevel() < targetLevel) {
            game.getLevelManager().completeCurrentLevel();
        }
    }
}

/**
 * 测试 EatCookieCommand（eat）功能
 *
 * @author liujing
 * @version 1.6
 */
package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.command.EatCookieCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EatCookieCommandTest {
    private Game game;
    private Player player;
    private EatCookieCommand eatCookieCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        player = game.getPlayer();
        eatCookieCommand = new EatCookieCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testEatMagicCookie() {
        player.takeItem(new Item("magic cookie", 100));

        int initialMaxWeight = player.getMaxWeight();
        int initialCurrentWeight = player.getCurrentWeight();
        int beforeTime = game.getLevelTimer().getRemainingSeconds();

        outContent.reset();
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("你吃掉了 magic cookie"));
        assertTrue(output.contains("食用耗时 " + ActionTimeCost.EAT + " 秒的同时"));
        assertTrue(output.contains("魔法饼干让你多争取了 " + ActionTimeCost.COOKIE_BONUS + " 秒"));
        assertEquals(
                beforeTime - ActionTimeCost.EAT + ActionTimeCost.COOKIE_BONUS,
                game.getLevelTimer().getRemainingSeconds());
        assertTrue(output.contains("最大负重增加了 1000g"));
        assertEquals(initialMaxWeight + 1000, player.getMaxWeight());
        assertEquals(0, player.getInventory().size());
        assertEquals(initialCurrentWeight - 100, player.getCurrentWeight());
    }

    @Test
    public void testEatWithoutFood() {
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("背包里没有可以吃的食物"));
        assertEquals(3000, player.getMaxWeight());
    }

    @Test
    public void testEatMilkTeaExtraDiarrheaPenalty() {
        player.takeItem(new Item(FoodItems.MILK_TEA_ITEM, 100));
        int beforeTime = game.getLevelTimer().getRemainingSeconds();

        outContent.reset();
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("好像拉肚子了"));
        assertEquals(beforeTime - ActionTimeCost.EAT - ActionTimeCost.MILK_TEA_DIARRHEA,
                game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testEatOtherFoodDeductsTime() {
        player.takeItem(new Item("一个辣椒包", 30));
        int beforeTime = game.getLevelTimer().getRemainingSeconds();

        outContent.reset();
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("你吃掉了 一个辣椒包"));
        assertTrue(output.contains("除了填饱肚子，似乎没什么特别的效果"));
        assertEquals(beforeTime - ActionTimeCost.EAT, game.getLevelTimer().getRemainingSeconds());
        assertEquals(0, player.getInventory().size());
    }

    @Test
    public void testEatSpecificFoodByName() {
        player.takeItem(new Item("magic cookie", 100));
        outContent.reset();
        eatCookieCommand.execute(game, "magic cookie");

        assertTrue(outContent.toString().contains("你吃掉了 magic cookie"));
        assertEquals(0, player.getInventory().size());
    }

    @Test
    public void testEatWrongSecondWordFails() {
        player.takeItem(new Item("magic cookie", 100));
        outContent.reset();
        eatCookieCommand.execute(game, "cookie");

        assertTrue(outContent.toString().contains("背包里没有「cookie」或该物品不可食用"));
        assertEquals(1, player.getInventory().size());
    }

    @Test
    public void testEatCookieBonusOncePerLevel() {
        player.takeItem(new Item("magic cookie", 100));
        eatCookieCommand.execute(game, null);
        int afterFirstEat = game.getLevelTimer().getRemainingSeconds();

        player.takeItem(new Item("magic cookie", 100));
        outContent.reset();
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue(output.contains("本关魔法饼干加时已获得"));
        assertEquals(afterFirstEat - ActionTimeCost.EAT, game.getLevelTimer().getRemainingSeconds());
        assertFalse(game.getLevelManager().isMagicCookieBonusAvailable());
    }

    @Test
    public void testEatCookieBonusResetsOnLevelRestart() {
        player.takeItem(new Item("magic cookie", 100));
        eatCookieCommand.execute(game, null);
        assertFalse(game.getLevelManager().isMagicCookieBonusAvailable());

        game.getLevelManager().restartCurrentLevel();
        assertTrue(game.getLevelManager().isMagicCookieBonusAvailable());

        int beforeTime = game.getLevelTimer().getRemainingSeconds();
        player.takeItem(new Item("magic cookie", 100));
        outContent.reset();
        eatCookieCommand.execute(game, null);

        assertEquals(
                beforeTime - ActionTimeCost.EAT + ActionTimeCost.COOKIE_BONUS,
                game.getLevelTimer().getRemainingSeconds());
    }
}

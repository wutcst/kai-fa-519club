/**
 * 测试EatCookieCommand功能
 *
 * @author liujing
 * @version 1.5
 */
package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class EatCookieCommandTest {
    private Game game;
    private Player player;
    private EatCookieCommand eatCookieCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() throws Exception {
        // 创建游戏实例
        game = new Game();
        player = game.getPlayer();
        eatCookieCommand = new EatCookieCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * 测试吃掉魔法饼干
     */
    @Test
    public void testEatCookie() {
        // 玩家拾取魔法饼干
        Item magicCookie = new Item("magic cookie", 100);
        player.takeItem(magicCookie);

        int initialMaxWeight = player.getMaxWeight();
        int initialCurrentWeight = player.getCurrentWeight();

        // 清空输出
        outContent.reset();

        // 执行eat cookie命令
        eatCookieCommand.execute(game, "cookie");

        // 验证输出
        String output = outContent.toString();
        assertTrue("应显示吃掉饼干信息", output.contains("你吃掉了 magic cookie"));
        assertTrue("应显示负重增加", output.contains("最大负重增加了 1000g"));
        assertTrue("应显示当前最大负重", output.contains("当前最大负重: " + (initialMaxWeight + 1000) + "g"));
        assertTrue("应显示当前负重", output.contains("当前负重: " + (initialCurrentWeight - 100) + "g"));

        // 验证玩家最大负重增加
        assertEquals(initialMaxWeight + 1000, player.getMaxWeight());

        // 验证饼干已从物品栏移除
        assertEquals(0, player.getInventory().size());
        assertEquals(initialCurrentWeight - 100, player.getCurrentWeight());
    }

    /**
     * 测试没有魔法饼干时执行eat cookie命令
     */
    @Test
    public void testEatCookieWithoutCookie() {
        eatCookieCommand.execute(game, "cookie");

        String output = outContent.toString();
        assertTrue("应提示没有魔法饼干", output.contains("你没有magic cookie可以吃"));
        assertTrue("应提示如何获取饼干", output.contains("使用 'take magic cookie' 命令拾取它"));

        // 验证最大负重未改变
        assertEquals(3000, player.getMaxWeight());
    }

    /**
     * 测试不带参数的eat命令
     */
    @Test
    public void testEatWithoutParameter() {
        eatCookieCommand.execute(game, null);

        String output = outContent.toString();
        assertTrue("应提示需要指定参数", output.contains("Eat what? 请使用 'eat cookie' 命令"));
    }

    /**
     * 测试带错误参数的eat命令
     */
    @Test
    public void testEatWithWrongParameter() {
        eatCookieCommand.execute(game, "apple");

        String output = outContent.toString();
        assertTrue("应提示只能吃cookie", output.contains("Eat apple? 请使用 'eat cookie' 命令"));
    }

    /**
     * 测试带额外参数的eat命令
     */
    @Test
    public void testEatWithExtraParameter() {
        // 玩家有魔法饼干
        player.takeItem(new Item("magic cookie", 100));

        // 清空输出
        outContent.reset();

        // 执行eat命令，带额外参数
        eatCookieCommand.execute(game, "cookie now");

        // 应该仍然能吃掉饼干（只检查第一个参数）
        String output = outContent.toString();
        assertTrue("应能吃掉饼干", output.contains("你吃掉了 magic cookie"));

        // 验证饼干已吃掉
        assertEquals(0, player.getInventory().size());
    }

    /**
     * 测试吃掉魔法饼干后的负重计算
     */
    @Test
    public void testWeightCalculationAfterEatingCookie() {
        // 玩家携带一些物品
        player.takeItem(new Item("重物", 2500));
        player.takeItem(new Item("magic cookie", 100));

        int initialMaxWeight = player.getMaxWeight();
        int initialCurrentWeight = player.getCurrentWeight();

        // 吃掉饼干
        eatCookieCommand.execute(game, "cookie");

        // 验证负重计算正确
        assertEquals(initialMaxWeight + 1000, player.getMaxWeight());
        assertEquals(initialCurrentWeight - 100, player.getCurrentWeight());
        assertEquals(player.getMaxWeight() - player.getCurrentWeight(), player.getRemainingCapacity());
    }
}
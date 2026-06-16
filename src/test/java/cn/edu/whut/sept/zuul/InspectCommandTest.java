package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.InspectCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.Assert.assertTrue;

/**
 * InspectCommand 单元测试。
 */
public class InspectCommandTest {

    private Game game;
    private InspectCommand inspectCommand;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() {
        game = new Game();
        inspectCommand = new InspectCommand();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testInspectItemInInventory() {
        game.getPlayer().takeItem(new Item(UseCommand.MONEY_ITEM, 10));

        inspectCommand.execute(game, UseCommand.MONEY_ITEM);

        String output = outContent.toString();
        assertTrue(output.contains(UseCommand.MONEY_ITEM));
        assertTrue(output.contains("一卡通"));
    }

    @Test
    public void testInspectMissingItem() {
        inspectCommand.execute(game, "不存在的物品");

        String output = outContent.toString();
        assertTrue(output.contains("背包里没有"));
    }
}

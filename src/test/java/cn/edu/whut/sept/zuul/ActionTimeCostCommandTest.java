package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.command.DropCommand;
import cn.edu.whut.sept.zuul.command.GoCommand;
import cn.edu.whut.sept.zuul.command.InspectCommand;
import cn.edu.whut.sept.zuul.command.ItemsCommand;
import cn.edu.whut.sept.zuul.command.LookCommand;
import cn.edu.whut.sept.zuul.command.TakeCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

import static org.junit.Assert.assertEquals;

/**
 * E11 操作耗时：各命令成功后扣秒，查看背包不扣时。
 */
public class ActionTimeCostCommandTest {

    private Game game;
    private GoCommand goCommand;
    private LookCommand lookCommand;
    private TakeCommand takeCommand;
    private DropCommand dropCommand;
    private ItemsCommand itemsCommand;
    private InspectCommand inspectCommand;

    @Before
    public void setUp() {
        game = new Game();
        goCommand = new GoCommand();
        lookCommand = new LookCommand();
        takeCommand = new TakeCommand();
        dropCommand = new DropCommand();
        itemsCommand = new ItemsCommand();
        inspectCommand = new InspectCommand();
    }

    @Test
    public void testGoDeductsOnSuccessfulMove() {
        int before = game.getLevelTimer().getRemainingSeconds();
        goCommand.execute(game, "north");
        assertEquals(before - ActionTimeCost.GO, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testGoDoesNotDeductWhenBlocked() {
        int before = game.getLevelTimer().getRemainingSeconds();
        goCommand.execute(game, "up");
        assertEquals(before, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testLookDeductsTime() {
        int before = game.getLevelTimer().getRemainingSeconds();
        lookCommand.execute(game, null);
        assertEquals(before - ActionTimeCost.LOOK, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testTakeDeductsOnSuccess() {
        goCommand.execute(game, "north");
        goCommand.execute(game, "north");
        int before = game.getLevelTimer().getRemainingSeconds();
        takeCommand.execute(game, "湿漉漉的三十元钱");
        assertEquals(before - ActionTimeCost.TAKE, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testTakeDoesNotDeductWhenItemMissing() {
        int before = game.getLevelTimer().getRemainingSeconds();
        takeCommand.execute(game, "不存在的物品");
        assertEquals(before, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testDropDeductsOnSuccess() {
        goCommand.execute(game, "north");
        goCommand.execute(game, "north");
        takeCommand.execute(game, "湿漉漉的三十元钱");
        int before = game.getLevelTimer().getRemainingSeconds();
        dropCommand.execute(game, "湿漉漉的三十元钱");
        assertEquals(before - ActionTimeCost.DROP, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testItemsDoesNotDeductTime() {
        int before = game.getLevelTimer().getRemainingSeconds();
        itemsCommand.execute(game, null);
        assertEquals(before, game.getLevelTimer().getRemainingSeconds());
    }

    @Test
    public void testInspectDoesNotDeductTime() {
        goCommand.execute(game, "north");
        goCommand.execute(game, "north");
        takeCommand.execute(game, "湿漉漉的三十元钱");
        int before = game.getLevelTimer().getRemainingSeconds();
        inspectCommand.execute(game, "湿漉漉的三十元钱");
        assertEquals(before, game.getLevelTimer().getRemainingSeconds());
    }
}

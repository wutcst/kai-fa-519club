package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class HelpCommandSingleTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        TestUtils.clearOutput();
        TestUtils.redirectOutput();
    }

    // 测试：Help命令 → 不触发退出（核心逻辑）
    @Test
    void testHelpCommandNoQuit() {
        Command helpCommand = new Command("help", null);
        boolean isQuit = game.processCommand(helpCommand);
        // 核心断言：不触发退出
        assertFalse(isQuit, "help命令不应触发退出");
    }
}
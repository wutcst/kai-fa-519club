package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuitCommandSingleTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        TestUtils.clearOutput();
        TestUtils.redirectOutput();
    }

    // 测试：Quit命令-无参数 → 触发退出（核心逻辑）
    @Test
    void testQuitCommand_NoParam() {
        Command quitCommand = new Command("quit", null);
        boolean isQuit = game.processCommand(quitCommand);
        // 核心断言：返回true（触发退出）
        assertTrue(isQuit, "无参数quit应返回true");
    }

    // 测试：Quit命令-带参数 → 不触发退出（核心逻辑）
    @Test
    void testQuitCommand_WithParam() {
        Command quitWithParamCommand = new Command("quit", "test");
        boolean isQuit = game.processCommand(quitWithParamCommand);
        // 核心断言：返回false
        assertFalse(isQuit, "带参数quit应返回false");
    }
}
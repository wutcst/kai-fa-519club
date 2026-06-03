package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LookCommandSingleTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        TestUtils.clearOutput();
        TestUtils.redirectOutput();
    }

    // 测试：Look命令 → 不触发退出 + 房间信息正确（核心逻辑）
    @Test
    void testLookCommand_NoQuit() {
        Command lookCommand = new Command("look", null);
        boolean isQuit = game.processCommand(lookCommand);
        // 核心断言1：不触发退出
        assertFalse(isQuit, "look命令不应触发退出");
        // 核心断言2：当前房间仍为初始房间（look不切换房间）
        assertTrue(game.getCurrentRoom().getShortDescription().toLowerCase().contains("outside")
                || game.getCurrentRoom().getShortDescription().toLowerCase().contains("university"));
    }
}
package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandManager单元测试：验证命令注册、执行逻辑（兼容自定义扩展）
 * @author liujing
 * @version 1.1
 */
public class CommandManagerTest {
    private CommandManager commandManager;
    private Game game;

    @BeforeEach
    void setUp() {
        // 初始化命令管理器和游戏实例
        commandManager = new CommandManager();
        game = new Game();
        // 注册扩展的Look命令（若已注册则跳过）
        commandManager.registerCommand(new LookCommand());
        // 每次测试前清空输出缓存，避免跨测试干扰
        TestUtils.clearOutput();
        TestUtils.redirectOutput();
    }

    @AfterEach
    void tearDown() {
        // 恢复控制台输出
        TestUtils.restoreOutput();
    }

    /**
     * 测试1：命令管理器能正确注册所有核心命令
     */
    @Test
    void testCommandRegistration() {
        String[] commandWords = commandManager.getCommandWords();
        // 验证包含核心命令关键词（不依赖拼写/扩展）
        assertTrue(containsCommand(commandWords, "go"));
        assertTrue(containsCommand(commandWords, "help"));
        assertTrue(containsCommand(commandWords, "quit"));
        assertTrue(containsCommand(commandWords, "look"));
        // 验证不包含无效命令
        assertFalse(containsCommand(commandWords, "test"));
    }

    /**
     * 测试2：执行有效Go命令（east）能切换房间（弱化房间描述校验）
     */
    @Test
    void testExecuteValidGoCommand() {
        // 执行go east命令
        boolean isQuit = commandManager.executeCommand("go", "east", game);
        // 核心验证1：未触发退出
        assertFalse(isQuit);
        // 核心验证2：房间描述包含"lecture"（兼容theatre/theater）
        String roomDesc = game.getCurrentRoom().getShortDescription();
        assertTrue(roomDesc.contains("lecture"),
                "房间描述应包含'lecture'，实际：" + roomDesc);
    }

    /**
     * 测试3：执行Go命令无参数时提示核心语义（兼容自定义提示语）
     */
    @Test
    void testExecuteGoCommandWithoutParam() {
        // 执行go命令（无参数）
        commandManager.executeCommand("go", null, game);
        // 过滤无关输出后，校验核心提示（兼容"Go where?"/"去哪？"等）
        String output = TestUtils.getFilteredOutput();
        assertTrue(output.contains("where") || output.contains("去哪"),
                "无参数Go命令应提示方向，实际输出：" + output);
    }

    /**
     * 测试4：执行Go命令绝对无效方向（up）时提示无出口（兼容自定义提示语）
     */
    @Test
    void testExecuteGoCommandInvalidDirection() {
        // 选绝对无效的方向（up），避免用户扩展房间导致north变有效
        commandManager.executeCommand("go", "up", game);
        // 过滤无关输出后，校验核心提示（兼容"There is no door!"/"无出口"等）
        String output = TestUtils.getFilteredOutput();
        assertTrue(output.contains("no door") || output.contains("无出口") || output.contains("没有门"),
                "无效方向应提示无出口，实际输出：" + output);
    }

    /**
     * 测试5：执行Quit命令无参数时触发退出（核心逻辑，无关提示）
     */
    @Test
    void testExecuteValidQuitCommand() {
        // 执行quit命令（无参数），只验证返回值（核心逻辑）
        boolean isQuit = commandManager.executeCommand("quit", null, game);
        assertTrue(isQuit, "无参数Quit命令应返回true（触发退出）");
    }

    /**
     * 测试6：执行Quit命令带参数时提示核心语义（兼容自定义提示语）
     */
    @Test
    void testExecuteQuitCommandWithParam() {
        // 执行quit test命令
        boolean isQuit = commandManager.executeCommand("quit", "test", game);
        // 核心验证1：未触发退出
        assertFalse(isQuit);
        // 核心验证2：提示包含"quit what"或"退出什么"等
        String output = TestUtils.getFilteredOutput();
        assertTrue(output.contains("what") || output.contains("退出什么"),
                "带参数Quit命令应提示无效，实际输出：" + output);
    }

    /**
     * 测试7：执行无效命令时提示核心语义（兼容自定义提示语）
     */
    @Test
    void testExecuteInvalidCommand() {
        // 执行无效命令test
        commandManager.executeCommand("test", null, game);
        // 过滤无关输出后，校验核心提示（兼容"I don't know"/"不知道"等）
        String output = TestUtils.getFilteredOutput();
        assertTrue(output.contains("don't know") || output.contains("不知道") || output.contains("不理解"),
                "无效命令应提示不理解，实际输出：" + output);
    }

    /**
     * 辅助方法：检查命令数组是否包含指定命令
     */
    private boolean containsCommand(String[] commandWords, String target) {
        for (String cmd : commandWords) {
            if (cmd.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 完全绕过Parser输入，直接构造Command测试Go命令核心逻辑
 * 忽略所有扩展功能（magic cookie/物品/自定义提示）
 * @author liujing
 * @version 1.2
 */
public class GoCommandSingleTest {
    private Game game;

    @BeforeEach
    void setUp() {
        // 仅初始化Game，不处理任何输入输出（避免阻塞）
        game = new Game();
        // 可选：重定向输出过滤无关内容，不影响核心断言
        TestUtils.clearOutput();
        TestUtils.redirectOutput();
    }

    // 测试：Go命令-有效方向（east）→ 房间切换（核心逻辑）
    @Test
    void testGoCommand_ValidDirection_East() {
        // 直接构造Command对象，绕过Parser的输入读取（彻底避免阻塞）
        Command goEastCommand = new Command("go", "east");
        boolean isQuit = game.processCommand(goEastCommand);

        // 核心断言1：执行go命令不触发退出
        assertFalse(isQuit);
        // 核心断言2：房间切换（只校验关键词，忽略拼写theatre/theater）
        String roomDesc = game.getCurrentRoom().getShortDescription().toLowerCase();
        assertTrue(roomDesc.contains("lecture"),
                "执行go east后房间应包含'lecture'，实际：" + roomDesc);
    }

    // 测试：Go命令-无效方向（up）→ 不切换房间（核心逻辑）
    @Test
    void testGoCommand_InvalidDirection_Up() {
        // 保存初始房间
        Room initialRoom = game.getCurrentRoom();
        // 直接构造无效方向的Command
        Command goUpCommand = new Command("go", "up");
        game.processCommand(goUpCommand);

        // 核心断言：房间未切换（忽略提示语，只测核心逻辑）
        assertSame(initialRoom, game.getCurrentRoom(), "无效方向应保持原房间");
    }

    // 测试：Go命令-无参数 → 不切换房间（核心逻辑）
    @Test
    void testGoCommand_NoParam() {
        Room initialRoom = game.getCurrentRoom();
        Command goNoParamCommand = new Command("go", null);
        game.processCommand(goNoParamCommand);

        // 核心断言：房间未切换
        assertSame(initialRoom, game.getCurrentRoom(), "无参数go命令应保持原房间");
    }
}
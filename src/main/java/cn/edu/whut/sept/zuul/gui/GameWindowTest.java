/**
 * GameWindow类单元测试
 *
 * @author liujing
 * @version 2.0
 */
        package cn.edu.whut.sept.zuul.gui;

import cn.edu.whut.sept.zuul.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试图形界面功能
 */
public class GameWindowTest {
    private Game game;
    private GameWindow gameWindow;

    /**
     * 测试前的准备工作
     */
    @BeforeEach
    public void setUp() {
        game = new Game();
        // 在EDT中创建窗口但不显示
        try {
            SwingUtilities.invokeAndWait(() -> {
                gameWindow = new GameWindow(game);
                // 不显示窗口，只测试创建
            });
        } catch (Exception e) {
            fail("创建GameWindow失败: " + e.getMessage());
        }
    }

    /**
     * 测试窗口初始化
     */
    @Test
    public void testWindowInitialization() {
        assertNotNull(gameWindow, "GameWindow实例不应为null");
        assertEquals("World of Zuul - 图形化界面", gameWindow.getTitle(), "窗口标题不正确");
        assertEquals(JFrame.EXIT_ON_CLOSE, gameWindow.getDefaultCloseOperation(), "关闭操作不正确");
        assertTrue(gameWindow.getSize().width > 0 && gameWindow.getSize().height > 0, "窗口大小应大于0");
    }

    /**
     * 测试组件创建
     */
    @Test
    public void testComponentsCreation() {
        try {
            // 使用反射访问私有字段
            Field outputAreaField = GameWindow.class.getDeclaredField("outputArea");
            outputAreaField.setAccessible(true);
            JTextArea outputArea = (JTextArea) outputAreaField.get(gameWindow);

            assertNotNull(outputArea, "输出区域不应为null");
            assertFalse(outputArea.isEditable(), "输出区域不应可编辑");

            Field inputFieldField = GameWindow.class.getDeclaredField("inputField");
            inputFieldField.setAccessible(true);
            JTextField inputField = (JTextField) inputFieldField.get(gameWindow);

            assertNotNull(inputField, "输入框不应为null");
            assertTrue(inputField.isEditable(), "输入框应可编辑");

        } catch (Exception e) {
            fail("访问组件失败: " + e.getMessage());
        }
    }

    /**
     * 测试命令处理
     */
    @Test
    public void testCommandProcessing() {
        try {
            // 使用反射调用私有方法
            java.lang.reflect.Method processCommandMethod = GameWindow.class.getDeclaredMethod(
                    "processCommand", String.class);
            processCommandMethod.setAccessible(true);

            // 调用帮助命令
            processCommandMethod.invoke(gameWindow, "help");

            // 验证命令管理器工作正常
            String[] commands = game.getCommandManager().getCommandWords();
            assertTrue(commands.length > 0, "应有至少一个命令");

            // 检查是否包含基本命令
            boolean hasGo = false;
            boolean hasQuit = false;
            boolean hasHelp = false;

            for (String cmd : commands) {
                if (cmd.equals("go")) hasGo = true;
                if (cmd.equals("quit")) hasQuit = true;
                if (cmd.equals("help")) hasHelp = true;
            }

            assertTrue(hasGo, "应包含go命令");
            assertTrue(hasQuit, "应包含quit命令");
            assertTrue(hasHelp, "应包含help命令");

        } catch (Exception e) {
            fail("命令处理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试界面更新
     */
    @Test
    public void testDisplayUpdate() {
        try {
            // 使用反射调用私有方法
            java.lang.reflect.Method updateDisplayMethod = GameWindow.class.getDeclaredMethod("updateGameDisplay");
            updateDisplayMethod.setAccessible(true);
            updateDisplayMethod.invoke(gameWindow);

            // 验证玩家和房间信息可访问
            assertNotNull(game.getPlayer(), "玩家不应为null");
            assertNotNull(game.getCurrentRoom(), "当前房间不应为null");
            assertNotNull(game.getPlayer().getName(), "玩家名称不应为null");
            assertNotNull(game.getCurrentRoom().getShortDescription(), "房间描述不应为null");

        } catch (Exception e) {
            fail("显示更新测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试游戏功能
     */
    @Test
    public void testGameFeatures() {
        // 验证游戏基本功能
        assertNotNull(game.getPlayer(), "玩家不应为null");
        assertNotNull(game.getCurrentRoom(), "当前房间不应为null");
        assertNotNull(game.getParser(), "解析器不应为null");
        assertNotNull(game.getCommandManager(), "命令管理器不应为null");

        // 验证玩家初始状态
        assertEquals(3000, game.getPlayer().getMaxWeight(), "玩家最大负重应为3000");
        assertEquals(0, game.getPlayer().getCurrentWeight(), "玩家初始负重应为0");

        // 验证命令可用
        String[] commands = game.getCommandManager().getCommandWords();
        assertTrue(commands.length >= 8, "应有至少8个命令");
    }
}
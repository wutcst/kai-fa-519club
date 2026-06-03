
/**
 * EnhancedGameWindow类单元测试
 * 新增：测试增强图形界面的初始化和高级功能
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
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试增强图形界面功能
 */
public class EnhancedGameWindowTest {
    private Game game;
    private EnhancedGameWindow enhancedWindow;

    /**
     * 测试前的准备工作
     */
    @BeforeEach
    public void setUp() {
        game = new Game();
        // 在EDT中创建窗口但不显示
        try {
            SwingUtilities.invokeAndWait(() -> {
                enhancedWindow = new EnhancedGameWindow(game);
                // 不显示窗口，只测试创建
            });
        } catch (Exception e) {
            fail("创建EnhancedGameWindow失败: " + e.getMessage());
        }
    }

    /**
     * 测试窗口初始化
     */
    @Test
    public void testWindowInitialization() {
        assertNotNull(enhancedWindow, "EnhancedGameWindow实例不应为null");
        assertEquals("World of Zuul - 增强图形界面", enhancedWindow.getTitle(), "窗口标题不正确");
        assertEquals(JFrame.EXIT_ON_CLOSE, enhancedWindow.getDefaultCloseOperation(), "关闭操作不正确");
        assertTrue(enhancedWindow.getSize().width > 0 && enhancedWindow.getSize().height > 0, "窗口大小应大于0");
    }

    /**
     * 测试图像加载器
     */
    @Test
    public void testImageLoader() {
        try {
            // 使用反射访问ImageLoader字段
            Field imageLoaderField = EnhancedGameWindow.class.getDeclaredField("imageLoader");
            imageLoaderField.setAccessible(true);
            ImageLoader imageLoader = (ImageLoader) imageLoaderField.get(enhancedWindow);

            assertNotNull(imageLoader, "ImageLoader不应为null");

            // 验证图像加载器工作
            ImageLoader singleton = ImageLoader.getInstance();
            assertSame(imageLoader, singleton, "ImageLoader应为单例");

        } catch (Exception e) {
            fail("图像加载器测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试方向按钮
     */
    @Test
    public void testDirectionButtons() {
        try {
            // 使用反射调用创建方向按钮的方法
            Method createDirectionButtonsMethod = EnhancedGameWindow.class.getDeclaredMethod(
                    "createDirectionButtons");
            createDirectionButtonsMethod.setAccessible(true);
            JPanel directionPanel = (JPanel) createDirectionButtonsMethod.invoke(enhancedWindow);

            assertNotNull(directionPanel, "方向按钮面板不应为null");

            // 检查面板布局
            assertTrue(directionPanel.getLayout() instanceof GridLayout, "应使用GridLayout");

            // 检查组件数量 (3x3网格应有9个组件)
            Component[] components = directionPanel.getComponents();
            assertEquals(9, components.length, "方向按钮面板应有9个组件");

        } catch (Exception e) {
            fail("方向按钮测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试游戏命令
     */
    @Test
    public void testGameCommands() {
        // 验证命令管理器工作正常
        assertNotNull(game.getCommandManager(), "命令管理器不应为null");

        String[] commands = game.getCommandManager().getCommandWords();
        assertTrue(commands.length > 0, "应有至少一个命令");

        // 检查增强界面需要的命令
        boolean hasLook = false;
        boolean hasItems = false;
        boolean hasTake = false;
        boolean hasEat = false;

        for (String cmd : commands) {
            if (cmd.equals("look")) hasLook = true;
            if (cmd.equals("items")) hasItems = true;
            if (cmd.equals("take")) hasTake = true;
            if (cmd.equals("eat")) hasEat = true;
        }

        assertTrue(hasLook, "应包含look命令");
        assertTrue(hasItems, "应包含items命令");
        assertTrue(hasTake, "应包含take命令");
        assertTrue(hasEat, "应包含eat命令");
    }
}
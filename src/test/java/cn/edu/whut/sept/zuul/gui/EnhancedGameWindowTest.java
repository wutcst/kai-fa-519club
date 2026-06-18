package cn.edu.whut.sept.zuul.gui;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * EnhancedGameWindow 阶段 1 单元测试。
 */
public class EnhancedGameWindowTest {

    private Game game;
    private EnhancedGameWindow window;

    @BeforeEach
    public void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Skipping GUI test: no display (headless CI)");
        ImageLoader.resetForTest();
        game = new Game();
        try {
            SwingUtilities.invokeAndWait(() -> window = new EnhancedGameWindow(game));
        } catch (Exception exception) {
            fail("创建 EnhancedGameWindow 失败: " + exception.getMessage());
        }
    }

    @Test
    public void testWindowInitialization() {
        assertNotNull(window);
        assertEquals("熄灯前归寝 - 图形界面", window.getTitle());
        assertEquals(JFrame.DO_NOTHING_ON_CLOSE, window.getDefaultCloseOperation());
        assertTrue(window.getSize().width > 0 && window.getSize().height > 0);
    }

    @Test
    public void testSceneAndInventoryPanelsCreated() {
        assertNotNull(window.getScenePanelForTest());
        assertNotNull(window.getInventoryPanelForTest());
        assertNotNull(window.getControllerForTest());
    }

    @Test
    public void testAutoTickEnabledOnCreate() {
        assertTrue(game.getLevelTimer().isAutoTickEnabled());
        assertNotNull(window.getGlassModalLayerForTest());
        window.getControllerForTest().shutdownGuiSession(game);
    }

    @Test
    public void testRequiredCommandsExist() {
        String[] commands = game.getCommandManager().getCommandWords();
        assertTrue(contains(commands, "look"));
        assertTrue(contains(commands, "take"));
        assertTrue(contains(commands, "use"));
        assertTrue(contains(commands, "sleep"));
        window.getControllerForTest().shutdownGuiSession(game);
    }

    private boolean contains(String[] commands, String target) {
        for (String command : commands) {
            if (target.equals(command)) {
                return true;
            }
        }
        return false;
    }
}

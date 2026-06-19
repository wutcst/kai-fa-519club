package cn.edu.whut.sept.zuul.gui;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.persistence.InMemoryGameTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T3：Vue GUI 桥接在 headless CI 下可运行；可选 Swing 窗口测试在无显示器时跳过。
 */
public class GameGuiHeadlessBridgeTest {

    @Test
    public void controllerBridgeRunsWithoutDisplay() {
        Game game = InMemoryGameTestSupport.createGameWithInMemoryPersistence();
        GameGuiController controller = new GameGuiController();
        controller.prepareGuiSession(game);

        GameGuiController.CommandResult result = controller.execute(game, "go", "north");
        assertFalse(result.isQuitRequested());
        assertEquals("boxue_main", game.getCurrentRoom().getRoomId());
        assertTrue(game.getLevelTimer().getDisplayText().contains("距熄灯（23:00）"));

        controller.shutdownGuiSession(game);
    }

    @Test
    public void helperBridgeExposesOutcomeDetection() {
        assertEquals(
            GuiOutcomeHelper.OutcomeType.NONE,
            GuiOutcomeHelper.detectFromOutput(java.util.List.of("普通输出")));
    }

    @Test
    public void optionalDisplayTestsSkippedInHeadlessCi() {
        Assumptions.assumeFalse(
            GraphicsEnvironment.isHeadless(),
            "Skipping optional display test: no AWT display (headless CI)");
        assertFalse(GraphicsEnvironment.isHeadless());
    }
}

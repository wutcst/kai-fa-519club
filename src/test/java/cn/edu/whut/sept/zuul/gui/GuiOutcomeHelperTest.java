package cn.edu.whut.sept.zuul.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GuiOutcomeHelper 单元测试（阶段 4 结局弹层）。
 */
public class GuiOutcomeHelperTest {

    private Game game;

    @BeforeEach
    public void setUp() {
        game = new Game();
    }

    @Test
    public void detectsLevelFailedFromOutput() {
        List<String> lines = Collections.singletonList("熄灯了！本关失败。");
        assertEquals(
            GuiOutcomeHelper.OutcomeType.LEVEL_FAILED,
            GuiOutcomeHelper.detectFromOutput(lines));
    }

    @Test
    public void detectsLevelPassedFromOutput() {
        List<String> lines = Collections.singletonList("恭喜通关第 1 关！");
        assertEquals(
            GuiOutcomeHelper.OutcomeType.LEVEL_PASSED,
            GuiOutcomeHelper.detectFromOutput(lines));
    }

    @Test
    public void detectsGameWonFromOutput() {
        List<String> lines = Arrays.asList(
            "恭喜通关第 5 关！",
            "五关全部通关，你赶在熄灯前回到了寝室！"
        );
        assertEquals(
            GuiOutcomeHelper.OutcomeType.GAME_WON,
            GuiOutcomeHelper.detectFromOutput(lines));
    }

    @Test
    public void gameWonTakesPriorityOverLevelPassed() {
        List<String> lines = Arrays.asList(
            "恭喜通关第 5 关！",
            "五关全部通关，你赶在熄灯前回到了寝室！"
        );
        assertEquals(GuiOutcomeHelper.OutcomeType.GAME_WON, GuiOutcomeHelper.detectFromOutput(lines));
    }

    @Test
    public void detectsTimeoutFromStateTransition() {
        assertEquals(
            GuiOutcomeHelper.OutcomeType.LEVEL_FAILED,
            GuiOutcomeHelper.detectFromStateTransition(LevelState.IN_PROGRESS, LevelState.FAILED));
    }

    @Test
    public void ignoresOtherStateTransitions() {
        assertEquals(
            GuiOutcomeHelper.OutcomeType.NONE,
            GuiOutcomeHelper.detectFromStateTransition(LevelState.FAILED, LevelState.IN_PROGRESS));
    }

    @Test
    public void buildsFailedOutcomeCopy() {
        assertEquals("熄灯了", GuiOutcomeHelper.buildTitle(GuiOutcomeHelper.OutcomeType.LEVEL_FAILED));
        assertEquals("重试本关", GuiOutcomeHelper.buildActionLabel(GuiOutcomeHelper.OutcomeType.LEVEL_FAILED));
        assertTrue(GuiOutcomeHelper.buildMessage(
            GuiOutcomeHelper.OutcomeType.LEVEL_FAILED, game, Collections.emptyList()
        ).contains("本关失败"));
    }

    @Test
    public void buildsPassedOutcomeCopy() {
        List<String> lines = Collections.singletonList("恭喜通关第 2 关！即将进入第 3 关。");
        String message = GuiOutcomeHelper.buildMessage(
            GuiOutcomeHelper.OutcomeType.LEVEL_PASSED, game, lines);
        assertEquals("关卡通关", GuiOutcomeHelper.buildTitle(GuiOutcomeHelper.OutcomeType.LEVEL_PASSED));
        assertEquals("继续", GuiOutcomeHelper.buildActionLabel(GuiOutcomeHelper.OutcomeType.LEVEL_PASSED));
        assertTrue(message.contains("恭喜通关第 2 关"));
        assertFalse(message.isEmpty());
    }
}

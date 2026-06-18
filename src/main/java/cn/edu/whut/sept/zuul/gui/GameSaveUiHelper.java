package cn.edu.whut.sept.zuul.gui;

import java.awt.Component;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GamePersistenceService;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GameSaveRecord;
import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * GUI 存档与读档交互辅助类（F8）；Enhanced 客户端使用玻璃对话框。
 */
public final class GameSaveUiHelper {

    private GameSaveUiHelper() {
    }

    /**
     * 玻璃对话框保存当前进度到 H2。
     *
     * @param dialogs 模态层
     * @param game 游戏实例
     * @param onComplete 完成回调（参数为是否保存成功）
     */
    public static void saveGame(GlassModalLayer dialogs, Game game, Consumer<Boolean> onComplete) {
        if (dialogs == null || game == null) {
            if (onComplete != null) {
                onComplete.accept(false);
            }
            return;
        }
        dialogs.showTextInput(
            "保存进度",
            "输入玩家昵称（用于存档）：",
            game.getPlayer().getName(),
            "保存",
            "取消",
            playerName -> performSave(dialogs, game, playerName, onComplete),
            () -> {
                if (onComplete != null) {
                    onComplete.accept(false);
                }
            }
        );
    }

    /**
     * 玻璃对话框从 H2 读档。
     *
     * @param dialogs 模态层
     * @param game 游戏实例
     * @param onLoaded 读档成功后的界面刷新回调
     */
    public static void loadGame(GlassModalLayer dialogs, Game game, Runnable onLoaded) {
        if (dialogs == null || game == null) {
            return;
        }
        List<GameSaveRecord> saves;
        try {
            saves = game.getPersistenceService().listSaves();
        } catch (PersistenceException exception) {
            dialogs.showMessage("加载失败", exception.getMessage(), "确定", null);
            return;
        }

        if (saves.isEmpty()) {
            dialogs.showMessage("加载进度", "暂无存档，请先保存进度。", "确定", null);
            return;
        }

        String[] options = saves.stream()
            .map(GameSaveRecord::getSummaryText)
            .toArray(String[]::new);

        dialogs.showOptionList(
            "加载进度",
            "选择要加载的存档：",
            options,
            choice -> performLoad(dialogs, game, saves, choice, onLoaded),
            null
        );
    }

    /**
     * 经典 Swing 窗口保存（GameWindow 仍用 JOptionPane）。
     *
     * @param parent 父组件
     * @param game 游戏实例
     * @return 保存成功返回 true
     */
    public static boolean saveGame(Component parent, Game game) {
        String playerName = JOptionPane.showInputDialog(
            parent,
            "输入玩家昵称（用于存档）：",
            game.getPlayer().getName()
        );
        if (playerName == null) {
            return false;
        }
        if (playerName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "昵称不能为空。", "保存失败", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return executeSave(parent, game, playerName.trim());
    }

    /**
     * 经典 Swing 窗口读档。
     *
     * @param parent 父组件
     * @param game 游戏实例
     * @param onLoaded 读档成功后的界面刷新回调
     * @return 读档成功返回 true
     */
    public static boolean loadGame(Component parent, Game game, Runnable onLoaded) {
        List<GameSaveRecord> saves;
        try {
            saves = game.getPersistenceService().listSaves();
        } catch (PersistenceException exception) {
            JOptionPane.showMessageDialog(
                parent,
                exception.getMessage(),
                "加载失败",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "暂无存档，请先保存进度。", "加载进度", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        String[] options = saves.stream()
            .map(GameSaveRecord::getSummaryText)
            .toArray(String[]::new);

        int choice = JOptionPane.showOptionDialog(
            parent,
            "选择要加载的存档：",
            "加载进度",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        if (choice < 0) {
            return false;
        }

        GameSaveRecord selected = saves.get(choice);
        try {
            boolean loaded = game.getPersistenceService().loadProgress(game, selected.getId());
            if (!loaded) {
                JOptionPane.showMessageDialog(parent, "存档不存在或已被删除。", "加载失败", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (onLoaded != null) {
                onLoaded.run();
            }
            JOptionPane.showMessageDialog(
                parent,
                "读档成功！\n" + game.getLevelTimer().getDisplayText(),
                "加载进度",
                JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (PersistenceException exception) {
            JOptionPane.showMessageDialog(
                parent,
                exception.getMessage(),
                "加载失败",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private static void performSave(
        GlassModalLayer dialogs,
        Game game,
        String playerName,
        Consumer<Boolean> onComplete) {
        if (playerName == null || playerName.trim().isEmpty()) {
            dialogs.showMessage("保存失败", "昵称不能为空。", "确定", () -> {
                if (onComplete != null) {
                    onComplete.accept(false);
                }
            });
            return;
        }
        boolean success = executeSave(dialogs, game, playerName.trim());
        if (onComplete != null) {
            onComplete.accept(success);
        }
    }

    private static boolean executeSave(Component feedbackParent, Game game, String playerName) {
        game.getPlayer().setName(playerName);
        try {
            GamePersistenceService service = game.getPersistenceService();
            long saveId = service.saveProgress(game);
            String message = "存档成功！\n存档编号: #" + saveId + "\n"
                + game.getLevelTimer().getDisplayText();
            if (feedbackParent instanceof GlassModalLayer) {
                ((GlassModalLayer) feedbackParent).showMessage("保存进度", message, "确定", null);
            } else {
                JOptionPane.showMessageDialog(
                    feedbackParent,
                    message,
                    "保存进度",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            return true;
        } catch (PersistenceException exception) {
            if (feedbackParent instanceof GlassModalLayer) {
                ((GlassModalLayer) feedbackParent).showMessage("保存失败", exception.getMessage(), "确定", null);
            } else {
                JOptionPane.showMessageDialog(
                    feedbackParent,
                    exception.getMessage(),
                    "保存失败",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        }
    }

    private static void performLoad(
        GlassModalLayer dialogs,
        Game game,
        List<GameSaveRecord> saves,
        int choice,
        Runnable onLoaded) {
        if (choice < 0 || choice >= saves.size()) {
            return;
        }
        GameSaveRecord selected = saves.get(choice);
        try {
            boolean loaded = game.getPersistenceService().loadProgress(game, selected.getId());
            if (!loaded) {
                dialogs.showMessage("加载失败", "存档不存在或已被删除。", "确定", null);
                return;
            }
            if (onLoaded != null) {
                onLoaded.run();
            }
            dialogs.showMessage(
                "加载进度",
                "读档成功！\n" + game.getLevelTimer().getDisplayText()
                    + "\n背包已清空，你从本关起点出发。",
                "继续",
                null
            );
        } catch (PersistenceException exception) {
            dialogs.showMessage("加载失败", exception.getMessage(), "确定", null);
        }
    }
}

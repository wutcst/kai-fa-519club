package cn.edu.whut.sept.zuul.gui;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GamePersistenceService;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GameSaveRecord;
import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * GUI 存档与读档交互辅助类（F8）。
 */
public final class GameSaveUiHelper {

    private GameSaveUiHelper() {
    }

    /**
     * 弹出对话框保存当前进度到 H2。
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

        game.getPlayer().setName(playerName.trim());
        try {
            GamePersistenceService service = game.getPersistenceService();
            long saveId = service.saveProgress(game);
            JOptionPane.showMessageDialog(
                parent,
                "存档成功！\n存档编号: #" + saveId + "\n"
                    + game.getLevelTimer().getDisplayText(),
                "保存进度",
                JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (PersistenceException exception) {
            JOptionPane.showMessageDialog(
                parent,
                exception.getMessage(),
                "保存失败",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * 弹出对话框从 H2 读档。
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
}

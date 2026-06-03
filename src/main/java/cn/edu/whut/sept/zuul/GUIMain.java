/**
 * 该包包含World-of-Zuul文本冒险游戏的核心实现类，
 * 涵盖游戏控制、命令解析、房间管理等功能模块，
 * 实现了玩家与文本界面的交互逻辑。
 * 【新增】扩展游戏入口，支持选择命令行或图形界面模式。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.gui.GameWindow;
import cn.edu.whut.sept.zuul.gui.EnhancedGameWindow;
import javax.swing.*;

/**
 * 图形界面主入口类，提供游戏启动模式选择
 * 新增：支持启动命令行模式或图形界面模式
 *
 * @author liujing
 * @version 2.0
 */
public class GUIMain {

    /**
     * 主启动方法，提供模式选择界面
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 检查是否通过命令行参数指定模式
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("console")) {
                startConsoleMode();
            } else if (args[0].equalsIgnoreCase("gui")) {
                startGUIMode();
            } else if (args[0].equalsIgnoreCase("enhanced")) {
                startEnhancedMode();
            } else {
                showModeSelectionDialog();
            }
        } else {
            showModeSelectionDialog();
        }
    }

    /**
     * 显示模式选择对话框
     */
    private static void showModeSelectionDialog() {
        // 使用Swing对话框选择模式
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 使用默认外观
        }

        String[] options = {"命令行模式", "图形界面模式", "增强图形模式", "退出"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "选择游戏启动模式",
                "World of Zuul 启动器",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0:
                startConsoleMode();
                break;
            case 1:
                startGUIMode();
                break;
            case 2:
                startEnhancedMode();
                break;
            case 3:
            default:
                System.exit(0);
        }
    }

    /**
     * 启动命令行模式
     */
    private static void startConsoleMode() {
        System.out.println("启动命令行模式...");
        Game game = new Game();
        game.play();
    }

    /**
     * 启动图形界面模式
     */
    private static void startGUIMode() {
        System.out.println("启动图形界面模式...");

        SwingUtilities.invokeLater(() -> {
            try {
                Game game = new Game();
                GameWindow window = new GameWindow(game);
                window.start();
            } catch (Exception e) {
                System.err.println("启动图形界面失败: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "启动图形界面失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * 启动增强图形界面模式
     */
    private static void startEnhancedMode() {
        System.out.println("启动增强图形界面模式...");

        SwingUtilities.invokeLater(() -> {
            try {
                Game game = new Game();
                EnhancedGameWindow window = new EnhancedGameWindow(game);
                window.start();
            } catch (Exception e) {
                System.err.println("启动增强界面失败: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "启动增强界面失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * 命令行参数说明
     */
    private static void printUsage() {
        System.out.println("用法: java cn.edu.whut.sept.zuul.GUIMain [mode]");
        System.out.println("模式:");
        System.out.println("  console    - 命令行模式");
        System.out.println("  gui        - 基本图形界面模式");
        System.out.println("  enhanced   - 增强图形界面模式");
        System.out.println("  无参数     - 显示模式选择对话框");
    }
}
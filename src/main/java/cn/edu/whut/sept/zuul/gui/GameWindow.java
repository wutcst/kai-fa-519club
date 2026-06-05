/**
 * 该包包含World-of-Zuul文本冒险游戏的图形化界面实现类，
 * 涵盖窗口管理、界面布局、事件处理等功能模块，
 * 实现了玩家与图形界面的交互逻辑。
 * 【新增】扩展游戏图形化界面，保持原有命令行功能不变。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 2.0
 */
package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;

/**
 * 游戏主窗口类，提供图形化游戏界面
 * 新增：实现Swing图形界面，支持鼠标和键盘操作
 *
 * @author liujing
 * @version 2.0
 */
public class GameWindow extends JFrame {
    private Game game;
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton submitButton;
    private JPanel roomPanel;
    private JPanel inventoryPanel;
    private JLabel roomLabel;
    private JTextArea roomInfoArea;
    private JTextArea inventoryArea;

    /**
     * 初始化游戏窗口
     *
     * @param game 游戏实例
     */
    public GameWindow(Game game) {
        this.game = game;
        initializeWindow();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        updateGameDisplay();
    }

    /**
     * 初始化窗口属性
     */
    private void initializeWindow() {
        setTitle("World of Zuul - 图形化界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());
    }

    /**
     * 创建界面组件
     */
    private void createComponents() {
        // 输出区域（显示游戏反馈）
        outputArea = new JTextArea(15, 40);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("宋体", Font.PLAIN, 14));

        // 输入区域
        inputField = new JTextField(30);
        inputField.setFont(new Font("宋体", Font.PLAIN, 14));

        // 提交按钮
        submitButton = new JButton("执行");
        submitButton.setFont(new Font("宋体", Font.BOLD, 14));

        // 房间信息面板
        roomPanel = new JPanel();
        roomPanel.setLayout(new BorderLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder("当前房间"));

        roomLabel = new JLabel();
        roomLabel.setFont(new Font("黑体", Font.BOLD, 16));

        roomInfoArea = new JTextArea(8, 20);
        roomInfoArea.setEditable(false);
        roomInfoArea.setLineWrap(true);
        roomInfoArea.setWrapStyleWord(true);

        // 物品面板
        inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("物品栏"));

        inventoryArea = new JTextArea(8, 20);
        inventoryArea.setEditable(false);
        inventoryArea.setLineWrap(true);
        inventoryArea.setWrapStyleWord(true);
    }

    /**
     * 布局界面组件
     */
    private void layoutComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建顶部面板（房间信息）
        roomPanel.add(roomLabel, BorderLayout.NORTH);
        roomPanel.add(new JScrollPane(roomInfoArea), BorderLayout.CENTER);

        // 创建右侧面板（物品信息）
        inventoryPanel.add(new JScrollPane(inventoryArea), BorderLayout.CENTER);

        // 创建游戏输出面板
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("游戏输出"));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("命令输入"));

        JPanel inputControls = new JPanel(new FlowLayout());
        inputControls.add(new JLabel("命令:"));
        inputControls.add(inputField);
        inputControls.add(submitButton);

        inputPanel.add(inputControls, BorderLayout.NORTH);

        // 创建快速命令按钮面板
        JPanel quickCommandsPanel = createQuickCommandsPanel();
        inputPanel.add(quickCommandsPanel, BorderLayout.CENTER);

        // 创建左侧面板（信息面板）
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(roomPanel, BorderLayout.NORTH);
        leftPanel.add(inventoryPanel, BorderLayout.CENTER);

        // 组装主界面
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(outputPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // 添加菜单栏
        setJMenuBar(createMenuBar());

        add(mainPanel);
    }

    /**
     * 创建快速命令按钮面板
     *
     * @return 快速命令面板
     */
    private JPanel createQuickCommandsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("快速命令"));

        String[] commands = {
                "go north", "go south", "go east", "go west",
                "look", "items", "back", "help",
                "take", "drop", "eat cookie", "quit"  // 修改这里：添加 "eat cookie"，移除 "clear"
        };

        String[] buttonTexts = {
                "向北", "向南", "向东", "向西",
                "查看", "物品", "返回", "帮助",
                "拾取", "丢弃", "吃饼干", "退出"  // 修改这里："清屏" → "吃饼干"
        };

        for (int i = 0; i < commands.length; i++) {
            JButton button = new JButton(buttonTexts[i]);
            button.setFont(new Font("宋体", Font.PLAIN, 12));
            final String command = commands[i];

            // 特殊处理拾取、丢弃和帮助按钮
            if (command.equals("take") || command.equals("drop")) {
                button.addActionListener(e -> {
                    String itemName = JOptionPane.showInputDialog(
                            this,
                            "请输入物品名称:",
                            command.equals("take") ? "拾取物品" : "丢弃物品",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (itemName != null && !itemName.trim().isEmpty()) {
                        processCommand(command + " " + itemName.trim());
                    }
                });
            } else if (command.equals("help")) {
                // 帮助按钮直接调用showHelp方法，在UI输出区域显示
                button.addActionListener(e -> showHelp());
            } else {
                button.addActionListener(e -> processCommand(command));
            }
            panel.add(button);
        }

        return panel;
    }

    /**
     * 创建菜单栏
     *
     * @return 菜单栏
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        gameMenu.setFont(new Font("宋体", Font.PLAIN, 12));

        JMenuItem newGameItem = new JMenuItem("新游戏");
        JMenuItem saveGameItem = new JMenuItem("保存游戏");
        JMenuItem loadGameItem = new JMenuItem("加载游戏");
        JMenuItem exitItem = new JMenuItem("退出");

        newGameItem.addActionListener(e -> restartGame());
        saveGameItem.addActionListener(e -> saveGame());
        loadGameItem.addActionListener(e -> loadGame());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(saveGameItem);
        gameMenu.add(loadGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setFont(new Font("宋体", Font.PLAIN, 12));

        JMenuItem helpItem = new JMenuItem("游戏帮助");
        JMenuItem aboutItem = new JMenuItem("关于游戏");

        helpItem.addActionListener(e -> showHelp());
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 提交按钮点击事件
        submitButton.addActionListener(e -> {
            String command = inputField.getText().trim();
            if (!command.isEmpty()) {
                processCommand(command);
                inputField.setText("");
            }
        });

        // 输入框回车事件
        inputField.addActionListener(e -> {
            String command = inputField.getText().trim();
            if (!command.isEmpty()) {
                processCommand(command);
                inputField.setText("");
            }
        });

        // 窗口关闭事件
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    /**
     * 处理游戏命令
     *
     * @param command 用户输入的命令
     */
    private void processCommand(String command) {
        if (command.trim().isEmpty()) {
            return;
        }

        // 显示输入的命令
        appendOutput("> " + command);

        // 处理特殊命令
        if (command.equalsIgnoreCase("clear")) {
            outputArea.setText("");
            return;
        }

        // 解析命令
        String[] parts = command.split(" ", 2);
        String commandWord = parts[0];
        String parameter = parts.length > 1 ? parts[1] : null;

        // 调用游戏处理命令
        boolean exitGame = game.getCommandManager().executeCommand(
                commandWord,
                parameter,
                game
        );

        // 在 processCommand 方法中添加
        if (commandWord.equalsIgnoreCase("take")) {
            // 特殊处理拾取命令，检查重量限制
            Player player = game.getPlayer();
            Room currentRoom = game.getCurrentRoom();
            List<Item> roomItems = currentRoom.getItems();

            // 查找物品
            Item targetItem = null;
            for (Item item : roomItems) {
                if (item.getDescription().equalsIgnoreCase(parameter)) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem != null) {
                // 检查负重
                if (player.getCurrentWeight() + targetItem.getWeight() > player.getMaxWeight()) {
                    appendOutput("你无法拾取 '" + parameter + "', 它太重了！");
                    appendOutput("当前负重: " + player.getCurrentWeight() + "g / " +
                            player.getMaxWeight() + "g");
                    appendOutput("需要: " + targetItem.getWeight() + "g, 但只剩: " +
                            player.getRemainingCapacity() + "g");
                    updateGameDisplay(); // 仍然更新显示
                    return;
                }
            }
        }

        // 更新界面显示
        updateGameDisplay();

        // 如果退出游戏
        if (exitGame) {
            appendOutput("\n游戏结束。感谢游玩！");
            submitButton.setEnabled(false);
            inputField.setEnabled(false);
        }
    }

    /**
     * 更新游戏显示信息
     */
    private void updateGameDisplay() {
        // 更新房间信息
        Room currentRoom = game.getCurrentRoom();
        roomLabel.setText("当前位置: " + currentRoom.getShortDescription());
        roomInfoArea.setText(currentRoom.getLongDescription());

        // 更新物品栏信息
        Player player = game.getPlayer();
        inventoryArea.setText(player.getInventoryDetails());

        // 设置焦点到输入框
        inputField.requestFocus();
    }

    /**
     * 向输出区域追加文本
     *
     * @param text 要显示的文本
     */
    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    /**
     * 重新开始游戏
     */
    private void restartGame() {
        int response = JOptionPane.showConfirmDialog(
                this,
                "确定要开始新游戏吗？当前进度将丢失。",
                "新游戏",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            this.game = new Game();
            outputArea.setText("");
            appendOutput("新游戏已开始！");
            updateGameDisplay();
            submitButton.setEnabled(true);
            inputField.setEnabled(true);
        }
    }

    /**
     * 保存游戏
     */
    private void saveGame() {
        JOptionPane.showMessageDialog(
                this,
                "保存游戏功能正在开发中...",
                "保存游戏",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 加载游戏
     */
    private void loadGame() {
        JOptionPane.showMessageDialog(
                this,
                "加载游戏功能正在开发中...",
                "加载游戏",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 显示游戏帮助（简洁版）
     */
    private void showHelp() {
        // 从命令管理器获取所有可用命令
        String[] commandWords = game.getCommandManager().getCommandWords();

        // 清空之前的输出
        outputArea.setText("");

        // 添加标准的帮助信息
        appendOutput("You are lost. You are alone. You wander");
        appendOutput("around at the university.");
        appendOutput("");
        appendOutput("Your command words are:");

        // 显示命令列表，一行显示所有命令
        StringBuilder commandsLine = new StringBuilder();
        for (String command : commandWords) {
            commandsLine.append(command).append(" ");
        }

        // 显示所有命令在一行
        appendOutput(commandsLine.toString().trim());
    }

    /**
     * 显示关于信息
     */
    private void showAbout() {
        String aboutText =
                "World of Zuul 图形化版本\n" +
                        "版本: 2.0\n" +
                        "开发者: liujing\n" +
                        "基于原版游戏: Michael Kölling and David J. Barnes\n\n" +
                        "本游戏是一个文本冒险游戏的图形化实现，\n" +
                        "保持了原版游戏的所有功能和特性。\n\n" +
                        "© 2025 武汉理工大学";

        JOptionPane.showMessageDialog(
                this,
                aboutText,
                "关于游戏",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 启动图形化界面
     */
    public void start() {
        setVisible(true);
        appendOutput("欢迎来到 World of Zuul 图形化版本！");
        appendOutput("输入 'help' 获取游戏帮助。");
    }
}
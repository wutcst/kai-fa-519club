/**
 * 该包包含World-of-Zuul文本冒险游戏的图形化界面实现类，
 * 涵盖窗口管理、界面布局、事件处理等功能模块，
 * 实现了玩家与图形界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 2.0
 */
package cn.edu.whut.sept.zuul.gui;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.Item;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * 增强版游戏窗口类，提供更丰富的图形化功能
 * 新增：支持图像显示、地图视图、物品拖放等高级功能
 *
 * @author liujing
 * @version 2.0
 */
public class EnhancedGameWindow extends JFrame {
    private Game game;
    private ImageLoader imageLoader;
    private JTextArea outputArea;
    private JTextField inputField;
    private JPanel mapPanel;
    private JPanel inventoryPanel;
    private JPanel roomItemsPanel;
    private JLabel currentRoomImage;
    private JLabel playerStatusLabel;
    private JButton northButton, southButton, eastButton, westButton;

    /**
     * 初始化增强版游戏窗口
     *
     * @param game 游戏实例
     */
    public EnhancedGameWindow(Game game) {
        this.game = game;
        this.imageLoader = ImageLoader.getInstance();
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
        setTitle("World of Zuul - 增强图形界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /**
     * 创建界面组件
     */
    private void createComponents() {
        // 游戏输出区域
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("宋体", Font.PLAIN, 13));

        // 命令输入框
        inputField = new JTextField(25);
        inputField.setFont(new Font("宋体", Font.PLAIN, 14));

        // 地图面板
        mapPanel = new JPanel(new GridBagLayout());
        mapPanel.setBorder(BorderFactory.createTitledBorder("地图视图"));
        mapPanel.setBackground(new Color(240, 240, 240));

        // 当前房间图像
        currentRoomImage = new JLabel();
        currentRoomImage.setHorizontalAlignment(SwingConstants.CENTER);

        // 玩家状态标签
        playerStatusLabel = new JLabel();
        playerStatusLabel.setFont(new Font("黑体", Font.BOLD, 14));

        // 物品栏面板
        inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("我的物品"));

        // 房间物品面板
        roomItemsPanel = new JPanel();
        roomItemsPanel.setLayout(new BoxLayout(roomItemsPanel, BoxLayout.Y_AXIS));
        roomItemsPanel.setBorder(BorderFactory.createTitledBorder("房间物品"));
    }

    /**
     * 布局界面组件
     */
    private void layoutComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建左侧面板（地图和房间）
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(400, 600));

        // 地图面板
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(currentRoomImage, BorderLayout.CENTER);
        mapContainer.add(createDirectionButtons(), BorderLayout.SOUTH);

        mapPanel.add(mapContainer);
        leftPanel.add(mapPanel, BorderLayout.CENTER);

        // 创建右侧面板（信息和控制）
        JPanel rightPanel = new JPanel(new BorderLayout());

        // 游戏输出面板
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("游戏日志"));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // 输入控制面板
        JPanel inputPanel = createInputPanel();

        // 物品面板容器
        JPanel itemsContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        itemsContainer.add(new JScrollPane(roomItemsPanel));
        itemsContainer.add(new JScrollPane(inventoryPanel));

        rightPanel.add(outputPanel, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        rightPanel.add(itemsContainer, BorderLayout.NORTH);

        // 顶部状态栏
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(playerStatusLabel, BorderLayout.WEST);

        // 组装主界面
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // 添加菜单栏
        setJMenuBar(createEnhancedMenuBar());

        add(mainPanel);
    }

    /**
     * 创建方向按钮面板
     *
     * @return 方向按钮面板
     */
    private JPanel createDirectionButtons() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 5, 5));

        // 空单元格
        panel.add(new JLabel());

        // 北方向按钮
        JButton northButton = createDirectionButton("north", "北");
        panel.add(northButton);

        panel.add(new JLabel());

        // 西方向按钮
        JButton westButton = createDirectionButton("west", "西");
        panel.add(westButton);

        // 中间（玩家位置）
        JLabel playerLabel = new JLabel(imageLoader.getScaledImage("player", 40, 40));
        playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(playerLabel);

        // 东方向按钮
        JButton eastButton = createDirectionButton("east", "东");
        panel.add(eastButton);

        panel.add(new JLabel());

        // 南方向按钮
        JButton southButton = createDirectionButton("south", "南");
        panel.add(southButton);

        panel.add(new JLabel());

        return panel;
    }

    /**
     * 创建方向按钮
     *
     * @param direction 方向
     * @param text 按钮文本
     * @return 方向按钮
     */
    private JButton createDirectionButton(String direction, String text) {
        JButton button = new JButton(text, imageLoader.getDirectionIcon(direction));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(new Font("宋体", Font.BOLD, 11));

        button.addActionListener(e -> processCommand("go " + direction));

        return button;
    }

    /**
     * 创建输入面板
     *
     * @return 输入面板
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("命令控制"));

        JPanel inputControls = new JPanel(new FlowLayout());
        inputControls.add(new JLabel("输入命令:"));
        inputControls.add(inputField);

        JButton submitButton = new JButton("执行");
        submitButton.addActionListener(e -> executeInputCommand());
        inputControls.add(submitButton);

        JButton clearButton = new JButton("清屏");
        clearButton.addActionListener(e -> outputArea.setText(""));
        inputControls.add(clearButton);

        panel.add(inputControls, BorderLayout.NORTH);
        panel.add(createActionButtons(), BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建动作按钮面板
     *
     * @return 动作按钮面板
     */
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));

        String[][] actions = {
                {"look", "查看房间", "查看当前房间详情"},
                {"items", "所有物品", "查看房间和物品栏物品"},
                {"back", "返回", "返回上一个房间"},
                {"help", "帮助", "显示帮助信息"},
                {"take", "拾取", "拾取房间内物品"},
                {"drop", "丢弃", "丢弃携带物品"},
                {"eat cookie", "吃饼干", "吃掉魔法饼干"},
                {"quit", "退出", "退出游戏"}
        };

        for (String[] action : actions) {
            JButton button = new JButton(action[1]);
            button.setToolTipText(action[2]);
            button.setFont(new Font("宋体", Font.PLAIN, 11));

            final String command = action[0];

            // 特殊处理拾取、丢弃和帮助按钮
            if (command.equals("take") || command.equals("drop")) {
                button.addActionListener(e -> {
                    String itemName = JOptionPane.showInputDialog(
                            EnhancedGameWindow.this,
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
     * 创建增强菜单栏
     *
     * @return 菜单栏
     */
    private JMenuBar createEnhancedMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏");
        JMenuItem saveItem = new JMenuItem("保存进度");
        JMenuItem loadItem = new JMenuItem("加载进度");
        JMenuItem statsItem = new JMenuItem("游戏统计");
        JMenuItem exitItem = new JMenuItem("退出");

        newGameItem.addActionListener(e -> restartGame());
        saveItem.addActionListener(e -> saveGame());
        loadItem.addActionListener(e -> loadGame());
        statsItem.addActionListener(e -> showStatistics());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(statsItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        JMenuItem zoomInItem = new JMenuItem("放大");
        JMenuItem zoomOutItem = new JMenuItem("缩小");
        JMenuItem resetViewItem = new JMenuItem("重置视图");

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.addSeparator();
        viewMenu.add(resetViewItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem quickHelpItem = new JMenuItem("快速指南");
        JMenuItem commandsItem = new JMenuItem("命令大全");
        JMenuItem aboutItem = new JMenuItem("关于");
        JMenuItem helpItem = new JMenuItem("游戏帮助");

        helpItem.addActionListener(e -> showHelp());
        quickHelpItem.addActionListener(e -> showQuickHelp());
        commandsItem.addActionListener(e -> showAllCommands());
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(helpItem);
        helpMenu.add(quickHelpItem);
        helpMenu.add(commandsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 输入框回车事件
        inputField.addActionListener(e -> executeInputCommand());

        // 双击物品事件
        inventoryPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // 处理物品双击
                    Component component = inventoryPanel.getComponentAt(e.getPoint());
                    if (component instanceof JLabel) {
                        JLabel label = (JLabel) component;
                        String itemName = label.getText();
                        processCommand("drop " + itemName.split(" ")[0]);
                    }
                }
            }
        });

        roomItemsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Component component = roomItemsPanel.getComponentAt(e.getPoint());
                    if (component instanceof JLabel) {
                        JLabel label = (JLabel) component;
                        String itemName = label.getText();
                        processCommand("take " + itemName.split(" ")[0]);
                    }
                }
            }
        });
    }

    /**
     * 执行输入命令
     */
    private void executeInputCommand() {
        String command = inputField.getText().trim();
        if (!command.isEmpty()) {
            processCommand(command);
            inputField.setText("");
        }
    }

    /**
     * 处理游戏命令
     */
    private void processCommand(String command) {
        appendOutput("> " + command);

        if (command.equalsIgnoreCase("clear")) {
            outputArea.setText("");
            return;
        }

        // 解析命令
        String[] parts = command.split(" ", 2);
        String commandWord = parts[0];
        String parameter = parts.length > 1 ? parts[1] : null;

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

        updateGameDisplay();

        if (exitGame) {
            appendOutput("\n游戏结束！");
            inputField.setEnabled(false);
        }
    }

    /**
     * 更新游戏显示
     */
    private void updateGameDisplay() {
        // 更新房间图像
        Room currentRoom = game.getCurrentRoom();
        ImageIcon roomIcon = imageLoader.getRoomIcon(currentRoom.getShortDescription());
        currentRoomImage.setIcon(imageLoader.getScaledImage(
                getRoomImageKey(currentRoom), 200, 200));

        // 在房间图片下方添加出口信息标签
        updateExitButtons(currentRoom);

        // 更新玩家状态
        Player player = game.getPlayer();
        playerStatusLabel.setText(String.format(
                "玩家: %s | 负重: %dg/%dg | 物品: %d件 | 位置: %s",
                player.getName(),
                player.getCurrentWeight(),
                player.getMaxWeight(),
                player.getInventory().size(),
                currentRoom.getShortDescription()
        ));

        // 更新房间物品
        updateRoomItems();

        // 更新物品栏
        updateInventory();

        // 焦点设置
        inputField.requestFocus();

        // 在输出区域显示方向提示
        appendOutput("当前位置: " + currentRoom.getShortDescription());
        appendOutput(getRoomExitsInfo(currentRoom));
    }

    /**
     * 更新方向按钮状态
     */
    private void updateExitButtons(Room room) {
        // 如果按钮已经创建，更新它们的状态
        if (northButton != null) {
            northButton.setEnabled(room.getExit("north") != null);
            northButton.setToolTipText(room.getExit("north") != null ?
                    "向北移动" : "此路不通");
        }
        if (southButton != null) {
            southButton.setEnabled(room.getExit("south") != null);
            southButton.setToolTipText(room.getExit("south") != null ?
                    "向南移动" : "此路不通");
        }
        if (eastButton != null) {
            eastButton.setEnabled(room.getExit("east") != null);
            eastButton.setToolTipText(room.getExit("east") != null ?
                    "向东移动" : "此路不通");
        }
        if (westButton != null) {
            westButton.setEnabled(room.getExit("west") != null);
            westButton.setToolTipText(room.getExit("west") != null ?
                    "向西移动" : "此路不通");
        }
    }

    /**
     * 获取房间图像键
     */
    private String getRoomImageKey(Room room) {
        String desc = room.getShortDescription();
        if (desc.contains("outside")) return "room_outside";
        if (desc.contains("theater")) return "room_theater";
        if (desc.contains("pub")) return "room_pub";
        if (desc.contains("lab")) return "room_lab";
        if (desc.contains("office")) return "room_office";
        if (desc.contains("teleport")) return "room_teleport";
        return "room_outside";
    }

    /**
     * 更新房间物品显示
     */
    private void updateRoomItems() {
        roomItemsPanel.removeAll();

        Room currentRoom = game.getCurrentRoom();
        List<Item> items = currentRoom.getItems();

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("房间内没有物品");
            emptyLabel.setFont(new Font("宋体", Font.ITALIC, 12));
            roomItemsPanel.add(emptyLabel);
        } else {
            for (Item item : items) {
                JLabel itemLabel = createItemLabel(item, false);
                roomItemsPanel.add(itemLabel);
            }
        }

        roomItemsPanel.revalidate();
        roomItemsPanel.repaint();
    }

    /**
     * 更新物品栏显示
     */
    private void updateInventory() {
        inventoryPanel.removeAll();

        Player player = game.getPlayer();
        List<Item> items = player.getInventory();

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("物品栏为空");
            emptyLabel.setFont(new Font("宋体", Font.ITALIC, 12));
            inventoryPanel.add(emptyLabel);
        } else {
            for (Item item : items) {
                JLabel itemLabel = createItemLabel(item, true);
                inventoryPanel.add(itemLabel);
            }
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    /**
     * 创建物品标签
     */
    private JLabel createItemLabel(Item item, boolean inInventory) {
        JLabel label = new JLabel(item.getDetails());
        label.setIcon(imageLoader.getItemIcon(item.getDescription()));
        label.setFont(new Font("宋体", inInventory ? Font.BOLD : Font.PLAIN, 12));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (inInventory) {
            label.setToolTipText("双击丢弃: " + item.getDescription());
        } else {
            label.setToolTipText("双击拾取: " + item.getDescription());
        }

        return label;
    }

    /**
     * 向输出区域追加文本
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
                this, "确定要开始新游戏吗？", "新游戏", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            game = new Game();
            outputArea.setText("");
            appendOutput("新游戏开始！");
            updateGameDisplay();
            inputField.setEnabled(true);
        }
    }

    /**
     * 保存游戏
     */
    private void saveGame() {
        JOptionPane.showMessageDialog(this, "保存功能开发中", "保存", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 加载游戏
     */
    private void loadGame() {
        JOptionPane.showMessageDialog(this, "加载功能开发中", "加载", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示游戏统计
     */
    private void showStatistics() {
        Player player = game.getPlayer();
        String stats = String.format(
                "=== 游戏统计 ===\n" +
                        "玩家: %s\n" +
                        "当前负重: %dg/%dg\n" +
                        "携带物品: %d件\n" +
                        "剩余容量: %dg\n" +
                        "当前位置: %s",
                player.getName(),
                player.getCurrentWeight(),
                player.getMaxWeight(),
                player.getInventory().size(),
                player.getRemainingCapacity(),
                game.getCurrentRoom().getShortDescription()
        );

        JOptionPane.showMessageDialog(this, stats, "游戏统计", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示快速帮助
     */
    private void showQuickHelp() {
        String help = "=== 快速指南 ===\n\n" +
                "1. 使用方向按钮或输入'go <方向>'移动\n" +
                "2. 双击物品可拾取/丢弃\n" +
                "3. 使用动作按钮执行常用命令\n" +
                "4. 查看房间物品和物品栏信息\n" +
                "5. 输入'help'查看详细帮助";

        JOptionPane.showMessageDialog(this, help, "快速指南", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示所有命令
     */
    private void showAllCommands() {
        String commands =
                "=== 命令大全 ===\n\n" +
                        "移动命令:\n" +
                        "  go north/south/east/west - 向指定方向移动\n" +
                        "  back - 返回上一个房间\n" +
                        "  look - 查看当前房间\n\n" +
                        "物品命令:\n" +
                        "  take <物品> - 拾取物品\n" +
                        "  take all - 拾取所有物品\n" +
                        "  drop <物品> - 丢弃物品\n" +
                        "  drop all - 丢弃所有物品\n" +
                        "  items - 查看所有物品\n\n" +
                        "特殊命令:\n" +
                        "  eat cookie - 吃魔法饼干增加负重\n" +
                        "  help - 显示帮助\n" +
                        "  quit - 退出游戏";

        JOptionPane.showMessageDialog(this, commands, "命令大全", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示关于信息
     */
    private void showAbout() {
        String about =
                "World of Zuul 增强图形界面\n" +
                        "版本 2.0\n\n" +
                        "基于Swing开发的图形化冒险游戏\n" +
                        "支持鼠标操作和键盘命令\n" +
                        "保持原版游戏所有功能\n\n" +
                        "© 2023 武汉理工大学";

        JOptionPane.showMessageDialog(this, about, "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 启动增强界面
     */
    public void start() {
        setVisible(true);
        appendOutput("欢迎来到 World of Zuul 增强图形界面！");
        appendOutput("使用方向按钮移动，双击物品进行交互。");
    }

    /**
     * 获取房间出口信息
     */
    private String getRoomExitsInfo(Room room) {
        StringBuilder exitsInfo = new StringBuilder();
        exitsInfo.append("可用出口:\n");

        // 检查各个方向
        String[] directions = {"north", "south", "east", "west"};
        String[] directionNames = {"北", "南", "东", "西"};
        boolean hasExit = false;

        for (int i = 0; i < directions.length; i++) {
            if (room.getExit(directions[i]) != null) {
                exitsInfo.append("  ").append(directionNames[i]).append(": 可通行\n");
                hasExit = true;
            }
        }

        if (!hasExit) {
            exitsInfo.append("  没有可用出口\n");
        }

        return exitsInfo.toString();
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
}
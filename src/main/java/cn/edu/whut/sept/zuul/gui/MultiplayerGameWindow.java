package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.multiplayer.GameCommandResult;
import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.MultiplayerSession;
import cn.edu.whut.sept.zuul.multiplayer.PlayerStateSnapshot;

/**
 * 联机图形界面：命令经服务端权威执行，本地仅做展示与输入。
 */
public class MultiplayerGameWindow extends JFrame {

    private final Game game;
    private final MultiplayerSession session;
    private final ImageLoader imageLoader = ImageLoader.getInstance();

    private final JTextArea outputArea = new JTextArea(12, 48);
    private final JTextField inputField = new JTextField(24);
    private final JLabel timerLabel = new JLabel("计时同步中...", SwingConstants.CENTER);
    private final JLabel roomLabel = new JLabel("房间", SwingConstants.CENTER);
    private final JLabel playersLabel = new JLabel("玩家", SwingConstants.CENTER);
    private final JLabel roomImageLabel = new JLabel();

    public MultiplayerGameWindow(Game game, MultiplayerSession session) {
        this.game = game;
        this.session = session;
        initializeWindow();
        session.addStateListener(this::onStatePolled);
        session.startPolling();
        applyState(session.getLastState());
        appendOutput("已加入联机房间 " + session.getRoomId());
        appendOutput("你的玩家 ID: " + session.getPlayerId());
        appendOutput("输入 help 查看命令，输入 quit 退出客户端。");
    }

    private void initializeWindow() {
        setTitle("熄灯前归寝 - 联机模式");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setFont(new Font("宋体", Font.PLAIN, 13));

        JPanel top = new JPanel(new GridLayout(1, 3, 8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        timerLabel.setFont(new Font("黑体", Font.BOLD, 14));
        roomLabel.setFont(new Font("宋体", Font.PLAIN, 13));
        playersLabel.setFont(new Font("宋体", Font.PLAIN, 12));
        top.add(timerLabel);
        top.add(roomLabel);
        top.add(playersLabel);

        JPanel center = new JPanel(new BorderLayout(8, 0));
        roomImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roomImageLabel.setPreferredSize(new Dimension(220, 220));
        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBorder(BorderFactory.createTitledBorder("当前位置"));
        mapPanel.add(roomImageLabel, BorderLayout.CENTER);
        mapPanel.add(createDirectionPanel(), BorderLayout.SOUTH);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("游戏日志"));
        logPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        center.add(mapPanel, BorderLayout.WEST);
        center.add(logPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        JButton sendButton = new JButton("发送");
        sendButton.addActionListener(event -> executeInput());
        inputField.addActionListener(event -> executeInput());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                leaveAndExit();
            }
        });
    }

    private JPanel createDirectionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 4));
        panel.add(directionButton("北", "go north"));
        panel.add(directionButton("南", "go south"));
        panel.add(directionButton("西", "go west"));
        panel.add(directionButton("东", "go east"));
        return panel;
    }

    private JButton directionButton(String text, String command) {
        JButton button = new JButton(text);
        button.addActionListener(event -> processCommand(command));
        return button;
    }

    private void executeInput() {
        String command = inputField.getText().trim();
        if (!command.isEmpty()) {
            processCommand(command);
            inputField.setText("");
        }
    }

    private void processCommand(String command) {
        if ("clear".equalsIgnoreCase(command)) {
            outputArea.setText("");
            return;
        }
        if ("quit".equalsIgnoreCase(command)) {
            leaveAndExit();
            return;
        }
        appendOutput("> " + command);
        String[] parts = command.split("\\s+", 2);
        String commandWord = parts[0];
        String secondWord = parts.length > 1 ? parts[1] : null;
        try {
            GameCommandResult result = session.executeCommand(commandWord, secondWord);
            for (String message : result.getMessages()) {
                appendOutput(message);
            }
            applyState(result.getState());
            if (result.isQuitRequested()) {
                appendOutput("本局已结束。");
                inputField.setEnabled(false);
            }
        } catch (IOException exception) {
            appendOutput("命令失败: " + exception.getMessage());
        }
    }

    private void onStatePolled(GameStateSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> applyState(snapshot));
    }

    private void applyState(GameStateSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        game.syncClientViewFromSnapshot(snapshot, session.getPlayerId());
        timerLabel.setText(snapshot.getTimerText() + " | 第 " + snapshot.getLevel() + " 关");
        roomLabel.setText(snapshot.getRoomDescription());

        StringBuilder playersText = new StringBuilder();
        for (PlayerStateSnapshot player : snapshot.getPlayers()) {
            if (playersText.length() > 0) {
                playersText.append("  |  ");
            }
            String marker = player.getPlayerId().equals(session.getPlayerId()) ? "（你）" : "";
            playersText.append(player.getDisplayName()).append(marker)
                .append(" @ ").append(player.getRoomName());
        }
        playersLabel.setText(playersText.toString());

        Room room = game.getCurrentRoom();
        if (room != null) {
            roomImageLabel.setIcon(imageLoader.getScaledImage(
                getRoomImageKey(room), 200, 200));
        }
    }

    private String getRoomImageKey(Room room) {
        String shortDescription = room.getShortDescription();
        if (shortDescription.contains("校门")) {
            return "gate";
        }
        if (shortDescription.contains("超市")) {
            return "supermarket";
        }
        if (shortDescription.contains("寝室")) {
            return "dormitory";
        }
        if (shortDescription.contains("图书馆")) {
            return "library";
        }
        if (shortDescription.contains("体育馆")) {
            return "gym";
        }
        return "default";
    }

    private void leaveAndExit() {
        int choice = JOptionPane.showConfirmDialog(
            this, "确定离开联机房间并退出？", "离开房间", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            session.leave();
        } catch (IOException exception) {
            session.shutdown();
        }
        dispose();
        System.exit(0);
    }

    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public void start() {
        setVisible(true);
        inputField.requestFocus();
    }
}

package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cn.edu.whut.sept.zuul.multiplayer.MultiplayerClient;
import cn.edu.whut.sept.zuul.multiplayer.MultiplayerConfig;
import cn.edu.whut.sept.zuul.multiplayer.MultiplayerSession;

/**
 * 联机大厅：创建/加入房间。
 */
public class MultiplayerLobbyDialog extends JDialog {

    private final JTextField serverUrlField = new JTextField(MultiplayerConfig.DEFAULT_SERVER_URL, 28);
    private final JTextField nicknameField = new JTextField(12);
    private final JTextField roomNameField = new JTextField("熄灯联机", 12);
    private final JTextField roomIdField = new JTextField(24);
    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private final JList<String> roomList = new JList<>(roomListModel);

    private MultiplayerSession session;

    public MultiplayerLobbyDialog() {
        super((java.awt.Frame) null, "联机大厅", true);
        setLayout(new BorderLayout(8, 8));
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new GridLayout(0, 1, 4, 4));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        top.add(row("服务端地址", serverUrlField));
        top.add(row("昵称", nicknameField));
        top.add(row("房间名（创建）", roomNameField));
        top.add(row("房间 ID（加入）", roomIdField));

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("房间列表"));
        center.add(new JScrollPane(roomList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton refreshButton = new JButton("刷新列表");
        JButton createButton = new JButton("创建房间");
        JButton joinButton = new JButton("加入房间");
        JButton cancelButton = new JButton("取消");
        buttons.add(refreshButton);
        buttons.add(createButton);
        buttons.add(joinButton);
        buttons.add(cancelButton);

        refreshButton.addActionListener(event -> refreshRooms());
        createButton.addActionListener(event -> createRoom());
        joinButton.addActionListener(event -> joinRoom());
        cancelButton.addActionListener(event -> dispose());
        roomList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selected = roomList.getSelectedValue();
                if (selected != null) {
                    int idx = selected.indexOf(" | ");
                    if (idx > 0) {
                        roomIdField.setText(selected.substring(0, idx));
                    }
                }
            }
        });

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public MultiplayerSession showAndConnect() {
        setVisible(true);
        return session;
    }

    private JPanel row(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private String requireNickname() {
        String name = nicknameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入昵称", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return name;
    }

    private MultiplayerClient buildClient() {
        return new MultiplayerClient(serverUrlField.getText().trim());
    }

    private void refreshRooms() {
        try {
            List<Map<String, Object>> rooms = buildClient().listRooms();
            roomListModel.clear();
            for (Map<String, Object> room : rooms) {
                String line = room.get("roomId") + " | "
                    + room.get("roomName") + " | "
                    + room.get("playerCount") + " 人 | 第 "
                    + room.get("level") + " 关";
                roomListModel.addElement(line);
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                "无法获取房间列表：" + exception.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createRoom() {
        String nickname = requireNickname();
        if (nickname == null) {
            return;
        }
        try {
            session = new MultiplayerSession(buildClient());
            session.createRoom(roomNameField.getText().trim(), nickname);
            dispose();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                "创建房间失败：" + exception.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void joinRoom() {
        String nickname = requireNickname();
        String roomId = roomIdField.getText().trim();
        if (nickname == null) {
            return;
        }
        if (roomId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入或选择房间 ID", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            session = new MultiplayerSession(buildClient());
            session.joinRoom(roomId, nickname);
            dispose();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                "加入房间失败：" + exception.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 在 EDT 上显示大厅并返回已连接的会话；取消时返回 null。
     */
    public static MultiplayerSession openLobby() {
        if (SwingUtilities.isEventDispatchThread()) {
            MultiplayerLobbyDialog dialog = new MultiplayerLobbyDialog();
            dialog.refreshRooms();
            return dialog.showAndConnect();
        }
        final MultiplayerSession[] holder = new MultiplayerSession[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                MultiplayerLobbyDialog dialog = new MultiplayerLobbyDialog();
                dialog.refreshRooms();
                holder[0] = dialog.showAndConnect();
            });
        } catch (Exception exception) {
            throw new IllegalStateException("无法打开联机大厅", exception);
        }
        return holder[0];
    }
}

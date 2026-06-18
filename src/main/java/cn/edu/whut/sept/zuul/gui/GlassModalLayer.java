package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * 沉浸式玻璃风格模态对话框层（替代 JOptionPane，F7 阶段 4-B）。
 */
public class GlassModalLayer extends JPanel {

    private static final int CARD_MAX_WIDTH = 460;
    private static final int CARD_MAX_HEIGHT = 420;

    private final JPanel backdrop;
    private final GlassPanel card;
    private final JLabel titleLabel;
    private final JLabel messageLabel;
    private final JPanel bodyPanel;
    private final JPanel buttonPanel;

    private int layerWidth;
    private int layerHeight;

    public GlassModalLayer() {
        setLayout(null);
        setOpaque(false);
        setVisible(false);

        backdrop = new JPanel();
        backdrop.setBackground(new Color(0, 0, 0, 165));
        backdrop.setOpaque(true);

        card = new GlassPanel(GuiTheme.HUD_BG_STRONG, GuiTheme.CORNER_RADIUS);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setForeground(GuiTheme.ACCENT);
        titleLabel.setFont(GuiTheme.FONT_BOLD);
        card.add(titleLabel, BorderLayout.NORTH);

        bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        messageLabel.setFont(GuiTheme.FONT_BODY);
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        bodyPanel.add(messageLabel);

        JScrollPane bodyScroll = new JScrollPane(bodyPanel);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(bodyScroll, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        card.add(buttonPanel, BorderLayout.SOUTH);

        add(backdrop);
        add(card);
    }

    public void layoutToSize(int width, int height) {
        layerWidth = Math.max(width, 0);
        layerHeight = Math.max(height, 0);
        setBounds(0, 0, layerWidth, layerHeight);
        backdrop.setBounds(0, 0, layerWidth, layerHeight);
        positionCard();
    }

    public boolean isDialogVisible() {
        return isVisible();
    }

    public void hideDialog() {
        setVisible(false);
        bodyPanel.removeAll();
        bodyPanel.add(messageLabel);
        buttonPanel.removeAll();
        revalidate();
        repaint();
    }

    public void showMessage(String title, String message, String okLabel, Runnable onOk) {
        prepareDialog(title, message);
        addButton(okLabel == null ? "确定" : okLabel, () -> finish(onOk));
        reveal();
    }

    public void showConfirm(
        String title,
        String message,
        String confirmLabel,
        String cancelLabel,
        Runnable onConfirm,
        Runnable onCancel) {
        prepareDialog(title, message);
        addButton(confirmLabel == null ? "确定" : confirmLabel, () -> finish(onConfirm));
        addButton(cancelLabel == null ? "取消" : cancelLabel, () -> finish(onCancel), false);
        reveal();
    }

    public void showTextInput(
        String title,
        String prompt,
        String defaultValue,
        String confirmLabel,
        String cancelLabel,
        Consumer<String> onSubmit,
        Runnable onCancel) {
        prepareDialog(title, prompt);

        JTextField inputField = new JTextField(defaultValue == null ? "" : defaultValue, 20);
        inputField.setFont(GuiTheme.FONT_BODY);
        inputField.setForeground(GuiTheme.TEXT_PRIMARY);
        inputField.setBackground(new Color(255, 255, 255, 28));
        inputField.setCaretColor(GuiTheme.TEXT_PRIMARY);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GuiTheme.SLOT_BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        inputField.setMaximumSize(new Dimension(CARD_MAX_WIDTH - 80, 34));
        inputField.setAlignmentX(CENTER_ALIGNMENT);
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(inputField);

        Runnable submit = () -> {
            if (onSubmit != null) {
                onSubmit.accept(inputField.getText());
            }
            hideDialog();
        };
        addButton(confirmLabel == null ? "确定" : confirmLabel, submit);
        addButton(cancelLabel == null ? "取消" : cancelLabel, () -> finish(onCancel), false);

        reveal();
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    public void showOptionList(
        String title,
        String prompt,
        String[] options,
        IntConsumer onSelect,
        Runnable onCancel) {
        prepareDialog(title, prompt);
        if (options != null) {
            for (int index = 0; index < options.length; index++) {
                final int selectedIndex = index;
                StyledGlassButton optionButton = new StyledGlassButton(options[index], GuiTheme.FONT_SMALL);
                optionButton.setPreferredSize(new Dimension(CARD_MAX_WIDTH - 96, 34));
                optionButton.addActionListener(event -> {
                    if (onSelect != null) {
                        onSelect.accept(selectedIndex);
                    }
                    hideDialog();
                });
                bodyPanel.add(Box.createVerticalStrut(6));
                bodyPanel.add(optionButton);
            }
        }
        addButton("取消", () -> finish(onCancel), false);
        reveal();
    }

    private void prepareDialog(String title, String message) {
        hideDialog();
        titleLabel.setText(title == null ? "" : title);
        messageLabel.setText(formatHtml(message));
        buttonPanel.removeAll();
    }

    private void addButton(String label, Runnable action) {
        addButton(label, action, true);
    }

    private void addButton(String label, Runnable action, boolean accent) {
        StyledGlassButton button = new StyledGlassButton(label, GuiTheme.FONT_SMALL);
        if (accent) {
            button.setAccentColor(GuiTheme.ACCENT);
        }
        button.addActionListener(event -> {
            if (action != null) {
                action.run();
            }
        });
        buttonPanel.add(button);
    }

    private void finish(Runnable action) {
        hideDialog();
        if (action != null) {
            action.run();
        }
    }

    private void reveal() {
        positionCard();
        setVisible(true);
        if (getParent() != null) {
            getParent().setComponentZOrder(this, 0);
        }
        revalidate();
        repaint();
    }

    private void positionCard() {
        int cardWidth = Math.min(CARD_MAX_WIDTH, Math.max(layerWidth - 64, 280));
        int cardHeight = Math.min(CARD_MAX_HEIGHT, Math.max(layerHeight / 2, 220));
        int x = (layerWidth - cardWidth) / 2;
        int y = (layerHeight - cardHeight) / 2;
        card.setBounds(x, y, cardWidth, cardHeight);
    }

    private String formatHtml(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        String escaped = message
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>");
        int width = Math.min(CARD_MAX_WIDTH - 72, 380);
        return "<html><body style='width:" + width + "px;text-align:center;line-height:1.5'>"
            + escaped
            + "</body></html>";
    }
}

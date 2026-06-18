package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.LevelConfig;

/**
 * 房间场景面板：全屏底图、中央方向导航、物品锚点、NPC、切换动画（F7 视觉升级）。
 */
public class RoomScenePanel extends JPanel {

    public interface RoomItemListener {
        void onTakeItem(Item item);

        void onInspectRoomItem(Item item);
    }

    public interface RoomNpcListener {
        void onTalk();

        void onFeed();
    }

    public interface DirectionListener {
        void onNavigate(String direction);

        void onBack();
    }

    private static final int ITEM_ICON_SIZE = 72;
    private static final int NPC_ICON_SIZE = 112;

    private final ImageLoader imageLoader;
    private final JLayeredPane layeredPane;
    private final JLabel roomImageLabel;
    private final JPanel itemLayer;
    private final JPanel npcPanel;
    private final JLabel npcImageLabel;
    private final StyledGlassButton talkButton;
    private final StyledGlassButton feedButton;
    private final JLabel trapBannerLabel;
    private final JPanel lockedOverlayPanel;
    private final JLabel lockedMessageLabel;
    private final JLabel lockedHintLabel;
    private final JPanel popupLayer;
    private final GlassPanel popupCard;
    private final JLabel popupMessageLabel;
    private final StyledGlassButton popupCloseButton;
    private final JPanel outcomeLayer;
    private final GlassPanel outcomeCard;
    private final JLabel outcomeTitleLabel;
    private final JLabel outcomeMessageLabel;
    private final StyledGlassButton outcomeActionButton;
    private Runnable outcomeAction;
    private final JPanel directionNavPanel;
    private final DirectionArrowButton northButton;
    private final DirectionArrowButton southButton;
    private final DirectionArrowButton eastButton;
    private final DirectionArrowButton westButton;
    private final StyledGlassButton backButton;
    private final ItemActionMenu roomItemMenu;
    private final SceneVignettePanel vignettePanel;
    private final JPanel slideTransitionPanel;
    private final DirectionalRoomTransition directionalTransition;

    private ImageIcon roomIcon;
    private boolean suppressVisualRoomUpdate;
    private RoomItemListener itemListener;
    private RoomNpcListener npcListener;
    private DirectionListener directionListener;

    private Room currentRoom;
    private List<Item> currentItems = new ArrayList<>();
    private boolean showNpc;
    private boolean showFeed;
    private String westTrapText;
    private int sceneWidth;
    private int sceneHeight;

    public RoomScenePanel(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(GuiTheme.WINDOW_BG);

        layeredPane = new JLayeredPane();

        roomImageLabel = new JLabel();
        roomImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roomImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        itemLayer = new JPanel(null);
        itemLayer.setOpaque(false);

        npcPanel = new JPanel();
        npcPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
        npcPanel.setOpaque(false);
        npcImageLabel = new JLabel();
        npcImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        talkButton = new StyledGlassButton("对话", GuiTheme.FONT_SMALL);
        feedButton = new StyledGlassButton("喂猫", GuiTheme.FONT_SMALL);
        feedButton.setAccentColor(GuiTheme.ACCENT);
        feedButton.setVisible(false);
        talkButton.addActionListener(event -> {
            if (npcListener != null) {
                npcListener.onTalk();
            }
        });
        feedButton.addActionListener(event -> {
            if (npcListener != null) {
                npcListener.onFeed();
            }
        });
        npcPanel.add(npcImageLabel);
        npcPanel.add(talkButton);
        npcPanel.add(feedButton);

        trapBannerLabel = new JLabel();
        trapBannerLabel.setOpaque(true);
        trapBannerLabel.setBackground(new Color(90, 20, 20, 200));
        trapBannerLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        trapBannerLabel.setFont(GuiTheme.FONT_BOLD);
        trapBannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        trapBannerLabel.setVisible(false);

        lockedOverlayPanel = new JPanel(new BorderLayout());
        lockedOverlayPanel.setBackground(new Color(0, 0, 0, 220));
        lockedOverlayPanel.setVisible(false);

        lockedMessageLabel = new JLabel("", SwingConstants.CENTER);
        lockedMessageLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        lockedMessageLabel.setFont(GuiTheme.FONT_BOLD);

        lockedHintLabel = new JLabel("点击任意处继续", SwingConstants.CENTER);
        lockedHintLabel.setForeground(GuiTheme.TEXT_MUTED);
        lockedHintLabel.setFont(GuiTheme.FONT_SMALL);
        lockedHintLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel lockedContent = new JPanel();
        lockedContent.setLayout(new javax.swing.BoxLayout(lockedContent, javax.swing.BoxLayout.Y_AXIS));
        lockedContent.setOpaque(false);
        lockedContent.add(lockedMessageLabel);
        lockedContent.add(lockedHintLabel);

        JPanel lockedWrapper = new JPanel(new BorderLayout());
        lockedWrapper.setOpaque(false);
        lockedWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 48, 0, 48));
        lockedWrapper.add(lockedContent, BorderLayout.CENTER);
        lockedOverlayPanel.add(lockedWrapper, BorderLayout.CENTER);

        MouseAdapter dismissLocked = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                hideLockedOverlay();
            }
        };
        lockedOverlayPanel.addMouseListener(dismissLocked);

        popupLayer = new JPanel(null);
        popupLayer.setOpaque(false);
        popupLayer.setVisible(false);

        popupCard = new GlassPanel(GuiTheme.HUD_BG_STRONG, GuiTheme.CORNER_RADIUS);
        popupCard.setLayout(new javax.swing.BoxLayout(popupCard, javax.swing.BoxLayout.Y_AXIS));
        popupCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 22, 14, 22));

        popupMessageLabel = new JLabel("", SwingConstants.CENTER);
        popupMessageLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        popupMessageLabel.setFont(GuiTheme.FONT_BODY);
        popupCard.add(popupMessageLabel);

        popupCloseButton = new StyledGlassButton("关闭", GuiTheme.FONT_SMALL);
        popupCloseButton.setAlignmentX(CENTER_ALIGNMENT);
        popupCloseButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        popupCloseButton.addActionListener(event -> hidePopup());
        popupCard.add(popupCloseButton);

        popupLayer.add(popupCard);

        outcomeLayer = new JPanel(null);
        outcomeLayer.setOpaque(false);
        outcomeLayer.setVisible(false);

        outcomeCard = new GlassPanel(GuiTheme.HUD_BG_STRONG, GuiTheme.CORNER_RADIUS);
        outcomeCard.setLayout(new javax.swing.BoxLayout(outcomeCard, javax.swing.BoxLayout.Y_AXIS));
        outcomeCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(22, 28, 18, 28));

        outcomeTitleLabel = new JLabel("", SwingConstants.CENTER);
        outcomeTitleLabel.setForeground(GuiTheme.ACCENT);
        outcomeTitleLabel.setFont(GuiTheme.FONT_BOLD);
        outcomeCard.add(outcomeTitleLabel);

        outcomeMessageLabel = new JLabel("", SwingConstants.CENTER);
        outcomeMessageLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        outcomeMessageLabel.setFont(GuiTheme.FONT_BODY);
        outcomeMessageLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        outcomeCard.add(outcomeMessageLabel);

        outcomeActionButton = new StyledGlassButton("继续", GuiTheme.FONT_SMALL);
        outcomeActionButton.setAlignmentX(CENTER_ALIGNMENT);
        outcomeActionButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 0, 0));
        outcomeActionButton.addActionListener(event -> {
            hideOutcome();
            if (outcomeAction != null) {
                outcomeAction.run();
            }
        });
        outcomeCard.add(outcomeActionButton);
        outcomeLayer.add(outcomeCard);

        directionNavPanel = new JPanel(new GridBagLayout());
        directionNavPanel.setOpaque(false);
        northButton = new DirectionArrowButton(DirectionArrowButton.ArrowDirection.NORTH);
        southButton = new DirectionArrowButton(DirectionArrowButton.ArrowDirection.SOUTH);
        eastButton = new DirectionArrowButton(DirectionArrowButton.ArrowDirection.EAST);
        westButton = new DirectionArrowButton(DirectionArrowButton.ArrowDirection.WEST);
        backButton = new StyledGlassButton("返回", GuiTheme.FONT_SMALL);
        backButton.setPreferredSize(new Dimension(56, 36));
        wireDirectionButton(northButton, "north");
        wireDirectionButton(southButton, "south");
        wireDirectionButton(eastButton, "east");
        wireDirectionButton(westButton, "west");
        backButton.addActionListener(event -> {
            if (directionListener != null) {
                directionListener.onBack();
            }
        });
        layoutDirectionNav();

        slideTransitionPanel = new JPanel();
        directionalTransition = new DirectionalRoomTransition(slideTransitionPanel);

        vignettePanel = new SceneVignettePanel();
        roomItemMenu = new ItemActionMenu();

        layeredPane.add(roomImageLabel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(vignettePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(itemLayer, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(directionNavPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(npcPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(trapBannerLabel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(popupLayer, JLayeredPane.MODAL_LAYER);
        layeredPane.add(outcomeLayer, JLayeredPane.MODAL_LAYER);
        layeredPane.add(lockedOverlayPanel, JLayeredPane.MODAL_LAYER);
        layeredPane.add(roomItemMenu, JLayeredPane.MODAL_LAYER);
        layeredPane.add(slideTransitionPanel, JLayeredPane.DRAG_LAYER);
        npcPanel.setVisible(false);

        add(layeredPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layoutScene();
            }
        });

        layeredPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                dismissRoomItemMenuIfOutside(event);
            }
        });
    }

    public boolean isTransitionRunning() {
        return directionalTransition.isRunning();
    }

    /**
     * 按方向播放房间切换动画。
     *
     * @param direction 方向
     * @param midpoint 切换中执行（如 go 命令）
     * @param onComplete 结束回调
     */
    public void playDirectionalTransition(String direction, Runnable midpoint, Runnable onComplete) {
        if (directionalTransition.isRunning()) {
            return;
        }
        roomItemMenu.hideMenu();
        hidePopup();
        ImageIcon outgoing = roomIcon;
        suppressVisualRoomUpdate = true;
        itemLayer.setVisible(false);
        directionNavPanel.setVisible(false);
        directionalTransition.play(
            direction,
            outgoing,
            sceneWidth,
            sceneHeight,
            () -> {
                if (midpoint != null) {
                    midpoint.run();
                }
                return roomIcon;
            },
            () -> {
                suppressVisualRoomUpdate = false;
                itemLayer.setVisible(true);
                layoutScene();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        );
    }

    public void hideRoomItemMenu() {
        roomItemMenu.hideMenu();
    }

    private void dismissRoomItemMenuIfOutside(MouseEvent event) {
        if (!roomItemMenu.isMenuVisible()) {
            return;
        }
        java.awt.Point point = javax.swing.SwingUtilities.convertPoint(
            event.getComponent(),
            event.getPoint(),
            roomItemMenu
        );
        if (!roomItemMenu.contains(point)) {
            roomItemMenu.hideMenu();
        }
    }

    public void setRoomItemListener(RoomItemListener listener) {
        this.itemListener = listener;
    }

    public void setRoomNpcListener(RoomNpcListener listener) {
        this.npcListener = listener;
    }

    public void setDirectionListener(DirectionListener listener) {
        this.directionListener = listener;
    }

    /**
     * 刷新房间场景。
     *
     * @param room 当前房间
     * @param items 房间物品
     * @param showNpcMarker 是否显示 NPC
     * @param showFeedButton 是否显示喂猫按钮
     * @param trapBanner 西楼困锁等顶栏提示，null 则隐藏
     */
    public void updateRoom(
        Room room,
        List<Item> items,
        boolean showNpcMarker,
        boolean showFeedButton,
        String trapBanner) {
        currentRoom = room;
        currentItems = items == null ? new ArrayList<>() : new ArrayList<>(items);
        showNpc = showNpcMarker;
        showFeed = showFeedButton;
        westTrapText = trapBanner;
        String roomId = room == null ? null : room.getRoomId();
        roomIcon = imageLoader.getRoomImage(roomId);
        updateNpcMarker(roomId);
        updateTrapBanner();
        layoutScene();
    }

    /**
     * 更新中央方向箭头可用状态。
     *
     * @param north 北
     * @param south 南
     * @param east 东
     * @param west 西
     * @param back 返回
     */
    public void updateDirectionAvailability(boolean north, boolean south, boolean east, boolean west, boolean back) {
        setDirectionVisible(northButton, north);
        setDirectionVisible(southButton, south);
        setDirectionVisible(eastButton, east);
        setDirectionVisible(westButton, west);
        setBackVisible(back);
        directionNavPanel.setVisible(north || south || east || west || back);
    }

    /**
     * 显示小弹窗公告（场景仍可见）。
     *
     * @param message 文案
     */
    public void showPopup(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        popupMessageLabel.setText(formatNoticeHtml(message, 360));
        popupLayer.setVisible(true);
        positionPopupCard();
        layeredPane.repaint();
    }

    /**
     * 隐藏小弹窗。
     */
    public void hidePopup() {
        popupLayer.setVisible(false);
        layeredPane.repaint();
    }

    public boolean isPopupVisible() {
        return popupLayer.isVisible();
    }

    /**
     * 显示结局专用弹层（超时 / 通关 / 全通）。
     *
     * @param title 标题
     * @param message 正文
     * @param actionLabel 按钮文案
     * @param onAction 按钮回调
     */
    public void showOutcome(String title, String message, String actionLabel, Runnable onAction) {
        hidePopup();
        hideLockedOverlay();
        roomItemMenu.hideMenu();
        outcomeTitleLabel.setText(title == null ? "" : title);
        outcomeMessageLabel.setText(formatNoticeHtml(message, 420));
        outcomeActionButton.setText(actionLabel == null ? "继续" : actionLabel);
        outcomeAction = onAction;
        outcomeLayer.setVisible(true);
        positionOutcomeCard();
        layeredPane.moveToFront(outcomeLayer);
        layeredPane.repaint();
    }

    public void hideOutcome() {
        outcomeLayer.setVisible(false);
        outcomeAction = null;
        layeredPane.repaint();
    }

    public boolean isOutcomeVisible() {
        return outcomeLayer.isVisible();
    }

    /**
     * 显示未开放方向的全屏黑幕提示。
     *
     * @param message 提示文案
     */
    public void showLockedOverlay(String message) {
        hidePopup();
        lockedMessageLabel.setText(formatNoticeHtml(
            message == null ? LevelConfig.LOCKED_EXIT_MESSAGE : message,
            420
        ));
        lockedOverlayPanel.setVisible(true);
        layeredPane.repaint();
    }

    /**
     * 隐藏全屏黑幕。
     */
    public void hideLockedOverlay() {
        lockedOverlayPanel.setVisible(false);
        layeredPane.repaint();
    }

    public boolean isLockedOverlayVisible() {
        return lockedOverlayPanel.isVisible();
    }

    /**
     * 当前场景宽度（供测试）。
     *
     * @return 场景宽
     */
    int getSceneWidthForTest() {
        return sceneWidth;
    }

    private void positionOutcomeCard() {
        if (!outcomeLayer.isVisible()) {
            return;
        }
        int cardWidth = Math.min(480, sceneWidth - 80);
        int cardHeight = Math.min(320, sceneHeight / 2 + 40);
        int x = (sceneWidth - cardWidth) / 2;
        int y = (sceneHeight - cardHeight) / 2;
        outcomeCard.setBounds(x, y, cardWidth, cardHeight);
    }

    private void positionPopupCard() {
        if (!popupLayer.isVisible()) {
            return;
        }
        int cardWidth = Math.min(440, sceneWidth - 64);
        int cardHeight = Math.min(280, sceneHeight / 2);
        int x = (sceneWidth - cardWidth) / 2;
        int y = (int) (sceneHeight * 0.22);
        popupCard.setBounds(x, y, cardWidth, cardHeight);
    }

    private String formatNoticeHtml(String message, int widthPx) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        String escaped = message
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>");
        return "<html><body style='width:" + widthPx + "px;text-align:center;line-height:1.5'>"
            + escaped
            + "</body></html>";
    }

    private void wireDirectionButton(DirectionArrowButton button, String direction) {
        button.addActionListener(event -> {
            if (directionListener != null && button.isEnabled() && button.isVisible()) {
                directionListener.onNavigate(direction);
            }
        });
    }

    private void layoutDirectionNav() {
        directionNavPanel.removeAll();
        GridBagConstraints center = new GridBagConstraints();
        center.gridx = 1;
        center.gridy = 1;
        center.insets = new Insets(4, 4, 4, 4);

        GridBagConstraints north = new GridBagConstraints();
        north.gridx = 1;
        north.gridy = 0;

        GridBagConstraints south = new GridBagConstraints();
        south.gridx = 1;
        south.gridy = 2;

        GridBagConstraints west = new GridBagConstraints();
        west.gridx = 0;
        west.gridy = 1;

        GridBagConstraints east = new GridBagConstraints();
        east.gridx = 2;
        east.gridy = 1;

        directionNavPanel.add(northButton, north);
        directionNavPanel.add(southButton, south);
        directionNavPanel.add(westButton, west);
        directionNavPanel.add(eastButton, east);
        directionNavPanel.add(backButton, center);
    }

    private void setDirectionVisible(DirectionArrowButton button, boolean available) {
        button.setVisible(available);
        button.setEnabled(available);
    }

    private void setBackVisible(boolean available) {
        backButton.setVisible(available);
        backButton.setEnabled(available);
    }

    private void layoutScene() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth < 80 || panelHeight < 80) {
            return;
        }

        sceneWidth = panelWidth;
        sceneHeight = panelHeight;

        layeredPane.setBounds(0, 0, sceneWidth, sceneHeight);

        roomImageLabel.setBounds(0, 0, sceneWidth, sceneHeight);
        if (!suppressVisualRoomUpdate && !directionalTransition.isRunning()) {
            roomImageLabel.setIcon(imageLoader.scaleCover(roomIcon, sceneWidth, sceneHeight));
        }

        vignettePanel.setBounds(0, 0, sceneWidth, sceneHeight);

        itemLayer.setBounds(0, 0, sceneWidth, sceneHeight);
        lockedOverlayPanel.setBounds(0, 0, sceneWidth, sceneHeight);
        popupLayer.setBounds(0, 0, sceneWidth, sceneHeight);
        outcomeLayer.setBounds(0, 0, sceneWidth, sceneHeight);
        slideTransitionPanel.setBounds(0, 0, sceneWidth, sceneHeight);
        positionPopupCard();
        positionOutcomeCard();

        int navWidth = 196;
        int navHeight = 196;
        directionNavPanel.setBounds(
            (sceneWidth - navWidth) / 2,
            (sceneHeight - navHeight) / 2,
            navWidth,
            navHeight
        );

        rebuildItemMarkers();
        positionNpcPanel();
        positionTrapBanner();
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private void updateTrapBanner() {
        if (westTrapText == null || westTrapText.trim().isEmpty()) {
            trapBannerLabel.setVisible(false);
        } else {
            trapBannerLabel.setText(westTrapText);
            trapBannerLabel.setVisible(true);
        }
    }

    private void positionTrapBanner() {
        if (!trapBannerLabel.isVisible()) {
            return;
        }
        trapBannerLabel.setBounds(16, 16, sceneWidth - 32, 36);
    }

    private void updateNpcMarker(String roomId) {
        if (!showNpc || roomId == null) {
            npcPanel.setVisible(false);
            return;
        }
        npcImageLabel.setIcon(imageLoader.scale(
            imageLoader.getNpcImage(roomId),
            NPC_ICON_SIZE,
            NPC_ICON_SIZE
        ));
        feedButton.setVisible(showFeed);
        npcPanel.setVisible(true);
    }

    private void positionNpcPanel() {
        if (!npcPanel.isVisible()) {
            return;
        }
        int npcWidth = NPC_ICON_SIZE + 180;
        int npcHeight = NPC_ICON_SIZE + 48;
        int x = sceneWidth - npcWidth - 24;
        int y = (int) (sceneHeight * 0.18);
        npcPanel.setBounds(x, y, npcWidth, npcHeight);
    }

    private void rebuildItemMarkers() {
        roomItemMenu.hideMenu();
        itemLayer.removeAll();
        String roomId = currentRoom == null ? null : currentRoom.getRoomId();
        double[][] anchors = RoomLayoutDefaults.getAnchors(roomId);
        for (int index = 0; index < currentItems.size(); index++) {
            Item item = currentItems.get(index);
            double[] anchor = anchors[index % anchors.length];
            int x = (int) (anchor[0] * sceneWidth) - ITEM_ICON_SIZE / 2;
            int y = (int) (anchor[1] * sceneHeight) - ITEM_ICON_SIZE / 2;
            itemLayer.add(createItemMarker(item, x, y));
        }
        itemLayer.revalidate();
        itemLayer.repaint();
    }

    private JPanel createItemMarker(Item item, int x, int y) {
        int markerSize = ITEM_ICON_SIZE + 12;
        final int menuAnchorX = x + markerSize + 4;
        final int menuAnchorY = y + 8;

        JPanel marker = new JPanel(null) {
            private boolean hovered;

            @Override
            protected void paintComponent(Graphics graphics) {
                if (hovered) {
                    Graphics2D graphics2d = (Graphics2D) graphics.create();
                    graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics2d.setColor(new Color(GuiTheme.ACCENT.getRed(), GuiTheme.ACCENT.getGreen(),
                        GuiTheme.ACCENT.getBlue(), 70));
                    graphics2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
                    graphics2d.setColor(GuiTheme.ACCENT);
                    graphics2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
                    graphics2d.dispose();
                }
                super.paintComponent(graphics);
            }

            private void openMenu() {
                openRoomItemMenu(item, menuAnchorX, menuAnchorY);
            }

            {
                MouseAdapter adapter = new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent event) {
                        hovered = true;
                        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent event) {
                        event.consume();
                        openMenu();
                    }
                };
                addMouseListener(adapter);
            }
        };
        marker.setOpaque(false);
        marker.setBounds(x, y, markerSize, markerSize);

        JLabel iconLabel = new JLabel(imageLoader.scale(
            imageLoader.getItemImage(item.getDescription()),
            ITEM_ICON_SIZE,
            ITEM_ICON_SIZE
        ));
        iconLabel.setBounds(6, 6, ITEM_ICON_SIZE, ITEM_ICON_SIZE);
        iconLabel.setToolTipText(item.getDescription());
        iconLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        iconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                event.consume();
                openRoomItemMenu(item, menuAnchorX, menuAnchorY);
            }
        });
        marker.add(iconLabel);
        return marker;
    }

    private void openRoomItemMenu(Item item, int anchorX, int anchorY) {
        if (itemListener == null) {
            return;
        }
        layeredPane.moveToFront(roomItemMenu);
        roomItemMenu.showAt(
            anchorX,
            anchorY,
            sceneWidth,
            sceneHeight,
            new ItemActionMenu.Entry("拾取", () -> itemListener.onTakeItem(item)),
            new ItemActionMenu.Entry("查看", () -> itemListener.onInspectRoomItem(item))
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(960, 640);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(GuiTheme.WINDOW_BG);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }
}

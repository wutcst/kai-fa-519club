package cn.edu.whut.sept.zuul.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Item;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;
import cn.edu.whut.sept.zuul.level.LevelConfig;
import cn.edu.whut.sept.zuul.level.LevelState;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * F7 图形界面：沉浸式全屏场景 + 半透明 HUD（无日志栏，公告弹层展示）。
 */
public class EnhancedGameWindow extends JFrame {

    private Game game;
    private final GameGuiController controller;
    private final ImageLoader imageLoader;

    private JLabel timerLabel;
    private JLabel levelLabel;
    private RoomScenePanel scenePanel;
    private InventorySlotPanel inventoryPanel;
    private GlassPanel topHudPanel;
    private GlassPanel bottomHudPanel;
    private GlassPanel actionPanel;
    private JLayeredPane rootLayer;
    private ItemActionMenu inventoryActionMenu;
    private GlassModalLayer glassModalLayer;

    private StyledGlassButton lookButton;
    private StyledGlassButton sleepButton;
    private StyledGlassButton submitButton;
    private StyledGlassButton combineButton;
    private StyledGlassButton feedActionButton;
    private StyledGlassButton unlockButton;

    private Timer uiRefreshTimer;
    private Timer timerPulseTimer;
    private boolean timerPulseBright = true;
    private LevelState trackedLevelState = LevelState.IN_PROGRESS;

    public EnhancedGameWindow(Game game) {
        this.game = game;
        this.controller = new GameGuiController();
        this.imageLoader = ImageLoader.getInstance();
        initializeWindow();
        createComponents();
        layoutComponents();
        wireListeners();
        controller.prepareGuiSession(this.game);
        trackedLevelState = game.getLevelManager().getState();
        refreshDisplay();
    }

    private void initializeWindow() {
        setTitle("熄灯前归寝 - 图形界面");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(GuiTheme.WINDOW_BG);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                controller.shutdownGuiSession(game);
                dispose();
                System.exit(0);
            }
        });
    }

    private void createComponents() {
        scenePanel = new RoomScenePanel(imageLoader);
        inventoryPanel = new InventorySlotPanel(imageLoader);

        timerLabel = new JLabel("距熄灯（23:00）还有 0 秒", SwingConstants.RIGHT);
        timerLabel.setFont(GuiTheme.FONT_TIMER);
        timerLabel.setForeground(GuiTheme.TEXT_PRIMARY);

        levelLabel = new JLabel("", SwingConstants.RIGHT);
        levelLabel.setFont(GuiTheme.FONT_SMALL);
        levelLabel.setForeground(GuiTheme.TEXT_MUTED);

        lookButton = createActionButton("环顾");
        sleepButton = createActionButton("睡觉");
        submitButton = createActionButton("提交归寝单");
        combineButton = createActionButton("合成锤子");
        feedActionButton = createActionButton("喂猫");
        unlockButton = createActionButton("解锁寝室");

        lookButton.addActionListener(event -> performLook());
        sleepButton.addActionListener(event -> runCommand("sleep", null, true));
        submitButton.addActionListener(event -> submitDormForm());
        combineButton.addActionListener(event -> runCommand("combine", null, true));
        feedActionButton.addActionListener(event -> runCommand("feed", null, true));
        unlockButton.addActionListener(event -> promptUnlockPassword());
    }

    private void layoutComponents() {
        rootLayer = new JLayeredPane();
        rootLayer.setLayout(null);
        rootLayer.setPreferredSize(new Dimension(1280, 800));

        topHudPanel = new GlassPanel();
        topHudPanel.setLayout(new BorderLayout(0, 4));
        topHudPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel timerPanel = new JPanel(new BorderLayout(0, 4));
        timerPanel.setOpaque(false);
        timerPanel.add(timerLabel, BorderLayout.NORTH);
        timerPanel.add(levelLabel, BorderLayout.SOUTH);
        topHudPanel.add(timerPanel, BorderLayout.CENTER);

        actionPanel = new GlassPanel(GuiTheme.HUD_BG, GuiTheme.CORNER_RADIUS_SM);
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        actionPanel.add(lookButton);
        actionPanel.add(submitButton);
        actionPanel.add(combineButton);
        actionPanel.add(feedActionButton);
        actionPanel.add(unlockButton);
        actionPanel.add(sleepButton);

        bottomHudPanel = new GlassPanel();
        bottomHudPanel.setLayout(new BorderLayout(0, 8));
        bottomHudPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 10, 10));
        bottomHudPanel.add(actionPanel, BorderLayout.NORTH);
        bottomHudPanel.add(inventoryPanel, BorderLayout.CENTER);

        rootLayer.add(scenePanel, JLayeredPane.DEFAULT_LAYER);
        rootLayer.add(topHudPanel, JLayeredPane.PALETTE_LAYER);
        rootLayer.add(bottomHudPanel, JLayeredPane.PALETTE_LAYER);

        inventoryActionMenu = new ItemActionMenu();
        rootLayer.add(inventoryActionMenu, JLayeredPane.MODAL_LAYER);

        glassModalLayer = new GlassModalLayer();
        rootLayer.add(glassModalLayer, JLayeredPane.MODAL_LAYER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(GuiTheme.WINDOW_BG);
        wrapper.add(rootLayer, BorderLayout.CENTER);
        setContentPane(wrapper);
        setJMenuBar(createMenuBar());

        rootLayer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layoutOverlays();
            }
        });
        layoutOverlays();
    }

    private StyledGlassButton createActionButton(String label) {
        StyledGlassButton button = new StyledGlassButton(label, GuiTheme.FONT_SMALL);
        button.setAccentColor(GuiTheme.ACCENT);
        return button;
    }

    private void layoutOverlays() {
        int width = rootLayer.getWidth();
        int height = rootLayer.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        scenePanel.setBounds(0, 0, width, height);
        topHudPanel.setBounds(width - 280, 16, 264, 72);
        bottomHudPanel.setBounds(16, height - 196, width - 32, 180);
        glassModalLayer.layoutToSize(width, height);
        rootLayer.revalidate();
        rootLayer.repaint();
    }

    private void wireListeners() {
        scenePanel.setRoomItemListener(new RoomScenePanel.RoomItemListener() {
            @Override
            public void onTakeItem(Item item) {
                runCommand("take", item.getDescription(), true);
                GuiPhase3Helper.promptCombineIfReady(
                    glassModalLayer,
                    game,
                    () -> runCommand("combine", null, true)
                );
            }

            @Override
            public void onInspectRoomItem(Item item) {
                ActionTimeCost.deduct(game, ActionTimeCost.LOOK);
                scenePanel.showPopup(item.getLongDescription());
                refreshDisplay();
            }
        });

        inventoryPanel.setInventoryItemListener(new InventorySlotPanel.InventoryItemListener() {
            @Override
            public void onDropItem(Item item) {
                runCommand("drop", item.getDescription(), true);
            }

            @Override
            public void onUseItem(Item item) {
                runCommand("use", item.getDescription(), true);
            }

            @Override
            public void onInspectItem(Item item) {
                runCommand("inspect", item.getDescription(), true);
            }
        });

        inventoryPanel.setSlotClickListener((item, slotView) -> {
            java.awt.Point origin = SwingUtilities.convertPoint(slotView, 0, 0, rootLayer);
            List<ItemActionMenu.Entry> entries = new ArrayList<>();
            entries.add(new ItemActionMenu.Entry("丢弃",
                () -> runCommand("drop", item.getDescription(), true)));
            if (FoodItems.isEdible(item.getDescription())) {
                entries.add(new ItemActionMenu.Entry("吃",
                    () -> runCommand("eat", item.getDescription(), true)));
            }
            entries.add(new ItemActionMenu.Entry("使用",
                () -> runCommand("use", item.getDescription(), true)));
            entries.add(new ItemActionMenu.Entry("查看",
                () -> runCommand("inspect", item.getDescription(), true)));
            rootLayer.moveToFront(inventoryActionMenu);
            inventoryActionMenu.showNearSlot(
                origin.x,
                origin.y,
                slotView.getWidth(),
                slotView.getHeight(),
                rootLayer.getWidth(),
                rootLayer.getHeight(),
                entries.toArray(new ItemActionMenu.Entry[0])
            );
        });

        scenePanel.setRoomNpcListener(new RoomScenePanel.RoomNpcListener() {
            @Override
            public void onTalk() {
                openNpcDialog();
            }

            @Override
            public void onFeed() {
                runCommand("feed", null, true);
            }
        });

        scenePanel.setDirectionListener(new RoomScenePanel.DirectionListener() {
            @Override
            public void onNavigate(String direction) {
                move(direction);
            }

            @Override
            public void onBack() {
                if (isInteractionBlocked()) {
                    return;
                }
                scenePanel.hidePopup();
                scenePanel.playDirectionalTransition("back", () -> runCommand("back", null, false), null);
            }
        });

        uiRefreshTimer = new Timer(1000, event -> refreshTimerLabels());
        uiRefreshTimer.start();

        timerPulseTimer = new Timer(600, event -> {
            if (game.getLevelTimer() != null && game.getLevelTimer().getRemainingSeconds() <= 60) {
                timerPulseBright = !timerPulseBright;
                refreshTimerLabels();
            }
        });
        timerPulseTimer.start();
    }

    private void performLook() {
        String bulletin = controller.buildBulletinText(game);
        runCommand("look", null, false);
        scenePanel.showPopup(bulletin);
    }

    private void openNpcDialog() {
        List<String> lines = NpcDialogHelper.performTalk(game);
        scenePanel.showPopup(String.join("\n", lines));
        refreshDisplay();
    }

    private void submitDormForm() {
        Player player = game.getPlayer();
        if (player.findItemInInventory(UseCommand.DORM_FORM_ITEM) == null) {
            scenePanel.showPopup("背包中没有归寝单，请先向志愿者或图书馆工作人员领取。");
            return;
        }
        runCommand("submit", UseCommand.DORM_FORM_ITEM, true);
    }

    private void promptUnlockPassword() {
        glassModalLayer.showTextInput(
            "解锁寝室",
            "请输入寝室智能锁八位密码：",
            "",
            "解锁",
            "取消",
            password -> {
                if (password != null && !password.trim().isEmpty()) {
                    runCommand("unlock", password.trim(), true);
                }
            },
            null
        );
    }

    private boolean isInteractionBlocked() {
        return scenePanel.isTransitionRunning()
            || scenePanel.isLockedOverlayVisible()
            || scenePanel.isOutcomeVisible()
            || glassModalLayer.isDialogVisible();
    }

    private void move(String direction) {
        if (isInteractionBlocked()) {
            return;
        }
        scenePanel.hidePopup();
        Room currentRoom = game.getCurrentRoom();
        Room target = currentRoom == null ? null : currentRoom.getExit(direction);
        if (target == null) {
            scenePanel.showPopup("此路不通。");
            return;
        }
        if (!game.isRoomAccessible(target)) {
            scenePanel.showLockedOverlay(LevelConfig.LOCKED_EXIT_MESSAGE);
            return;
        }

        scenePanel.hideLockedOverlay();
        scenePanel.playDirectionalTransition(direction, () -> {
            GameGuiController.CommandResult result = runCommand("go", direction, false);
            if (result.isLockedExitAttempt()) {
                scenePanel.showLockedOverlay(LevelConfig.LOCKED_EXIT_MESSAGE);
            }
        }, null);
    }

    private void handlePostCommandEffects(GameGuiController.CommandResult result) {
        if (result.isDarkPenaltyTriggered()) {
            scenePanel.showPopup(DarkRoom.PENALTY_MESSAGE);
        }
        if (result.getGatedDenialMessage() != null) {
            scenePanel.showPopup(result.getGatedDenialMessage());
        }
        if (result.isTeleported()) {
            scenePanel.showPopup("你进入体育馆后被传送到校园另一处！");
        }
    }

    private GameGuiController.CommandResult runCommand(
        String commandWord,
        String secondWord,
        boolean showOutputNotice) {
        GameGuiController.CommandResult result = controller.execute(game, commandWord, secondWord);
        refreshDisplay();
        handlePostCommandEffects(result);
        GuiOutcomeHelper.OutcomeType outcome = GuiOutcomeHelper.detectFromOutput(result.getOutputLines());
        if (outcome != GuiOutcomeHelper.OutcomeType.NONE) {
            presentOutcome(outcome, result.getOutputLines());
        } else if (showOutputNotice && !result.getOutputLines().isEmpty()) {
            scenePanel.showPopup(result.joinedOutput());
        }
        return result;
    }

    private void presentOutcome(GuiOutcomeHelper.OutcomeType type, List<String> outputLines) {
        if (scenePanel.isOutcomeVisible()) {
            return;
        }
        scenePanel.showOutcome(
            GuiOutcomeHelper.buildTitle(type),
            GuiOutcomeHelper.buildMessage(type, game, outputLines),
            GuiOutcomeHelper.buildActionLabel(type),
            () -> {
                if (type == GuiOutcomeHelper.OutcomeType.LEVEL_FAILED) {
                    controller.execute(game, "restart", null);
                }
                refreshDisplay();
            }
        );
    }

    private void refreshDisplay() {
        inventoryActionMenu.hideMenu();
        Room room = game.getCurrentRoom();
        refreshTimerLabels();
        scenePanel.updateRoom(
            room,
            room == null ? List.of() : room.getItems(),
            NpcDialogHelper.shouldShowNpc(
                room == null ? null : room.getRoomId(),
                game.getLevelManager().getCurrentLevel()
            ),
            GuiPhase3Helper.shouldShowFeedButton(game),
            GuiPhase3Helper.westTrapBannerText(game)
        );
        inventoryPanel.updateInventory(game.getPlayer().getInventory());
        updateDirectionAvailability(room);
        sleepButton.setVisible(room != null
            && UnlockService.DORMITORY_ROOM_ID.equals(room.getRoomId()));
        submitButton.setVisible(NpcDialogHelper.canSubmitAtSupermarket(game));
        combineButton.setVisible(GuiPhase3Helper.shouldShowCombineButton(game));
        feedActionButton.setVisible(GuiPhase3Helper.shouldShowFeedButton(game));
        unlockButton.setVisible(GuiPhase3Helper.shouldShowUnlockButton(game));
    }

    private void refreshTimerLabels() {
        if (game.getLevelTimer() == null) {
            return;
        }
        LevelState currentState = game.getLevelManager().getState();
        GuiOutcomeHelper.OutcomeType transitionOutcome =
            GuiOutcomeHelper.detectFromStateTransition(trackedLevelState, currentState);
        if (transitionOutcome != GuiOutcomeHelper.OutcomeType.NONE && !scenePanel.isOutcomeVisible()) {
            presentOutcome(transitionOutcome, List.of(GuiOutcomeHelper.FAIL_SNIPPET + "。"));
        }
        trackedLevelState = currentState;

        timerLabel.setText(game.getLevelTimer().getDisplayText());
        levelLabel.setText(controller.buildLevelTitle(game));

        int remaining = game.getLevelTimer().getRemainingSeconds();
        if (remaining <= 60) {
            timerLabel.setForeground(timerPulseBright ? GuiTheme.DANGER : GuiTheme.DANGER.darker());
        } else if (remaining <= 120) {
            timerLabel.setForeground(new Color(255, 196, 96));
        } else {
            timerLabel.setForeground(GuiTheme.TEXT_PRIMARY);
        }
    }

    private void updateDirectionAvailability(Room room) {
        boolean eastAvailable = hasExit(room, "east") && !game.isTrappedInWestBuilding();
        scenePanel.updateDirectionAvailability(
            hasExit(room, "north"),
            hasExit(room, "south"),
            eastAvailable,
            hasExit(room, "west"),
            !game.isTrappedInWestBuilding()
        );
    }

    private boolean hasExit(Room room, String direction) {
        return room != null && room.getExit(direction) != null;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("游戏");

        JMenuItem newGameItem = new JMenuItem("新游戏");
        newGameItem.addActionListener(event -> restartGame());
        JMenuItem saveItem = new JMenuItem("保存");
        saveItem.addActionListener(event -> GameSaveUiHelper.saveGame(glassModalLayer, game, null));
        JMenuItem loadItem = new JMenuItem("读档");
        loadItem.addActionListener(event -> GameSaveUiHelper.loadGame(
            glassModalLayer,
            game,
            this::syncUiAfterPersistedStateChange
        ));

        gameMenu.add(newGameItem);
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        menuBar.add(gameMenu);
        return menuBar;
    }

    private void restartGame() {
        glassModalLayer.showConfirm(
            "新游戏",
            "确定重新开始？当前进度将丢失。",
            "重新开始",
            "取消",
            () -> {
                controller.shutdownGuiSession(game);
                game = new Game();
                controller.prepareGuiSession(game);
                syncUiAfterPersistedStateChange();
                scenePanel.showPopup("新游戏开始。\n点击「环顾」可查看本关任务与房间公告。");
            },
            null
        );
    }

    private void syncUiAfterPersistedStateChange() {
        trackedLevelState = game.getLevelManager().getState();
        controller.prepareGuiSession(game);
        scenePanel.hidePopup();
        scenePanel.hideOutcome();
        scenePanel.hideLockedOverlay();
        glassModalLayer.hideDialog();
        refreshDisplay();
    }

    /**
     * 显示窗口。
     */
    public void start() {
        setVisible(true);
        refreshDisplay();
        SwingUtilities.invokeLater(() -> scenePanel.showPopup(
            "欢迎来到《熄灯前归寝》。\n点击中央箭头移动，点击「环顾」查看公告与任务。"
        ));
    }

    /**
     * 供测试访问场景面板。
     *
     * @return RoomScenePanel
     */
    RoomScenePanel getScenePanelForTest() {
        return scenePanel;
    }

    /**
     * 供测试访问物品栏面板。
     *
     * @return InventorySlotPanel
     */
    InventorySlotPanel getInventoryPanelForTest() {
        return inventoryPanel;
    }

    /**
     * 供测试访问控制器。
     *
     * @return GameGuiController
     */
    GameGuiController getControllerForTest() {
        return controller;
    }

    GlassModalLayer getGlassModalLayerForTest() {
        return glassModalLayer;
    }
}

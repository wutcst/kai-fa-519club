package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cn.edu.whut.sept.zuul.Item;

/**
 * 半透明悬浮物品栏（点击槽位由窗口级菜单展示操作）。
 */
public class InventorySlotPanel extends GlassPanel {

    public interface InventoryItemListener {
        void onDropItem(Item item);

        void onUseItem(Item item);

        void onInspectItem(Item item);
    }

    /**
     * 槽位被点击时通知宿主在窗口层弹出菜单。
     */
    public interface SlotClickListener {
        void onSlotClicked(Item item, java.awt.Component slotView);
    }

    public static final int SLOT_COUNT = 6;
    private static final int THUMB_SIZE = 52;
    private static final int SLOT_GAP = 10;
    private static final int SLOT_FRAME = 8;

    private final ImageLoader imageLoader;
    private final List<SlotPanel> slots = new ArrayList<>();
    private InventoryItemListener listener;
    private SlotClickListener slotClickListener;

    public InventorySlotPanel(ImageLoader imageLoader) {
        super(GuiTheme.HUD_BG_STRONG, GuiTheme.CORNER_RADIUS);
        this.imageLoader = imageLoader;
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel slotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, SLOT_GAP, 0));
        slotRow.setOpaque(false);
        for (int index = 0; index < SLOT_COUNT; index++) {
            SlotPanel slot = new SlotPanel(index);
            slots.add(slot);
            slotRow.add(slot);
        }
        add(slotRow);
    }

    public void setInventoryItemListener(InventoryItemListener listener) {
        this.listener = listener;
    }

    public void setSlotClickListener(SlotClickListener slotClickListener) {
        this.slotClickListener = slotClickListener;
    }

    /**
     * 刷新背包显示。
     *
     * @param items 背包物品
     */
    public void updateInventory(List<Item> items) {
        for (SlotPanel slot : slots) {
            slot.clear();
        }
        if (items == null) {
            return;
        }
        int count = Math.min(items.size(), SLOT_COUNT);
        for (int index = 0; index < count; index++) {
            slots.get(index).setItem(items.get(index));
        }
    }

    private void notifySlotClicked(SlotPanel slot, Item item) {
        if (slotClickListener != null) {
            slotClickListener.onSlotClicked(item, slot);
        }
    }

    private final class SlotPanel extends JPanel {
        private Item item;
        private boolean hovered;

        private SlotPanel(int index) {
            setLayout(null);
            setOpaque(false);
            setPreferredSize(new Dimension(THUMB_SIZE + SLOT_FRAME, THUMB_SIZE + SLOT_FRAME));
            setToolTipText("空槽 " + (index + 1));

            MouseAdapter clickAdapter = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                    if (item != null) {
                        notifySlotClicked(SlotPanel.this, item);
                    }
                }
            };
            addMouseListener(clickAdapter);
        }

        private void clear() {
            this.item = null;
            removeAll();
            setToolTipText("空槽");
            revalidate();
            repaint();
        }

        private void setItem(Item newItem) {
            this.item = newItem;
            removeAll();

            MouseAdapter clickAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    event.consume();
                    notifySlotClicked(SlotPanel.this, item);
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            };

            JLabel icon = new JLabel(imageLoader.scale(
                imageLoader.getItemImage(newItem.getDescription()),
                THUMB_SIZE,
                THUMB_SIZE
            ), SwingConstants.CENTER);
            icon.setBounds(SLOT_FRAME / 2, SLOT_FRAME / 2, THUMB_SIZE, THUMB_SIZE);
            icon.setToolTipText(newItem.getDescription());
            icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            icon.addMouseListener(clickAdapter);
            add(icon);

            addMouseListener(clickAdapter);

            setToolTipText(newItem.getDescription());
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2d = (Graphics2D) graphics.create();
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = item == null ? GuiTheme.SLOT_EMPTY : GuiTheme.SLOT_FILL;
            Color border = hovered ? GuiTheme.SLOT_BORDER_ACTIVE : GuiTheme.SLOT_BORDER;
            graphics2d.setColor(fill);
            graphics2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, GuiTheme.CORNER_RADIUS_SM,
                GuiTheme.CORNER_RADIUS_SM);
            graphics2d.setColor(border);
            graphics2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, GuiTheme.CORNER_RADIUS_SM,
                GuiTheme.CORNER_RADIUS_SM);
            graphics2d.dispose();
            super.paintComponent(graphics);
        }
    }
}

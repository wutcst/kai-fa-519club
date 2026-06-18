package cn.edu.whut.sept.zuul.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * 竖向浮动操作菜单（避免被父容器裁剪）。
 */
public class ItemActionMenu extends GlassPanel {

    private static final int BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 30;
    private static final int GAP = 5;
    private static final int PADDING = 8;

    /**
     * 菜单项。
     */
    public static final class Entry {
        private final String label;
        private final Runnable action;

        public Entry(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    public ItemActionMenu() {
        super(GuiTheme.HUD_BG_STRONG, GuiTheme.CORNER_RADIUS_SM);
        setLayout(null);
        setVisible(false);
    }

    /**
     * 在容器坐标系内显示菜单，自动避让边界。
     *
     * @param anchorX 锚点 X
     * @param anchorY 锚点 Y
     * @param containerWidth 容器宽
     * @param containerHeight 容器高
     * @param entries 菜单项
     */
    public void showAt(int anchorX, int anchorY, int containerWidth, int containerHeight, Entry... entries) {
        removeAll();
        if (entries == null || entries.length == 0) {
            hideMenu();
            return;
        }

        int innerY = PADDING;
        for (Entry entry : entries) {
            StyledGlassButton button = new StyledGlassButton(entry.label, GuiTheme.FONT_SMALL);
            button.setBounds(PADDING, innerY, BUTTON_WIDTH, BUTTON_HEIGHT);
            button.addActionListener(event -> {
                hideMenu();
                if (entry.action != null) {
                    entry.action.run();
                }
            });
            add(button);
            innerY += BUTTON_HEIGHT + GAP;
        }

        int menuWidth = BUTTON_WIDTH + PADDING * 2;
        int menuHeight = innerY + PADDING - GAP;
        int x = anchorX;
        int y = anchorY;

        if (x + menuWidth > containerWidth - 8) {
            x = anchorX - menuWidth - 8;
        }
        if (y + menuHeight > containerHeight - 8) {
            y = anchorY - menuHeight - 8;
        }
        if (x < 8) {
            x = 8;
        }
        if (y < 8) {
            y = 8;
        }

        setBounds(x, y, menuWidth, menuHeight);
        setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * 在槽位附近显示菜单（优先显示在槽位上方，避免被底部 HUD 裁剪）。
     *
     * @param slotX 槽位 X
     * @param slotY 槽位 Y
     * @param slotWidth 槽位宽
     * @param slotHeight 槽位高
     * @param containerWidth 容器宽
     * @param containerHeight 容器高
     * @param entries 菜单项
     */
    public void showNearSlot(
        int slotX,
        int slotY,
        int slotWidth,
        int slotHeight,
        int containerWidth,
        int containerHeight,
        Entry... entries) {
        removeAll();
        if (entries == null || entries.length == 0) {
            hideMenu();
            return;
        }

        int innerY = PADDING;
        for (Entry entry : entries) {
            StyledGlassButton button = new StyledGlassButton(entry.label, GuiTheme.FONT_SMALL);
            button.setBounds(PADDING, innerY, BUTTON_WIDTH, BUTTON_HEIGHT);
            button.addActionListener(event -> {
                hideMenu();
                if (entry.action != null) {
                    entry.action.run();
                }
            });
            add(button);
            innerY += BUTTON_HEIGHT + GAP;
        }

        int menuWidth = BUTTON_WIDTH + PADDING * 2;
        int menuHeight = innerY + PADDING - GAP;
        int x = slotX + (slotWidth - menuWidth) / 2;
        int y = slotY - menuHeight - 8;
        if (y < 8) {
            y = slotY + slotHeight + 8;
        }
        if (x + menuWidth > containerWidth - 8) {
            x = containerWidth - menuWidth - 8;
        }
        if (x < 8) {
            x = 8;
        }
        if (y + menuHeight > containerHeight - 8) {
            y = containerHeight - menuHeight - 8;
        }

        setBounds(x, y, menuWidth, menuHeight);
        setVisible(true);
        revalidate();
        repaint();
    }

    public void hideMenu() {
        setVisible(false);
    }

    public boolean isMenuVisible() {
        return isVisible();
    }

    /**
     * 便捷构建菜单项列表。
     *
     * @param entries 菜单项
     * @return 列表
     */
    public static List<Entry> entries(Entry... entries) {
        List<Entry> list = new ArrayList<>();
        if (entries != null) {
            for (Entry entry : entries) {
                list.add(entry);
            }
        }
        return list;
    }
}

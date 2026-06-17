package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * F7 界面主题色与字体（沉浸式 HUD）。
 */
public final class GuiTheme {

    public static final Color WINDOW_BG = new Color(8, 10, 16);
    public static final Color HUD_BG = new Color(15, 18, 28, 175);
    public static final Color HUD_BG_STRONG = new Color(10, 12, 20, 210);
    public static final Color SLOT_EMPTY = new Color(255, 255, 255, 32);
    public static final Color SLOT_FILL = new Color(255, 255, 255, 48);
    public static final Color SLOT_BORDER = new Color(255, 255, 255, 72);
    public static final Color SLOT_BORDER_ACTIVE = new Color(136, 198, 255, 180);
    public static final Color TEXT_PRIMARY = new Color(240, 244, 252);
    public static final Color TEXT_MUTED = new Color(180, 188, 204);
    public static final Color ACCENT = new Color(88, 166, 255);
    public static final Color ACCENT_HOVER = new Color(120, 188, 255);
    public static final Color DANGER = new Color(220, 90, 90);
    public static final Color BUTTON_BG = new Color(255, 255, 255, 42);
    public static final Color BUTTON_BG_HOVER = new Color(255, 255, 255, 78);
    public static final Color BUTTON_BORDER = new Color(255, 255, 255, 90);
    public static final Color TRANSITION_OVERLAY = new Color(0, 0, 0);

    public static final int CORNER_RADIUS = 16;
    public static final int CORNER_RADIUS_SM = 10;
    public static final int HUD_PADDING = 14;

    public static final Font FONT_BODY = new Font("Microsoft YaHei UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Microsoft YaHei UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Microsoft YaHei UI", Font.PLAIN, 11);
    public static final Font FONT_TIMER = new Font("Microsoft YaHei UI", Font.BOLD, 15);

    private GuiTheme() {
    }
}

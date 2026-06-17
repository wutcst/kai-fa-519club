package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.Timer;

/**
 * 圆角玻璃风格按钮，带悬停渐变动画。
 */
public class StyledGlassButton extends JButton {

    private static final int ANIMATION_MS = 120;
    private static final int TIMER_DELAY_MS = 16;

    private float hoverProgress;
    private boolean hovering;
    private Color accentColor = GuiTheme.ACCENT;
    private Timer hoverTimer;

    public StyledGlassButton(String text) {
        super(text);
        init();
    }

    public StyledGlassButton(String text, Font font) {
        super(text);
        setFont(font);
        init();
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor == null ? GuiTheme.ACCENT : accentColor;
    }

    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(GuiTheme.TEXT_PRIMARY);
        setFont(GuiTheme.FONT_BODY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (!isEnabled()) {
                    return;
                }
                hovering = true;
                startHoverAnimation();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovering = false;
                startHoverAnimation();
            }
        });
    }

    private void startHoverAnimation() {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        hoverTimer = new Timer(TIMER_DELAY_MS, event -> {
            float delta = (float) TIMER_DELAY_MS / ANIMATION_MS;
            if (hovering) {
                hoverProgress = Math.min(1f, hoverProgress + delta);
            } else {
                hoverProgress = Math.max(0f, hoverProgress - delta);
            }
            repaint();
            if ((hovering && hoverProgress >= 1f) || (!hovering && hoverProgress <= 0f)) {
                hoverTimer.stop();
            }
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(GuiTheme.CORNER_RADIUS_SM, height / 2);

        Color base = blend(GuiTheme.BUTTON_BG, GuiTheme.BUTTON_BG_HOVER, hoverProgress);
        if (!isEnabled()) {
            base = new Color(base.getRed(), base.getGreen(), base.getBlue(), 40);
        }
        graphics2d.setColor(base);
        graphics2d.fillRoundRect(0, 0, width, height, radius, radius);

        Color border = blend(GuiTheme.BUTTON_BORDER, accentColor, hoverProgress * 0.65f);
        graphics2d.setColor(border);
        graphics2d.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);

        if (hoverProgress > 0.05f && isEnabled()) {
            graphics2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                (int) (40 * hoverProgress)));
            graphics2d.fillRoundRect(0, 0, width, height, radius, radius);
        }

        graphics2d.dispose();
        super.paintComponent(graphics);
    }

    private static Color blend(Color from, Color to, float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        int red = (int) (from.getRed() + (to.getRed() - from.getRed()) * clamped);
        int green = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int blue = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
        int alpha = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * clamped);
        return new Color(red, green, blue, alpha);
    }
}

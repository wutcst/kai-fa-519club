package cn.edu.whut.sept.zuul.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.Timer;

/**
 * 手绘方向箭头按钮（避免字体 glyph 在 Windows 上显示为方框）。
 */
public class DirectionArrowButton extends JButton {

    /**
     * 箭头朝向。
     */
    public enum ArrowDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private static final int ANIMATION_MS = 120;
    private static final int TIMER_DELAY_MS = 16;

    private final ArrowDirection direction;
    private float hoverProgress;
    private boolean hovering;
    private Color accentColor = GuiTheme.ACCENT;
    private Timer hoverTimer;

    public DirectionArrowButton(ArrowDirection direction) {
        super("");
        this.direction = direction;
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(56, 56));
        setMinimumSize(new Dimension(56, 56));

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

        Color arrowColor = isEnabled()
            ? blend(GuiTheme.TEXT_PRIMARY, accentColor, hoverProgress * 0.5f)
            : GuiTheme.TEXT_MUTED;
        graphics2d.setColor(arrowColor);
        drawArrow(graphics2d, width, height);

        graphics2d.dispose();
    }

    private void drawArrow(Graphics2D graphics2d, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int arm = Math.min(width, height) / 5;
        Polygon triangle = new Polygon();
        switch (direction) {
            case NORTH:
                triangle.addPoint(centerX, centerY - arm);
                triangle.addPoint(centerX - arm, centerY + arm / 2);
                triangle.addPoint(centerX + arm, centerY + arm / 2);
                break;
            case SOUTH:
                triangle.addPoint(centerX, centerY + arm);
                triangle.addPoint(centerX - arm, centerY - arm / 2);
                triangle.addPoint(centerX + arm, centerY - arm / 2);
                break;
            case WEST:
                triangle.addPoint(centerX - arm, centerY);
                triangle.addPoint(centerX + arm / 2, centerY - arm);
                triangle.addPoint(centerX + arm / 2, centerY + arm);
                break;
            case EAST:
                triangle.addPoint(centerX + arm, centerY);
                triangle.addPoint(centerX - arm / 2, centerY - arm);
                triangle.addPoint(centerX - arm / 2, centerY + arm);
                break;
            default:
                break;
        }
        graphics2d.fillPolygon(triangle);
        graphics2d.setStroke(new BasicStroke(1.2f));
        graphics2d.setColor(new Color(255, 255, 255, 60));
        graphics2d.drawPolygon(triangle);
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

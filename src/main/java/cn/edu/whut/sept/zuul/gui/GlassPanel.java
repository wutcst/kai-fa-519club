package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * 半透明圆角玻璃面板。
 */
public class GlassPanel extends JPanel {

    private final Color fillColor;
    private final int cornerRadius;

    public GlassPanel(Color fillColor, int cornerRadius) {
        this.fillColor = fillColor;
        this.cornerRadius = cornerRadius;
        setOpaque(false);
    }

    public GlassPanel() {
        this(GuiTheme.HUD_BG, GuiTheme.CORNER_RADIUS);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.setColor(fillColor);
        graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        graphics2d.dispose();
        super.paintComponent(graphics);
    }
}

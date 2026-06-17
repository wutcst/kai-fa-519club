package cn.edu.whut.sept.zuul.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * 场景边缘暗角，增强沉浸感。
 */
public class SceneVignettePanel extends JPanel {

    public SceneVignettePanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            graphics2d.dispose();
            return;
        }

        Composite original = graphics2d.getComposite();
        graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));

        int edge = Math.max(48, Math.min(width, height) / 8);
        paintEdgeGradient(graphics2d, 0, 0, width, edge, true);
        paintEdgeGradient(graphics2d, 0, height - edge, width, edge, false);
        paintSideGradient(graphics2d, 0, 0, edge, height, true);
        paintSideGradient(graphics2d, width - edge, 0, edge, height, false);

        graphics2d.setComposite(original);
        graphics2d.dispose();
    }

    private void paintEdgeGradient(Graphics2D graphics2d, int x, int y, int width, int height, boolean top) {
        Color from = new Color(0, 0, 0, 110);
        Color to = new Color(0, 0, 0, 0);
        GradientPaint paint = top
            ? new GradientPaint(x, y, from, x, y + height, to)
            : new GradientPaint(x, y + height, from, x, y, to);
        graphics2d.setPaint(paint);
        graphics2d.fillRect(x, y, width, height);
    }

    private void paintSideGradient(Graphics2D graphics2d, int x, int y, int width, int height, boolean left) {
        Color from = new Color(0, 0, 0, 90);
        Color to = new Color(0, 0, 0, 0);
        GradientPaint paint = left
            ? new GradientPaint(x, y, from, x + width, y, to)
            : new GradientPaint(x + width, y, from, x, y, to);
        graphics2d.setPaint(paint);
        graphics2d.fillRect(x, y, width, height);
    }
}

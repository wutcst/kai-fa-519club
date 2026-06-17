package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * 按方向滑入滑出的房间切换动画。
 */
public final class DirectionalRoomTransition {

    private static final int SLIDE_OUT_MS = 340;
    private static final int SLIDE_IN_MS = 400;
    private static final int TIMER_MS = 16;

    private final JPanel hostPanel;
    private final JPanel dimPanel;
    private final JLabel fromLabel;
    private final JLabel toLabel;
    private Timer timer;
    private boolean running;

    public DirectionalRoomTransition(JPanel hostPanel) {
        this.hostPanel = hostPanel;
        hostPanel.setLayout(null);
        hostPanel.setOpaque(false);
        hostPanel.setVisible(false);

        dimPanel = new JPanel();
        dimPanel.setBackground(new Color(0, 0, 0, 120));
        dimPanel.setOpaque(true);
        dimPanel.setVisible(false);

        fromLabel = createSlideLabel();
        toLabel = createSlideLabel();
        hostPanel.add(dimPanel);
        hostPanel.add(fromLabel);
        hostPanel.add(toLabel);
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 播放方向切换动画。
     *
     * @param direction 移动方向 north/south/east/west/back
     * @param fromIcon 离开房间图
     * @param width 场景宽
     * @param height 场景高
     * @param toIconProvider 切换中更新房间并返回新图
     * @param onComplete 结束回调
     */
    public void play(
        String direction,
        ImageIcon fromIcon,
        int width,
        int height,
        java.util.function.Supplier<ImageIcon> toIconProvider,
        Runnable onComplete) {
        if (running || width <= 0 || height <= 0) {
            return;
        }
        running = true;
        hostPanel.setBounds(0, 0, width, height);
        dimPanel.setBounds(0, 0, width, height);
        dimPanel.setVisible(true);
        hostPanel.setVisible(true);

        fromLabel.setVisible(true);
        fromLabel.setIcon(scaleCover(fromIcon, width, height));
        fromLabel.setBounds(0, 0, width, height);
        toLabel.setIcon(null);
        toLabel.setBounds(0, 0, width, height);

        int[] exitDelta = slideDelta(direction, width, height, true);
        animateSlide(fromLabel, 0, 0, exitDelta[0], exitDelta[1], SLIDE_OUT_MS, () -> {
            ImageIcon toIcon = toIconProvider.get();
            toLabel.setIcon(scaleCover(toIcon, width, height));
            int[] start = slideStart(direction, width, height);
            toLabel.setBounds(start[0], start[1], width, height);
            fromLabel.setVisible(false);
            animateSlide(toLabel, start[0], start[1], 0, 0, SLIDE_IN_MS, () -> {
                dimPanel.setVisible(false);
                hostPanel.setVisible(false);
                fromLabel.setVisible(true);
                running = false;
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private void animateSlide(
        JLabel label,
        int startX,
        int startY,
        int endX,
        int endY,
        int durationMs,
        Runnable onDone) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        final long startTime = System.currentTimeMillis();
        timer = new Timer(TIMER_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, (float) elapsed / durationMs);
                float eased = easeInOut(progress);
                int x = Math.round(startX + (endX - startX) * eased);
                int y = Math.round(startY + (endY - startY) * eased);
                label.setBounds(x, y, label.getWidth(), label.getHeight());
                hostPanel.repaint();
                if (progress >= 1f) {
                    timer.stop();
                    if (onDone != null) {
                        onDone.run();
                    }
                }
            }
        });
        timer.start();
    }

    private float easeInOut(float progress) {
        if (progress < 0.5f) {
            return 2f * progress * progress;
        }
        return 1f - (float) Math.pow(-2f * progress + 2f, 2) / 2f;
    }

    private int[] slideDelta(String direction, int width, int height, boolean exiting) {
        int slideX = Math.max(120, width / 4);
        int slideY = Math.max(90, height / 4);
        if ("back".equals(direction)) {
            return exiting ? new int[] {slideX, 0} : new int[] {-slideX, 0};
        }
        switch (direction == null ? "" : direction) {
            case "north":
                return exiting ? new int[] {0, slideY} : new int[] {0, -slideY};
            case "south":
                return exiting ? new int[] {0, -slideY} : new int[] {0, slideY};
            case "east":
                return exiting ? new int[] {-slideX, 0} : new int[] {slideX, 0};
            case "west":
                return exiting ? new int[] {slideX, 0} : new int[] {-slideX, 0};
            default:
                return exiting ? new int[] {0, slideY / 2} : new int[] {0, -slideY / 2};
        }
    }

    private int[] slideStart(String direction, int width, int height) {
        int[] delta = slideDelta(direction, width, height, false);
        return new int[] {-delta[0], -delta[1]};
    }

    private JLabel createSlideLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private ImageIcon scaleCover(ImageIcon icon, int width, int height) {
        if (icon == null || width <= 0 || height <= 0) {
            return icon;
        }
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}

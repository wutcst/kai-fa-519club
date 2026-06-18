package cn.edu.whut.sept.zuul.gui;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 房间切换淡入淡出动画。
 */
public final class RoomTransitionAnimator {

    private static final int FADE_OUT_MS = 280;
    private static final int FADE_IN_MS = 360;
    private static final int TIMER_MS = 16;
    private static final int MAX_ALPHA = 235;

    private final JPanel overlayPanel;
    private Timer timer;
    private boolean running;

    public RoomTransitionAnimator(JPanel overlayPanel) {
        this.overlayPanel = overlayPanel;
        overlayPanel.setOpaque(false);
        overlayPanel.setVisible(false);
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 先淡出，执行 midpoint，再淡入。
     *
     * @param midpoint 黑屏中间执行（如 go 命令）
     * @param onComplete 动画结束回调
     */
    public void playTransition(Runnable midpoint, Runnable onComplete) {
        if (running) {
            return;
        }
        running = true;
        overlayPanel.setVisible(true);
        animateAlpha(0, MAX_ALPHA, FADE_OUT_MS, () -> {
            if (midpoint != null) {
                midpoint.run();
            }
            animateAlpha(MAX_ALPHA, 0, FADE_IN_MS, () -> {
                overlayPanel.setVisible(false);
                running = false;
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private void animateAlpha(int fromAlpha, int toAlpha, int durationMs, Runnable onFinished) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        final int[] alpha = {fromAlpha};
        timer = new Timer(TIMER_MS, event -> {
            int step = (int) Math.ceil((double) Math.abs(toAlpha - fromAlpha) * TIMER_MS / durationMs);
            if (fromAlpha < toAlpha) {
                alpha[0] = Math.min(toAlpha, alpha[0] + step);
            } else {
                alpha[0] = Math.max(toAlpha, alpha[0] - step);
            }
            overlayPanel.setBackground(new Color(0, 0, 0, alpha[0]));
            overlayPanel.repaint();
            if (alpha[0] == toAlpha) {
                timer.stop();
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        });
        timer.start();
    }
}

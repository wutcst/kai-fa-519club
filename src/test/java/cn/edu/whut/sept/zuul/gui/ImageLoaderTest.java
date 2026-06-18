package cn.edu.whut.sept.zuul.gui;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ImageLoader 单元测试。
 */
public class ImageLoaderTest {

    @BeforeEach
    public void resetLoader() {
        ImageLoader.resetForTest();
    }

    @Test
    public void testSingletonPattern() {
        ImageLoader instance1 = ImageLoader.getInstance();
        ImageLoader instance2 = ImageLoader.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testRoomImageLoading() {
        ImageLoader loader = ImageLoader.getInstance();
        ImageIcon gate = loader.getRoomImage("gate");
        assertNotNull(gate);
        assertTrue(gate.getIconWidth() > 0);
    }

    @Test
    public void testItemImageLoading() {
        ImageLoader loader = ImageLoader.getInstance();
        ImageIcon money = loader.getItemImage("湿漉漉的三十元钱");
        assertNotNull(money);
        assertTrue(money.getIconWidth() > 0);
    }

    @Test
    public void testScaleCover() {
        ImageLoader loader = ImageLoader.getInstance();
        ImageIcon original = loader.getRoomImage("gate");
        ImageIcon covered = loader.scaleCover(original, 320, 180);
        assertNotNull(covered);
        assertTrue(covered.getIconWidth() >= 320 || covered.getIconHeight() >= 180);
    }
}

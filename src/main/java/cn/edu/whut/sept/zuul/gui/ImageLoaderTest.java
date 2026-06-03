/**
 * ImageLoader类单元测试
 * 新增：测试图像加载器的基本功能
 *
 * @author liujing
 * @version 2.0
 */
        package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.Test;
import javax.swing.ImageIcon;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试图像加载器功能
 */
public class ImageLoaderTest {

    @Test
    public void testSingletonPattern() {
        ImageLoader instance1 = ImageLoader.getInstance();
        ImageLoader instance2 = ImageLoader.getInstance();

        assertNotNull(instance1, "ImageLoader实例不应为null");
        assertSame(instance1, instance2, "ImageLoader应为单例模式");
    }

    @Test
    public void testBasicImageLoading() {
        ImageLoader loader = ImageLoader.getInstance();

        // 测试获取图像（即使是默认图像）
        ImageIcon icon1 = loader.getImage("room_outside");
        assertNotNull(icon1, "图像不应为null");

        ImageIcon icon2 = loader.getImage("non_existent_key");
        assertNotNull(icon2, "默认图像不应为null");
    }

    @Test
    public void testScaledImage() {
        ImageLoader loader = ImageLoader.getInstance();

        ImageIcon original = loader.getImage("room_outside");
        ImageIcon scaled = loader.getScaledImage("room_outside", 100, 100);

        assertNotNull(scaled, "缩放图像不应为null");
        assertTrue(scaled.getIconWidth() <= 100 || scaled.getIconHeight() <= 100,
                "缩放图像尺寸应正确");
    }
}
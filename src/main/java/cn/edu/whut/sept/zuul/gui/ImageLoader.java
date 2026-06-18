package cn.edu.whut.sept.zuul.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * GUI 图像加载与缓存（F7 阶段 1：assets/gui 资源）。
 */
public class ImageLoader {

    private static ImageLoader instance;
    private final Map<String, ImageIcon> imageCache;

    private ImageLoader() {
        imageCache = new HashMap<>();
    }

    /**
     * 获取单例实例。
     *
     * @return ImageLoader
     */
    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    /**
     * 测试专用：重置单例与缓存。
     */
    static synchronized void resetForTest() {
        instance = null;
    }

    /**
     * 按房间 ID 获取房间底图。
     *
     * @param roomId 房间 ID
     * @return 图像图标
     */
    public ImageIcon getRoomImage(String roomId) {
        String cacheKey = "room:" + roomId;
        return loadCached(cacheKey, AssetCatalog.roomImagePath(roomId), "room", roomId);
    }

    /**
     * 按物品名获取物品图。
     *
     * @param itemName 物品短名
     * @return 图像图标
     */
    public ImageIcon getItemImage(String itemName) {
        String slug = AssetCatalog.itemSlug(itemName);
        String cacheKey = "item:" + slug;
        return loadCached(cacheKey, AssetCatalog.itemImagePath(itemName), "item", slug);
    }

    /**
     * 按房间 ID 获取 NPC 立绘。
     *
     * @param roomId 房间 ID
     * @return 图像图标
     */
    public ImageIcon getNpcImage(String roomId) {
        String cacheKey = "npc:" + roomId;
        return loadCached(cacheKey, AssetCatalog.npcImagePathForRoom(roomId), "npc", roomId);
    }

    /**
     * 获取缩放后的图标。
     *
     * @param icon 原图标
     * @param width 宽
     * @param height 高
     * @return 缩放图标
     */
    public ImageIcon scale(ImageIcon icon, int width, int height) {
        if (icon == null) {
            return createDefaultIcon("default", "?");
        }
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * 等比放大并居中裁剪，使图像铺满目标区域。
     *
     * @param icon 原图标
     * @param targetWidth 目标宽
     * @param targetHeight 目标高
     * @return 铺满后的图标
     */
    public ImageIcon scaleCover(ImageIcon icon, int targetWidth, int targetHeight) {
        if (icon == null || targetWidth <= 0 || targetHeight <= 0) {
            return createDefaultIcon("default", "?");
        }
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        if (iconWidth <= 0 || iconHeight <= 0) {
            return createDefaultIcon("default", "?");
        }
        double scale = Math.max((double) targetWidth / iconWidth, (double) targetHeight / iconHeight);
        int scaledWidth = (int) Math.ceil(iconWidth * scale);
        int scaledHeight = (int) Math.ceil(iconHeight * scale);
        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(icon.getImage(), (targetWidth - scaledWidth) / 2, (targetHeight - scaledHeight) / 2,
            scaledWidth, scaledHeight, null);
        graphics.dispose();
        return new ImageIcon(canvas);
    }

    /**
     * 兼容旧测试：按键名获取图像。
     *
     * @param key 缓存键
     * @return 图标
     */
    public ImageIcon getImage(String key) {
        if (key != null && key.startsWith("room:")) {
            return getRoomImage(key.substring("room:".length()));
        }
        if (key != null && key.startsWith("item:")) {
            String slug = key.substring("item:".length());
            return loadCached(key, AssetCatalog.ITEMS_DIR + slug + ".png", "item", slug);
        }
        return imageCache.getOrDefault(key, createDefaultIcon("default", "?"));
    }

    /**
     * 兼容旧测试：缩放图像。
     *
     * @param key 键
     * @param width 宽
     * @param height 高
     * @return 缩放图标
     */
    public ImageIcon getScaledImage(String key, int width, int height) {
        return scale(getImage(key), width, height);
    }

    private ImageIcon loadCached(String cacheKey, String classpathPath, String type, String label) {
        ImageIcon cached = imageCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ImageIcon loaded = loadFromClasspath(classpathPath);
        if (loaded != null && loaded.getIconWidth() > 0) {
            imageCache.put(cacheKey, loaded);
            return loaded;
        }
        ImageIcon fallback = createDefaultIcon(type, abbreviate(label));
        imageCache.put(cacheKey, fallback);
        return fallback;
    }

    private ImageIcon loadFromClasspath(String classpathPath) {
        java.net.URL url = ImageLoader.class.getResource(classpathPath);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        if (icon.getIconWidth() <= 0) {
            return null;
        }
        return icon;
    }

    private String abbreviate(String label) {
        if (label == null || label.isEmpty()) {
            return "?";
        }
        return label.substring(0, 1).toUpperCase();
    }

    private ImageIcon createDefaultIcon(String type, String text) {
        Color color = Color.LIGHT_GRAY;
        if ("room".equals(type)) {
            color = new Color(200, 220, 255);
        } else if ("item".equals(type)) {
            color = new Color(255, 240, 200);
        } else if ("npc".equals(type)) {
            color = new Color(220, 255, 220);
        }
        return new ImageIcon(createColoredImage(color, text));
    }

    private Image createColoredImage(Color color, String text) {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.fillRoundRect(2, 2, size - 4, size - 4, 8, 8);
        graphics.setColor(color.darker());
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRoundRect(2, 2, size - 4, size - 4, 8, 8);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        graphics.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - 4);
        graphics.dispose();
        return image;
    }
}

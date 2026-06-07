/**
 * 该包包含World-of-Zuul文本冒险游戏的图形化界面实现类，
 * 涵盖窗口管理、界面布局、事件处理等功能模块，
 * 实现了玩家与图形界面的交互逻辑。
 *
 * @author Michael Kölling and David J. Barnes/liujing
 * @version 2.0
 */
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
 * 图像加载器类，负责管理和缓存游戏图像资源
 * 新增：提供统一的图像资源管理，支持多种房间和物品图标
 *
 * @author liujing
 * @version 2.0
 */
public class ImageLoader {
    private static ImageLoader instance;
    private Map<String, ImageIcon> imageCache;

    /**
     * 私有构造函数，实现单例模式
     */
    private ImageLoader() {
        imageCache = new HashMap<>();
        loadDefaultImages();
    }

    /**
     * 获取ImageLoader单例实例
     *
     * @return ImageLoader实例
     */
    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    /**
     * 加载默认图像资源
     */
    private void loadDefaultImages() {
        // 房间图标
        loadImage("room_outside", "images/outside.png");
        loadImage("room_theater", "images/theater.png");
        loadImage("room_pub", "images/pub.png");
        loadImage("room_lab", "images/lab.png");
        loadImage("room_office", "images/office.png");
        loadImage("room_teleport", "images/teleport.png");

        // 物品图标
        loadImage("item_key", "images/key.png");
        loadImage("item_book", "images/book.png");
        loadImage("item_cup", "images/cup.png");
        loadImage("item_computer", "images/computer.png");
        loadImage("item_map", "images/map.png");
        loadImage("item_crystal", "images/crystal.png");
        loadImage("item_cookie", "images/cookie.png");

        // 方向图标
        loadImage("dir_north", "images/north.png");
        loadImage("dir_south", "images/south.png");
        loadImage("dir_east", "images/east.png");
        loadImage("dir_west", "images/west.png");

        // 玩家图标
        loadImage("player", "images/player.png");
    }

    /**
     * 加载图像到缓存
     *
     * @param key 图像键名
     * @param path 图像路径
     */
    private void loadImage(String key, String path) {
        try {
            // 尝试多种方式加载图像
            ImageIcon icon = null;

            // 方式1: 从类路径加载
            java.net.URL imageUrl = getClass().getResource(path);
            if (imageUrl != null) {
                icon = new ImageIcon(imageUrl);
            } else {
                // 方式2: 从绝对路径加载（如果图片在项目根目录的images文件夹中）
                String projectPath = System.getProperty("user.dir");
                java.io.File imageFile = new java.io.File(projectPath + "/images/" +
                        path.substring(path.lastIndexOf("/") + 1));
                if (imageFile.exists()) {
                    icon = new ImageIcon(imageFile.getAbsolutePath());
                } else {
                    // 方式3: 从resources文件夹加载
                    imageUrl = getClass().getClassLoader().getResource(path);
                    if (imageUrl != null) {
                        icon = new ImageIcon(imageUrl);
                    }
                }
            }

            if (icon != null && icon.getIconWidth() > 0) {
                imageCache.put(key, icon);
                System.out.println("成功加载图片: " + path + " → " + key);
            } else {
                System.out.println("无法加载图片: " + path + "，创建默认图标");
                imageCache.put(key, createDefaultIcon(key));
            }
        } catch (Exception e) {
            System.out.println("加载图像失败: " + path + " - " + e.getMessage());
            imageCache.put(key, createDefaultIcon(key));
        }
    }

    /**
     * 检查图片是否已加载
     *
     * @param key 图像键名
     * @return 是否加载成功
     */
    public boolean isImageLoaded(String key) {
        ImageIcon icon = imageCache.get(key);
        return icon != null && icon.getIconWidth() > 0;
    }

    /**
     * 打印已加载的图片列表
     */
    public void printLoadedImages() {
        System.out.println("已加载的图片:");
        for (String key : imageCache.keySet()) {
            ImageIcon icon = imageCache.get(key);
            System.out.println("  " + key + ": " +
                    (icon != null ? icon.getIconWidth() + "x" + icon.getIconHeight() : "null"));
        }
    }



    /**
     * 创建默认图标
     *
     * @param key 图标键名
     * @return 默认图标
     */
    private ImageIcon createDefaultIcon(String key) {
        // 根据键名创建不同的默认图标
        Color color = Color.LIGHT_GRAY;
        String text = "?";

        if (key.startsWith("room_")) {
            color = new Color(200, 220, 255);
            text = key.substring(5, 6).toUpperCase();
        } else if (key.startsWith("item_")) {
            color = new Color(255, 240, 200);
            text = "I";
        } else if (key.startsWith("dir_")) {
            color = new Color(220, 255, 220);
            text = key.substring(4, 5).toUpperCase();
        } else if (key.equals("player")) {
            color = new Color(255, 200, 200);
            text = "P";
        }

        Image image = createColoredIcon(color, text);
        return new ImageIcon(image);
    }

    /**
     * 创建彩色图标
     *
     * @param color 背景颜色
     * @param text 图标文字
     * @return 生成的图像
     */
    private Image createColoredIcon(Color color, String text) {
        int size = 32;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        g2d.setColor(color);
        g2d.fillRoundRect(2, 2, size-4, size-4, 8, 8);

        // 绘制边框
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, size-4, size-4, 8, 8);

        // 绘制文字
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - 4);

        g2d.dispose();
        return image;
    }

    /**
     * 获取图像图标
     *
     * @param key 图像键名
     * @return 图像图标，如果不存在则返回默认图标
     */
    public ImageIcon getImage(String key) {
        return imageCache.getOrDefault(key, createDefaultIcon("default"));
    }

    /**
     * 获取缩放后的图像图标
     *
     * @param key 图像键名
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的图像图标
     */
    public ImageIcon getScaledImage(String key, int width, int height) {
        ImageIcon original = getImage(key);
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * 根据房间描述获取对应的图标
     *
     * @param roomDescription 房间描述
     * @return 房间图标
     */
    public ImageIcon getRoomIcon(String roomDescription) {
        if (roomDescription.contains("校门")) {
            return getImage("room_outside");
        } else if (roomDescription.contains("博学主楼")) {
            return getImage("room_theater");
        } else if (roomDescription.contains("博学北楼")) {
            return getImage("room_office");
        } else if (roomDescription.contains("教育超市")) {
            return getImage("room_pub");
        } else if (roomDescription.contains("寝室")) {
            return getImage("room_lab");
        } else if (roomDescription.contains("图书馆")) {
            return getImage("room_office");
        } else if (roomDescription.contains("博学东楼")) {
            return getImage("room_theater");
        } else if (roomDescription.contains("博学西楼")) {
            return getImage("room_lab");
        } else if (roomDescription.contains("体育馆")) {
            return getImage("room_teleport");
        } else if (roomDescription.contains("越苑食堂")) {
            return getImage("room_pub");
        } else if (roomDescription.contains("teleport")) {
            return getImage("room_teleport");
        } else {
            return getImage("room_outside");
        }
    }

    /**
     * 根据物品描述获取对应的图标
     *
     * @param itemDescription 物品描述
     * @return 物品图标
     */
    public ImageIcon getItemIcon(String itemDescription) {
        if (itemDescription.contains("钥匙")) {
            return getImage("item_key");
        } else if (itemDescription.contains("书")) {
            return getImage("item_book");
        } else if (itemDescription.contains("酒杯")) {
            return getImage("item_cup");
        } else if (itemDescription.contains("电脑")) {
            return getImage("item_computer");
        } else if (itemDescription.contains("地图")) {
            return getImage("item_map");
        } else if (itemDescription.contains("水晶")) {
            return getImage("item_crystal");
        } else if (itemDescription.contains("cookie")) {
            return getImage("item_cookie");
        } else {
            return getImage("item_key");
        }
    }

    /**
     * 获取方向图标
     *
     * @param direction 方向
     * @return 方向图标
     */
    public ImageIcon getDirectionIcon(String direction) {
        return getImage("dir_" + direction.toLowerCase());
    }
}
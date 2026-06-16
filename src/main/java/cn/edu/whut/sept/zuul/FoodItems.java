package cn.edu.whut.sept.zuul;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 可食用物品登记（E16 新增食物时在此补充名称）。
 */
public final class FoodItems {

    public static final String MAGIC_COOKIE = "magic cookie";
    public static final String MILK_TEA_ITEM = "半瓶奶茶";
    public static final String MILK_TEA_LEGACY_ITEM = "一杯奶茶";

    private static final Set<String> EDIBLE_NAMES = new HashSet<>(Arrays.asList(
            MAGIC_COOKIE,
            MILK_TEA_ITEM,
            MILK_TEA_LEGACY_ITEM,
            "一根火腿肠",
            "火腿肠",
            "一个辣椒包",
            "辣椒包",
            "一份外卖",
            "外卖"
    ));

    private FoodItems() {
    }

    /**
     * 判断物品名是否为可食用食物。
     */
    public static boolean isEdible(String itemName) {
        if (itemName == null) {
            return false;
        }
        String trimmed = itemName.trim();
        for (String edible : EDIBLE_NAMES) {
            if (edible.equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为魔法饼干。
     */
    public static boolean isMagicCookie(String itemName) {
        return itemName != null && MAGIC_COOKIE.equalsIgnoreCase(itemName.trim());
    }

    /**
     * 判断是否为一杯奶茶（拉肚子额外罚时）。
     */
    public static boolean isMilkTea(String itemName) {
        if (itemName == null) {
            return false;
        }
        String trimmed = itemName.trim();
        return MILK_TEA_ITEM.equalsIgnoreCase(trimmed)
            || MILK_TEA_LEGACY_ITEM.equalsIgnoreCase(trimmed);
    }

    /**
     * 从背包中查找第一件可食用物品（按背包顺序）。
     */
    public static Item findFirstEdible(Player player) {
        if (player == null) {
            return null;
        }
        for (Item item : player.getInventory()) {
            if (isEdible(item.getDescription())) {
                return item;
            }
        }
        return null;
    }
}

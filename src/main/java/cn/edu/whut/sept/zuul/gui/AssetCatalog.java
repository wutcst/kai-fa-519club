package cn.edu.whut.sept.zuul.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.SubmitCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * GUI 资源路径与物品 slug 映射（F7 阶段 1）。
 */
public final class AssetCatalog {

    public static final String ASSET_ROOT = "/assets/gui/";
    public static final String ROOMS_DIR = ASSET_ROOT + "rooms/";
    public static final String ITEMS_DIR = ASSET_ROOT + "items/";
    public static final String NPCS_DIR = ASSET_ROOT + "npcs/";

    public static final String DEFAULT_ROOM_SLUG = "_default";
    public static final String DEFAULT_ITEM_SLUG = "_default";
    public static final String DEFAULT_NPC_SLUG = "_default";

    private static final Map<String, String> ITEM_SLUGS;

    static {
        Map<String, String> map = new HashMap<>();
        registerEssentials(map);
        registerDistractions(map);
        ITEM_SLUGS = Collections.unmodifiableMap(map);
    }

    private AssetCatalog() {
    }

    /**
     * 房间底图 classpath 路径。
     *
     * @param roomId 房间 ID
     * @return 如 /assets/gui/rooms/gate.png
     */
    public static String roomImagePath(String roomId) {
        String slug = roomId == null || roomId.trim().isEmpty() ? DEFAULT_ROOM_SLUG : roomId.trim();
        return ROOMS_DIR + slug + ".png";
    }

    /**
     * 物品图 classpath 路径。
     *
     * @param itemName 游戏内物品短名
     * @return 如 /assets/gui/items/money_30yuan.png
     */
    public static String itemImagePath(String itemName) {
        return ITEMS_DIR + itemSlug(itemName) + ".png";
    }

    /**
     * NPC 立绘 classpath 路径。
     *
     * @param roomId 房间 ID
     * @return NPC 图片路径
     */
    public static String npcImagePathForRoom(String roomId) {
        if ("supermarket".equals(roomId)) {
            return NPCS_DIR + "dorm_aunt.png";
        }
        if ("boxue_north".equals(roomId) || "library".equals(roomId)) {
            return NPCS_DIR + "volunteer.png";
        }
        return NPCS_DIR + DEFAULT_NPC_SLUG + ".png";
    }

    /**
     * 解析物品资源 slug。
     *
     * @param itemName 物品短名
     * @return slug，未登记时返回 default
     */
    public static String itemSlug(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return DEFAULT_ITEM_SLUG;
        }
        String trimmed = itemName.trim();
        String slug = ITEM_SLUGS.get(trimmed);
        if (slug != null) {
            return slug;
        }
        for (Map.Entry<String, String> entry : ITEM_SLUGS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }
        return DEFAULT_ITEM_SLUG;
    }

    private static void registerEssentials(Map<String, String> map) {
        map.put(UseCommand.MONEY_ITEM, "money_30yuan");
        map.put("三十元钱", "money_30yuan");
        map.put(GatedRoom.CAMPUS_CARD_ITEM, "campus_card");
        map.put(UseCommand.DORM_FORM_ITEM, "dorm_form");
        map.put(UseCommand.HAMMER_ITEM, "hammer");
        map.put(CombineCommand.STICK_ITEM, "stick");
        map.put(CombineCommand.STONE_ITEM, "stone");
        map.put(CombineCommand.ROPE_ITEM, "rope");
        map.put("手电筒", "flashlight");
        map.put(FeedCommand.SAUSAGE_ITEM, "sausage");
        map.put("火腿肠", "sausage");
        map.put(FoodItems.MAGIC_COOKIE, "magic_cookie");
        map.put(UseCommand.STOPWATCH_ITEM, "stopwatch");
        map.put(SubmitCommand.WITHDRAWAL_SLIP_ITEM, "withdrawal_slip");
        map.put(SubmitCommand.WRONG_MEAL_CARD_ITEM, "wrong_meal_card");
        map.put(UnlockService.CANTEEN_NOTE_ITEM, "canteen_note");
        map.put(UseCommand.PROJECTOR_REMOTE_ITEM, "projector_remote");
    }

    private static void registerDistractions(Map<String, String> map) {
        map.put(UseCommand.PRAYER_PAPER_ITEM, "prayer_paper");
        map.put(FoodItems.MILK_TEA_ITEM, "milk_tea");
        map.put(FoodItems.MILK_TEA_LEGACY_ITEM, "milk_tea");
        map.put("社团传单", "club_flyer");
        map.put("一根二手数据线", "usb_cable");
        map.put("寝室省电攻略", "power_saving_guide");
        map.put("一台打开的电脑", "open_computer");
        map.put("墙上的一张A4纸", "a4_notice");
        map.put("磨损的护膝", "knee_pad");
        map.put("赛事纪念帽", "event_cap");
        map.put("一双一次性筷子", "chopsticks");
        map.put("失物招领号码牌", "lost_found_tag");
        map.put("志愿者马甲", "volunteer_vest");
        map.put("一把生锈的钥匙", "rusty_key");
        map.put("一张英语四级准考证", "cet4_ticket");
        map.put("晚安玛卡巴卡抱枕", "makabaka_pillow");
        map.put("一块电工使用的胶带", "electrical_tape");
        map.put("一本数据库概论", "database_book");
        map.put("一个水杯", "water_cup");
        map.put("一张过期的借阅条", "expired_borrow_slip");
        map.put("一份外卖", "takeout");
        map.put("一块闪光的校友纪念章", "alumni_badge");
        map.put("一块印章", "club_stamp");
        map.put("一块闪闪发光的金块", "fake_gold");
        map.put("一张猫学长的照片", "cat_photo");
        map.put("一张购物小票", "receipt");
        map.put(UseCommand.FORTUNE_SLIP_ITEM, "fortune_slip");
        map.put(UseCommand.CHILI_PACKET_ITEM, "chili_packet");
    }
}

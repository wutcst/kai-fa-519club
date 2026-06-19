package cn.edu.whut.sept.zuul;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.SubmitCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * 物品详细介绍登记（E16）：短名用于 take/use，长文案供 inspect 展示。
 */
public final class ItemCatalog {

    private static final Map<String, String> LONG_DESCRIPTIONS;

    static {
        Map<String, String> map = new HashMap<>();
        registerEssentials(map);
        registerDistractions(map);
        LONG_DESCRIPTIONS = Collections.unmodifiableMap(map);
    }

    private ItemCatalog() {
    }

    /**
     * 获取物品详细介绍；未登记时返回通用说明。
     *
     * @param itemName 物品短名
     * @return 详细介绍文本
     */
    public static String getLongDescription(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return "一件来历不明的物品。";
        }
        String trimmed = itemName.trim();
        String description = LONG_DESCRIPTIONS.get(trimmed);
        if (description != null) {
            return description;
        }
        for (Map.Entry<String, String> entry : LONG_DESCRIPTIONS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }
        return "一件普通物品，看起来与通关没有直接关系。";
    }

    /**
     * 创建带详细介绍的游戏物品。
     *
     * @param name 物品短名
     * @param weight 重量（克）
     * @return Item 实例
     */
    public static Item create(String name, int weight) {
        return new Item(name, weight, getLongDescription(name));
    }

    private static void registerEssentials(Map<String, String> map) {
        map.put(UseCommand.MONEY_ITEM,
            "一张打湿的纸币，看起来皱巴巴的。");
        map.put("三十元钱", map.get(UseCommand.MONEY_ITEM));
        map.put(GatedRoom.CAMPUS_CARD_ITEM,
            "在黑暗中似乎格外重要，借出请记得归还。");
        map.put(UseCommand.DORM_FORM_ITEM,
            "加盖公章的归寝单，寝室门口须提交后才能入内睡觉。");
        map.put(UseCommand.HAMMER_ITEM,
            "用棍子、石头和绳子临时绑成的破门锤，足够砸开西楼锈蚀的门锁。");
        map.put(CombineCommand.STICK_ITEM, "或许是某把拖把的一部分。");
        map.put(CombineCommand.STONE_ITEM, "造型很独特，一块坚硬的石头。");
        map.put(CombineCommand.ROPE_ITEM, "粗糙的绳子，或许是拔河比赛的遗物。");
        map.put(DarkRoom.FLASHLIGHT_ITEM,
            "体育馆器材柜借出的手电，贴条写借出请还——反正没人还。"
                + "第三关起穿过断电博学主楼必备。");
        map.put(FeedCommand.SAUSAGE_ITEM, "越苑秘制火腿肠，猫学长闻了都回头。");
        map.put(FeedCommand.MAGIC_COOKIE_ITEM,
            "猫学长回馈的魔法饼干，食用可短暂增加负重并争取额外熄灯前时间。");
        map.put(FoodItems.MAGIC_COOKIE, map.get(FeedCommand.MAGIC_COOKIE_ITEM));
        map.put(UseCommand.STOPWATCH_ITEM,
            "秒表背面刻着「别浪费每一秒」。按 use 可查看当前剩余熄灯时间。");
        map.put(UseCommand.PROJECTOR_REMOTE_ITEM,
            "打开投影仪看看？");
        map.put(SubmitCommand.WITHDRAWAL_SLIP_ITEM,
            "办理退宿手续时留下的条子，不是本关需要的归寝单。");
        map.put(SubmitCommand.WRONG_MEAL_CARD_ITEM,
            "别人的饭卡，刷卡机一定会拒绝。");
        map.put(UnlockService.CANTEEN_NOTE_ITEM,
            "生日快乐！--2026年6月1日");
    }

    private static void registerDistractions(Map<String, String> map) {
        map.put(UseCommand.PRAYER_PAPER_ITEM,
            "黄纸上印着「极限=希望」，看起来像能加考运，与回寝无关。");
        map.put(FoodItems.MILK_TEA_ITEM,
            "看起来好像很好喝，要不要试试呢？");
        map.put(FoodItems.MILK_TEA_LEGACY_ITEM, map.get(FoodItems.MILK_TEA_ITEM));
        map.put("社团传单", "周五操场草坪趴，期待你的到来！");
        map.put("一根二手数据线", "看起来似乎有些年头，不知能不能通电。");
        map.put("寝室省电攻略", "封面写关灯等于功德，翻开只有一句冷笑话。");
        map.put("一台打开的电脑", "这不是你的电脑吗？怎么会遗忘在这里！");
        map.put("墙上的一张A4纸", "关灯等于功德，请随手关灯！");
        map.put("磨损的护膝", "在奔跑和运动时保护你的膝盖。");
        map.put("赛事纪念帽", "戴上它，你就是冠军！");
        map.put("一双一次性筷子", "没有它，你将寸饭难行。");
        map.put("失物招领号码牌", "编号521");
        map.put("志愿者马甲", "穿上试试吧，说不定可以伪装成志愿者！");
        map.put("一把生锈的钥匙", "或许能打开某扇大门。");
        map.put("一张英语四级准考证", "看来有人要提前知道四级成绩了。");
        map.put("晚安玛卡巴卡抱枕", "柔软舒服，抱着更想睡觉");
        map.put("一块电工使用的胶带", "你不会想用它来修灯泡吧？");
        map.put("一本数据库概论", "带你领略数据库的秘密。");
        map.put("一个水杯", "或许是某个急着回寝室的人留下的。");
        map.put("一张过期的借阅条", "借阅于2026年6月1日。");
        map.put("一份外卖", "不要加香菜谢谢！");
        map.put("一块闪光的校友纪念章", "武汉理工大学93届校友制。");
        map.put("一块印章", "或许能用来伪造归寝单？");
        map.put("一块闪闪发光的金块", "或许能用来购买一卡通？");
        map.put("一张猫学长的照片", "本喵是不是很帅？");
        map.put("一张购物小票", "一张一卡通花费30元。");
        map.put(UseCommand.FORTUNE_SLIP_ITEM, "这次你一定能去想去的地方！");
        map.put(UseCommand.CHILI_PACKET_ITEM, "没有辣椒的重庆小面是不完美的！");
        map.put("一根火腿肠", map.get(FeedCommand.SAUSAGE_ITEM));
    }
}

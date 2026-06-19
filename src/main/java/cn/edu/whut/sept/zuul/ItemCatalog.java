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
            "被雨水打湿的三张十元纸币，皱巴巴地粘在一起。"
                + "教育超市宿管阿姨应该愿意帮你换成一卡通。");
        map.put("三十元钱", map.get(UseCommand.MONEY_ITEM));
        map.put(GatedRoom.CAMPUS_CARD_ITEM,
            "武汉理工大学校园一卡通，刷卡可进图书馆与寝室。"
                + "熄灯后回寝的必备凭证。");
        map.put(UseCommand.DORM_FORM_ITEM,
            "加盖公章的归寝单，寝室门口须提交后才能入内睡觉。");
        map.put(UseCommand.HAMMER_ITEM,
            "用棍子、石头和绳子临时绑成的破门锤，足够砸开西楼锈蚀的门锁。");
        map.put(CombineCommand.STICK_ITEM, "一根结实的木棍，西楼角落里捡来的。");
        map.put(CombineCommand.STONE_ITEM, "半块砖大小的石头，棱角锋利。");
        map.put(CombineCommand.ROPE_ITEM, "一截尼龙绳，勉强能用来捆绑。");
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
            "西楼教室的投影仪遥控器，按钮大多已经失灵。");
        map.put(SubmitCommand.WITHDRAWAL_SLIP_ITEM,
            "办理退宿手续时留下的条子，不是本关需要的归寝单。");
        map.put(SubmitCommand.WRONG_MEAL_CARD_ITEM,
            "别人的饭卡，刷卡机一定会拒绝。");
        map.put(UnlockService.CANTEEN_NOTE_ITEM,
            "食堂拾到的纸条，上面写着「2026.06.01」——像是某个干扰密码。");
    }

    private static void registerDistractions(Map<String, String> map) {
        map.put(UseCommand.PRAYER_PAPER_ITEM,
            "黄纸上印着「极限=希望」，看起来像能加考运，与回寝无关。");
        map.put(FoodItems.MILK_TEA_ITEM,
            "标签写学长请的凉了也能喝，香甜诱人，喝多了可能会拉肚子耽误时间。");
        map.put(FoodItems.MILK_TEA_LEGACY_ITEM, map.get(FoodItems.MILK_TEA_ITEM));
        map.put("社团传单", "街舞社周五草坪吉他夜招新，纯剧情，与通关无关。");
        map.put("一根二手数据线", "接头像 Type-C，实则是命运玩笑，开不了任何门。");
        map.put("寝室省电攻略", "封面写关灯等于功德，翻开只有一句冷笑话。");
        map.put("一台打开的电脑", "屏幕亮着未提交的实验报告，诱人却沉重。");
        map.put("墙上的一张A4纸", "寝室楼道通知，提醒保持安静，没有新线索。");
        map.put("磨损的护膝", "看起来护膝，实际护寂寞。");
        map.put("赛事纪念帽", "戴上像冠军，交到寝室门口闸机可不认。");
        map.put("一双一次性筷子", "食堂顺走的一次性筷子，毫无用处。");
        map.put("失物招领号码牌", "编号像爱情，不是门禁密码。");
        map.put("志愿者马甲", "穿上像志愿者，闸机不会因此放行。");
        map.put("一把生锈的钥匙", "锈迹斑斑，西楼那扇坏锁不认它。");
        map.put("一张英语四级准考证", "证件上的姓名不是你。");
        map.put("晚安玛卡巴卡抱枕", "柔软舒服，抱着更想睡觉——但现在还不能睡。");
        map.put("一块电工使用的胶带", "像能修灯，修不了 deadline。");
        map.put("一本数据库概论", "厚到能防身，与通关无关。");
        map.put("一个水杯", "占座水杯，贴条写主人马上回。");
        map.put("一张过期的借阅条", "日期写着2026.06.01，与食堂纸条呼应，不是密码。");
        map.put("一份外卖", "校门旁未取走的外卖，条子写放南门人在北门。");
        map.put("一块闪光的校友纪念章", "闪闪发光，闸机无感。");
        map.put("一块印章", "社团印章，盖了也不能当通行证。");
        map.put("一块闪闪发光的金块", "喷漆塑料，超市不收。");
        map.put("一张猫学长的照片", "ins 风萌照，不能当智能锁密码。");
        map.put("一张购物小票", "金额三十，提醒你去换卡不是输密码。");
        map.put(UseCommand.FORTUNE_SLIP_ITEM, "写着「吉」，对传送和智能锁都没有加成。");
        map.put(UseCommand.CHILI_PACKET_ITEM, "食堂免费辣椒，use 或 eat 都会耽误时间。");
        map.put("一根火腿肠", map.get(FeedCommand.SAUSAGE_ITEM));
    }
}

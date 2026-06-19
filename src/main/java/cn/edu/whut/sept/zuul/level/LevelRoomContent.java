package cn.edu.whut.sept.zuul.level;

import java.util.Random;

import cn.edu.whut.sept.zuul.DarkRoom;
import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.ItemCatalog;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.FeedCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

/**
 * E16：按关卡刷新房间公告与物品。
 * <p>
 * 策划文档中干扰物与公告均按「相对上一关新增/修改」书写（只写本关变动）。
 * startLevel 清空物品后，公告与干扰物均按 1..当前关 回放各关配置，合成该关完整地图状态。
 * 通关线索物（钱、火腿肠、三材等）同样按关卡进度累积投放。
 */
public final class LevelRoomContent {

    private static final String[] MAGIC_COOKIE_ROOM_IDS = {
        "boxue_main", "boxue_east", "canteen"
    };

    private LevelRoomContent() {
    }

    /**
     * 按关卡应用公告与物品配置。
     *
     * @param game 游戏实例
     * @param config 当前关卡配置
     */
    public static void apply(Game game, LevelConfig config) {
        if (game == null || config == null) {
            return;
        }
        clearAllRoomItems(game);
        applyDefaultBulletins(game);
        int level = config.getLevelNumber();
        applyCumulativeBulletinPatches(game, level);
        appendMissionToGate(game, config);
        applyEssentialItems(game, level);
        applyCumulativeDistractionItems(game, level);
    }

    private static void clearAllRoomItems(Game game) {
        for (Room room : game.getRoomRegistry().values()) {
            room.clearItems();
        }
    }

    /** 总体公告（各关共用底稿）。 */
    private static void applyDefaultBulletins(Game game) {
        setBulletin(game, "gate", "晚上11点后请凭一卡通与归寝单回寝。");
        setBulletin(game, "boxue_main", "请努力拿到一卡通回寝吧！");
        setBulletin(game, "boxue_north", "温馨提示：志愿者服务台位于二楼转角处。");
        setBulletin(game, "supermarket", "日用品售卖。宿管阿姨：有钱就能办卡。");
        setBulletin(game, "dormitory", "进门请刷一卡通。");
        setBulletin(game, "library", "请刷校园卡入内。进入后请查看电子公告。");
        setBulletin(game, "boxue_east", "欢迎来到博学东楼。");
        setBulletin(game, "boxue_west", "请解开谜题。");
        setBulletin(game, "gymnasium", "因羽毛球社训练，占用一天。");
        setBulletin(game, "canteen", "今日火腿肠特价。");
    }

    /**
     * 累积套用 1..当前关 的公告 patch。
     * 策划文案只写本关改动，未列出的房间保持先前关卡状态。
     */
    private static void applyCumulativeBulletinPatches(Game game, int currentLevel) {
        for (int level = 1; level <= currentLevel; level++) {
            applyBulletinPatchForLevel(game, level);
        }
    }

    /** 单关公告 patch（仅覆盖本关在策划中写明有改动的房间）。 */
    private static void applyBulletinPatchForLevel(Game game, int level) {
        switch (level) {
            case 1:
                setBulletin(game, "gate", "晚上11点后请凭一卡通回寝。");
                setBulletin(game, "boxue_main", "请努力拿到一卡通回寝吧！");
                setBulletin(game, "boxue_north",
                    "温馨提示：志愿者服务台位于二楼转角处。"
                    + "\n【线索】志愿者台：本关只需一卡通，归寝单下周才查。");
                break;
            case 2:
                setBulletin(game, "gate",
                    "晚上11点后请凭一卡通与归寝单回寝。"
                    + "本关增开体育馆、越苑食堂，请尽情探索吧！");
                setBulletin(game, "gymnasium",
                    "今晚羽毛球社训练，手电在左手器材柜。"
                    + "\n【线索】贴条：借出请还——反正没人还。");
                setBulletin(game, "canteen",
                    "火腿肠特价。纸条别当真，那是同学占座。");
                setBulletin(game, "boxue_north",
                    "温馨提示：志愿者服务台位于二楼转角处。"
                    + "\n【线索】志愿者：请在此处对话领取归寝单。");
                break;
            case 3:
                setBulletin(game, "gate",
                    "晚上11点后请凭一卡通与归寝单回寝。"
                    + "本关增开博学东楼、博学西楼，请尽情探索吧！");
                setBulletin(game, "boxue_main",
                    "因电路老化维修，本楼今日停电一天。"
                    + "\n【线索】停电通知：手电在体育馆。");
                setBulletin(game, "boxue_west",
                    "请解开谜题。\n【线索】海报：请先合成锤子。");
                break;
            case 4:
                setBulletin(game, "gate", "晚上11点后请凭一卡通与归寝单回寝。");
                setBulletin(game, "boxue_main", "因电路老化维修，本楼今日停电一天。");
                setBulletin(game, "boxue_north",
                    "温馨提示：志愿者服务台位于二楼转角处。"
                    + "\n【线索】志愿者：请在图书馆刷卡领取归寝单。"
                    + "\n【线索】猫学长：喵喵喵，有没有火腿肠？（喂食有惊喜）");
                setBulletin(game, "library",
                    "请刷校园卡入内，查看电子公告屏。"
                    + "\n源于1898年创办的湖北工艺学堂，于2000年5月27日由原武汉工业大学、"
                    + "武汉交通科技大学、武汉汽车工业大学合并组建而成，是教育部直属、"
                    + "国家「211工程」建设的全国重点大学。");
                break;
            case 5:
                setBulletin(game, "gate",
                    "除一卡通与归寝单外，寝室门锁好像升级了，需八位密码。"
                    + "请尽情探索全图！");
                setBulletin(game, "boxue_main", "请努力拿到一卡通与归寝单回寝吧！本关主楼供电正常。");
                setBulletin(game, "dormitory",
                    "智能锁：请输入八位离校验证码（与校史相关，别用食堂纸条生日）。");
                setBulletin(game, "gymnasium", "进入后可能传送到校园任意角落（寝室与图书馆除外）。");
                break;
            default:
                break;
        }
    }

    private static void appendMissionToGate(Game game, LevelConfig config) {
        Room gate = game.getRoomById("gate");
        if (gate != null && gate.getBulletin() != null && !gate.getBulletin().isEmpty()) {
            gate.setBulletin(gate.getBulletin() + "\n【本关任务】" + config.getMissionHint());
        }
    }

    /** 通关线索物：按关卡进度累积投放（策划「新增线索」未每关重列，但各关仍需可拾取）。 */
    private static void applyEssentialItems(Game game, int level) {
        if (level >= 1) {
            addItem(game, "boxue_north", UseCommand.MONEY_ITEM, 10);
        }
        if (level >= 2) {
            addItem(game, "gymnasium", DarkRoom.FLASHLIGHT_ITEM, 200);
            addItem(game, "canteen", FeedCommand.SAUSAGE_ITEM, 80);
            addItem(game, "canteen", UnlockService.CANTEEN_NOTE_ITEM, 5);
        }
        if (level >= 3) {
            addItem(game, "boxue_west", CombineCommand.STICK_ITEM, 500);
            addItem(game, "boxue_west", CombineCommand.STONE_ITEM, 800);
            addItem(game, "boxue_west", CombineCommand.ROPE_ITEM, 300);
        }
    }

    /** 累积投放 1..当前关 各关新增的干扰物。 */
    private static void applyCumulativeDistractionItems(Game game, int currentLevel) {
        for (int level = 1; level <= currentLevel; level++) {
            applyDistractionItemsForLevel(game, level);
        }
    }

    /** 单关干扰物（策划「相比上一关增加」条目）。 */
    private static void applyDistractionItemsForLevel(Game game, int level) {
        switch (level) {
            case 1:
                addItem(game, "boxue_main", UseCommand.PRAYER_PAPER_ITEM, 30);
                addItem(game, "boxue_main", FoodItems.MILK_TEA_ITEM, 100);
                addItem(game, "boxue_main", "社团传单", 30);
                addItem(game, "boxue_north", "一根二手数据线", 80);
                addItem(game, "boxue_north", "寝室省电攻略", 20);
                addItem(game, "boxue_north", "一台打开的电脑", 2500);
                addItem(game, "dormitory", "墙上的一张A4纸", 10);
                break;
            case 2:
                addItem(game, "gymnasium", "磨损的护膝", 40);
                addItem(game, "gymnasium", "赛事纪念帽", 50);
                addItem(game, "canteen", "一双一次性筷子", 5);
                addItem(game, "boxue_main", "失物招领号码牌", 15);
                addItem(game, "boxue_north", "志愿者马甲", 200);
                break;
            case 3:
                addItem(game, "boxue_west", "一把生锈的钥匙", 50);
                addItem(game, "boxue_west", UseCommand.PROJECTOR_REMOTE_ITEM, 30);
                addItem(game, "boxue_west", "一张英语四级准考证", 10);
                addItem(game, "boxue_north", "晚安玛卡巴卡抱枕", 400);
                placeMagicCookie(game);
                break;
            case 4:
                addItem(game, "boxue_main", "一块电工使用的胶带", 100);
                addItem(game, "gymnasium", UseCommand.STOPWATCH_ITEM, 50);
                addItem(game, "library", "一本数据库概论", 600);
                addItem(game, "library", "一个水杯", 300);
                addItem(game, "library", "一张过期的借阅条", 5);
                break;
            case 5:
                addItem(game, "gate", "一份外卖", 80);
                addItem(game, "boxue_main", "一块闪光的校友纪念章", 60);
                addItem(game, "boxue_east", "一块印章", 120);
                addItem(game, "boxue_west", "一块闪闪发光的金块", 500);
                addItem(game, "boxue_north", "一张猫学长的照片", 20);
                addItem(game, "supermarket", "一张购物小票", 5);
                addItem(game, "gymnasium", UseCommand.FORTUNE_SLIP_ITEM, 5);
                addItem(game, "canteen", UseCommand.CHILI_PACKET_ITEM, 30);
                break;
            default:
                break;
        }
    }

    /** 第三关起在博学主楼 / 博学东楼 / 越苑食堂中随机投放一块地图饼干。 */
    private static void placeMagicCookie(Game game) {
        Random random = new Random();
        String roomId = MAGIC_COOKIE_ROOM_IDS[random.nextInt(MAGIC_COOKIE_ROOM_IDS.length)];
        addItem(game, roomId, FoodItems.MAGIC_COOKIE, 100);
    }

    private static void setBulletin(Game game, String roomId, String bulletin) {
        Room room = game.getRoomById(roomId);
        if (room != null) {
            room.setBulletin(bulletin);
        }
    }

    private static void addItem(Game game, String roomId, String name, int weight) {
        Room room = game.getRoomById(roomId);
        if (room != null) {
            room.addItem(ItemCatalog.create(name, weight));
        }
    }
}

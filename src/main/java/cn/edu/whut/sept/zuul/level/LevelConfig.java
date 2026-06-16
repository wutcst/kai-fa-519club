package cn.edu.whut.sept.zuul.level;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 单关配置：限时、起点、开放房间、主楼断电与任务提示。
 */
public final class LevelConfig {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;

    /** 未开放出口提示 */
    public static final String LOCKED_EXIT_MESSAGE = "夜色中，这个方向暂未开放。";

    private static final int[] TIME_LIMIT_SECONDS = {240, 300, 420, 540, 720};
    private static final String[] LEVEL_TITLES = {
        "第一关：初入校园",
        "第二关：归寝凭证",
        "第三关：西楼迷局",
        "第四关：博学暗夜",
        "第五关：终夜归寝"
    };
    private static final String[] LEVEL_MISSIONS = {
        "本关目标：拾取三十元，超市换一卡通，回寝室睡觉。",
        "本关目标：北楼对话领归寝单、超市换一卡通并提交，可探索体育馆与食堂。",
        "本关目标：体育馆取手电闯黑暗主楼，西楼合成锤子，持双证回寝。",
        "本关目标：换卡后进图书馆领归寝单，可选喂猫，回寝睡觉。",
        "本关目标：全图探索，读馆公告推智能锁密码，解锁后睡觉通关。"
    };

    private static final String[][] UNLOCKED_ROOM_IDS = {
        {"gate", "boxue_main", "boxue_north", "supermarket", "dormitory"},
        {"gate", "boxue_main", "boxue_north", "supermarket", "dormitory", "gymnasium", "canteen"},
        {"gate", "boxue_main", "boxue_north", "supermarket", "dormitory", "gymnasium", "canteen",
            "boxue_west", "boxue_east"},
        {"gate", "boxue_main", "boxue_north", "supermarket", "dormitory", "gymnasium", "canteen",
            "boxue_west", "boxue_east", "library"},
        {"gate", "boxue_main", "boxue_north", "supermarket", "dormitory", "gymnasium", "canteen",
            "boxue_west", "boxue_east", "library"}
    };

    private final int levelNumber;
    private final int timeLimitSeconds;
    private final String startRoomId;
    private final String title;
    private final Set<String> unlockedRoomIds;

    private LevelConfig(int levelNumber) {
        this.levelNumber = levelNumber;
        this.timeLimitSeconds = TIME_LIMIT_SECONDS[levelNumber - 1];
        this.startRoomId = "gate";
        this.title = LEVEL_TITLES[levelNumber - 1];
        this.unlockedRoomIds = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(UNLOCKED_ROOM_IDS[levelNumber - 1]))
        );
    }

    public static LevelConfig forLevel(int levelNumber) {
        if (levelNumber < MIN_LEVEL || levelNumber > MAX_LEVEL) {
            throw new IllegalArgumentException("关卡号必须在 " + MIN_LEVEL + "—" + MAX_LEVEL + " 之间");
        }
        return new LevelConfig(levelNumber);
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public String getStartRoomId() {
        return startRoomId;
    }

    public String getTitle() {
        return title;
    }

    public String getMissionHint() {
        return LEVEL_MISSIONS[levelNumber - 1];
    }

    public boolean isRoomUnlocked(String roomId) {
        if (roomId == null) {
            return true;
        }
        return unlockedRoomIds.contains(roomId);
    }

    public boolean requiresDormitorySubmit() {
        return levelNumber >= 2;
    }

    /** 第三、四关博学主楼断电，须手电筒。 */
    public boolean isMainBuildingDark() {
        return levelNumber == 3 || levelNumber == 4;
    }
}

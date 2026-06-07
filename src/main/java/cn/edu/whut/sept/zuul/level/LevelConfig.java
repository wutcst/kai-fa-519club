package cn.edu.whut.sept.zuul.level;

/**
 * 单关配置：限时、起点房间等。地图拓扑在 Game 中一次构建，关卡差异由此类描述。
 * E15 将在此基础上扩展各关可用房间集合。
 */
public final class LevelConfig {

    /** 最低关卡号 */
    public static final int MIN_LEVEL = 1;
    /** 最高关卡号 */
    public static final int MAX_LEVEL = 5;

    private static final int[] TIME_LIMIT_SECONDS = {240, 300, 420, 540, 720};
    private static final String[] LEVEL_TITLES = {
        "第一关：初入校园",
        "第二关：归寝凭证",
        "第三关：西楼迷局",
        "第四关：博学暗夜",
        "第五关：终夜归寝"
    };

    private final int levelNumber;
    private final int timeLimitSeconds;
    private final String startRoomId;
    private final String title;

    private LevelConfig(int levelNumber, int timeLimitSeconds, String startRoomId, String title) {
        this.levelNumber = levelNumber;
        this.timeLimitSeconds = timeLimitSeconds;
        this.startRoomId = startRoomId;
        this.title = title;
    }

    /**
     * 获取指定关卡的配置。
     *
     * @param levelNumber 关卡号（1—5）
     * @return 关卡配置
     */
    public static LevelConfig forLevel(int levelNumber) {
        if (levelNumber < MIN_LEVEL || levelNumber > MAX_LEVEL) {
            throw new IllegalArgumentException("关卡号必须在 " + MIN_LEVEL + "—" + MAX_LEVEL + " 之间");
        }
        return new LevelConfig(
            levelNumber,
            TIME_LIMIT_SECONDS[levelNumber - 1],
            "gate",
            LEVEL_TITLES[levelNumber - 1]
        );
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

    /**
     * 当前关进入寝室是否须先完成归寝单提交（第 2 关起）。
     *
     * @return 需要 submit 返回 true
     */
    public boolean requiresDormitorySubmit() {
        return levelNumber >= 2;
    }
}

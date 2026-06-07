package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.level.LevelConfig;

/**
 * 条件门房间（图书馆、寝室等）：须持一卡通；寝室在部分关卡须先完成归寝单提交。
 */
public class GatedRoom extends Room {

    /** 准入所需一卡通名称 */
    public static final String CAMPUS_CARD_ITEM = "一卡通";

    /** 无一卡通时的提示文案 */
    public static final String CARD_DENIED_MESSAGE = "请刷卡";

    /** 寝室须先提交归寝单时的提示文案（配合 E19 submit） */
    public static final String SUBMIT_REQUIRED_MESSAGE = "请先提交归寝单";

    /**
     * 条件门类型。
     */
    public enum AccessRule {
        /** 图书馆：仅须一卡通 */
        LIBRARY,
        /** 寝室：须一卡通，且当关配置要求时须已 submit */
        DORMITORY
    }

    private final AccessRule accessRule;

    private GatedRoom(String description, AccessRule accessRule) {
        super(description);
        this.accessRule = accessRule;
    }

    /**
     * 创建图书馆条件门。
     *
     * @param description 房间描述
     * @return 图书馆房间
     */
    public static GatedRoom library(String description) {
        return new GatedRoom(description, AccessRule.LIBRARY);
    }

    /**
     * 创建寝室条件门。
     *
     * @param description 房间描述
     * @return 寝室房间
     */
    public static GatedRoom dormitory(String description) {
        return new GatedRoom(description, AccessRule.DORMITORY);
    }

    public AccessRule getAccessRule() {
        return accessRule;
    }

    /**
     * 判断是否允许进入；不允许时返回对应提示文案，允许时返回 null。
     *
     * @param game 游戏上下文（读取玩家背包与当关配置）
     * @return 拒绝原因文案，允许进入则为 null
     */
    public String getDenialMessage(Game game) {
        if (game == null || game.getPlayer() == null) {
            return CARD_DENIED_MESSAGE;
        }
        if (!hasCampusCard(game.getPlayer())) {
            return CARD_DENIED_MESSAGE;
        }
        if (accessRule == AccessRule.DORMITORY && requiresSubmitForCurrentLevel(game)) {
            if (!game.getLevelManager().isDormitorySubmitCompleted()) {
                return SUBMIT_REQUIRED_MESSAGE;
            }
        }
        return null;
    }

    /**
     * 是否满足进入条件。
     *
     * @param game 游戏上下文
     * @return 允许进入返回 true
     */
    public boolean canEnter(Game game) {
        return getDenialMessage(game) == null;
    }

    private boolean hasCampusCard(Player player) {
        return player.findItemInInventory(CAMPUS_CARD_ITEM) != null;
    }

    private boolean requiresSubmitForCurrentLevel(Game game) {
        LevelConfig config = game.getLevelManager().getCurrentLevelConfig();
        return config != null && config.requiresDormitorySubmit();
    }
}

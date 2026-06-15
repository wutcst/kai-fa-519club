package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.level.LevelConfig;

/**
 * 条件门房间（图书馆、寝室等）：须持一卡通；寝室在部分关卡须先完成归寝单提交。
 */
public class GatedRoom extends Room {

    public static final String CAMPUS_CARD_ITEM = "一卡通";
    public static final String CARD_DENIED_MESSAGE = "请刷一卡通";
    public static final String LIBRARY_CARD_DENIED_MESSAGE = "未拿到一卡通，无法进入图书馆。";
    public static final String SUBMIT_REQUIRED_MESSAGE = "请先提交归寝单";

    public enum AccessRule {
        LIBRARY,
        DORMITORY
    }

    private final AccessRule accessRule;

    private GatedRoom(String roomId, String description, String bulletin, AccessRule accessRule) {
        super(roomId, description, bulletin);
        this.accessRule = accessRule;
    }

    public static GatedRoom library() {
        return new GatedRoom(
            "library",
            "图书馆",
            "需刷卡入内，请查看电子公告屏。",
            AccessRule.LIBRARY
        );
    }

    public static GatedRoom dormitory() {
        return new GatedRoom(
            "dormitory",
            "寝室",
            "楼道提醒：熄灯后请轻脚步，床位在走廊尽头。",
            AccessRule.DORMITORY
        );
    }

    public AccessRule getAccessRule() {
        return accessRule;
    }

    public String getDenialMessage(Game game) {
        if (game == null || game.getPlayer() == null) {
            return CARD_DENIED_MESSAGE;
        }
        if (!hasCampusCard(game.getPlayer())) {
            if (accessRule == AccessRule.LIBRARY) {
                return LIBRARY_CARD_DENIED_MESSAGE;
            }
            return CARD_DENIED_MESSAGE;
        }
        if (accessRule == AccessRule.DORMITORY && requiresSubmitForCurrentLevel(game)) {
            if (!game.getLevelManager().isDormitorySubmitCompleted()) {
                return SUBMIT_REQUIRED_MESSAGE;
            }
        }
        return null;
    }

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

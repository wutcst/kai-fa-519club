package cn.edu.whut.sept.zuul.multiplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Player;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.level.LevelState;

/**
 * 联机房间全局状态快照（关卡、计时、各玩家位置）。
 */
public class GameStateSnapshot {

    private final int level;
    private final String levelState;
    private final int remainingSeconds;
    private final String timerText;
    private final String activePlayerId;
    private final String roomId;
    private final String roomDescription;
    private final List<PlayerStateSnapshot> players;

    public GameStateSnapshot(int level, String levelState, int remainingSeconds, String timerText,
                             String activePlayerId, String roomId, String roomDescription,
                             List<PlayerStateSnapshot> players) {
        this.level = level;
        this.levelState = levelState;
        this.remainingSeconds = remainingSeconds;
        this.timerText = timerText;
        this.activePlayerId = activePlayerId;
        this.roomId = roomId;
        this.roomDescription = roomDescription;
        this.players = players == null ? new ArrayList<>() : new ArrayList<>(players);
    }

    public static GameStateSnapshot from(Game game, String requestingPlayerId) {
        if (requestingPlayerId != null) {
            game.setActiveOnlinePlayer(requestingPlayerId);
        }
        Room room = game.getCurrentRoom();
        List<PlayerStateSnapshot> playerStates = new ArrayList<>();
        for (Map.Entry<String, Player> entry : game.getOnlinePlayers().entrySet()) {
            playerStates.add(PlayerStateSnapshot.from(entry.getKey(), entry.getValue()));
        }
        return new GameStateSnapshot(
            game.getLevelManager().getCurrentLevel(),
            game.getLevelManager().getState().name(),
            game.getLevelTimer().getRemainingSeconds(),
            game.getLevelTimer().getDisplayText(),
            game.getActiveOnlinePlayerId(),
            room == null ? "" : room.getRoomId(),
            room == null ? "" : room.getShortDescription(),
            playerStates
        );
    }

    /**
     * 从 REST API 返回的 JSON Map 解析状态（客户端用）。
     */
    @SuppressWarnings("unchecked")
    public static GameStateSnapshot fromApiMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        List<PlayerStateSnapshot> playerStates = new ArrayList<>();
        Object playersObj = map.get("players");
        if (playersObj instanceof List) {
            for (Object entry : (List<?>) playersObj) {
                if (entry instanceof Map) {
                    playerStates.add(PlayerStateSnapshot.fromApiMap((Map<String, Object>) entry));
                }
            }
        }
        Number level = (Number) map.get("level");
        Number remaining = (Number) map.get("remainingSeconds");
        return new GameStateSnapshot(
            level == null ? 1 : level.intValue(),
            String.valueOf(map.getOrDefault("levelState", "IN_PROGRESS")),
            remaining == null ? 0 : remaining.intValue(),
            String.valueOf(map.getOrDefault("timerText", "")),
            map.get("activePlayerId") == null ? null : String.valueOf(map.get("activePlayerId")),
            String.valueOf(map.getOrDefault("roomId", "")),
            String.valueOf(map.getOrDefault("roomDescription", "")),
            playerStates
        );
    }

    public int getLevel() {
        return level;
    }

    public String getLevelState() {
        return levelState;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getTimerText() {
        return timerText;
    }

    public String getActivePlayerId() {
        return activePlayerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public List<PlayerStateSnapshot> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isLevelInProgress() {
        return LevelState.IN_PROGRESS.name().equals(levelState);
    }
}

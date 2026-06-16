package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.multiplayer.GameStateSnapshot;
import cn.edu.whut.sept.zuul.multiplayer.PlayerStateSnapshot;

/**
 * 联机游戏状态 DTO。
 */
public class GameStateDto {

    private int level;
    private String levelState;
    private int remainingSeconds;
    private String timerText;
    private String activePlayerId;
    private String roomId;
    private String roomDescription;
    private List<PlayerStateDto> players = new ArrayList<>();

    public static GameStateDto from(GameStateSnapshot snapshot) {
        GameStateDto dto = new GameStateDto();
        dto.level = snapshot.getLevel();
        dto.levelState = snapshot.getLevelState();
        dto.remainingSeconds = snapshot.getRemainingSeconds();
        dto.timerText = snapshot.getTimerText();
        dto.activePlayerId = snapshot.getActivePlayerId();
        dto.roomId = snapshot.getRoomId();
        dto.roomDescription = snapshot.getRoomDescription();
        for (PlayerStateSnapshot player : snapshot.getPlayers()) {
            dto.players.add(PlayerStateDto.from(player));
        }
        return dto;
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

    public List<PlayerStateDto> getPlayers() {
        return players;
    }
}

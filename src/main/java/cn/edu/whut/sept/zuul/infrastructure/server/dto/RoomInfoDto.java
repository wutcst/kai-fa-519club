package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import cn.edu.whut.sept.zuul.multiplayer.GameRoom;

/**
 * 联机房间摘要 DTO。
 */
public class RoomInfoDto {

    private String roomId;
    private String roomName;
    private int playerCount;
    private int level;
    private int remainingSeconds;
    private String hostPlayerId;
    private boolean inGame;

    public static RoomInfoDto from(GameRoom room) {
        RoomInfoDto dto = new RoomInfoDto();
        dto.roomId = room.getRoomId();
        dto.roomName = room.getRoomName();
        dto.playerCount = room.getPlayerCount();
        dto.level = room.getGame().getLevelManager().getCurrentLevel();
        dto.remainingSeconds = room.getGame().getLevelTimer().getRemainingSeconds();
        dto.hostPlayerId = room.getHostPlayerId();
        dto.inGame = room.isInGame();
        return dto;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getLevel() {
        return level;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}

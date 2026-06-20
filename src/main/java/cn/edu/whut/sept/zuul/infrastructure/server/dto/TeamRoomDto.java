package cn.edu.whut.sept.zuul.infrastructure.server.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前用户所在组队房间详情。
 */
public class TeamRoomDto {

    private String roomId;
    private String roomName;
    private String playerId;
    private String hostPlayerId;
    private boolean host;
    private boolean inGame;
    private List<RoomMemberDto> members = new ArrayList<>();

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public List<RoomMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<RoomMemberDto> members) {
        this.members = members;
    }
}

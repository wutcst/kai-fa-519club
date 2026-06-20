package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 创建联机房间请求。
 */
public class CreateRoomRequest {

    private String roomName;
    private String hostName;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}

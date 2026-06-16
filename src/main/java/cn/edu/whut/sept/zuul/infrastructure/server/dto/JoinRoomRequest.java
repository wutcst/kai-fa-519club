package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 加入联机房间请求。
 */
public class JoinRoomRequest {

    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

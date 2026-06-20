package cn.edu.whut.sept.zuul.multiplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 待处理房间邀请（按被邀请人 userId 存储）。
 */
@Component
public class RoomInviteRegistry {

    private final Map<Long, List<RoomInvite>> invitesByUser = new ConcurrentHashMap<>();

    public void sendInvite(long targetUserId, RoomInvite invite) {
        invitesByUser.compute(targetUserId, (key, list) -> {
            List<RoomInvite> invites = list == null ? new ArrayList<>() : new ArrayList<>(list);
            invites.removeIf(existing -> existing.getRoomId().equals(invite.getRoomId()));
            invites.add(invite);
            return invites;
        });
    }

    public List<RoomInvite> listInvites(long userId) {
        List<RoomInvite> invites = invitesByUser.get(userId);
        return invites == null ? List.of() : new ArrayList<>(invites);
    }

    public RoomInvite consumeInvite(long userId, String roomId) {
        List<RoomInvite> invites = invitesByUser.get(userId);
        if (invites == null) {
            return null;
        }
        RoomInvite found = null;
        for (RoomInvite invite : invites) {
            if (invite.getRoomId().equals(roomId)) {
                found = invite;
                break;
            }
        }
        if (found != null) {
            invites.remove(found);
            if (invites.isEmpty()) {
                invitesByUser.remove(userId);
            }
        }
        return found;
    }

    public void clearInvitesForUser(long userId) {
        invitesByUser.remove(userId);
    }

    public void clearInvitesForRoom(String roomId) {
        for (Map.Entry<Long, List<RoomInvite>> entry : invitesByUser.entrySet()) {
            entry.getValue().removeIf(invite -> invite.getRoomId().equals(roomId));
            if (entry.getValue().isEmpty()) {
                invitesByUser.remove(entry.getKey());
            }
        }
    }

    public void clearForTest() {
        invitesByUser.clear();
    }
}

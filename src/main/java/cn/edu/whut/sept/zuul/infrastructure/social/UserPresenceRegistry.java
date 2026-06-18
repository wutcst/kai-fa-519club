package cn.edu.whut.sept.zuul.infrastructure.social;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 用户在线状态（内存，配合客户端心跳）。
 */
@Component
public class UserPresenceRegistry {

    private static final long STALE_SECONDS = 90;

    private final Map<Long, PresenceEntry> entries = new ConcurrentHashMap<>();

    public void update(long userId, UserPresenceStatus status, String roomId) {
        if (userId <= 0) {
            return;
        }
        entries.put(userId, new PresenceEntry(status, roomId, Instant.now()));
    }

    public void markOffline(long userId) {
        if (userId <= 0) {
            return;
        }
        entries.put(userId, new PresenceEntry(UserPresenceStatus.OFFLINE, null, Instant.now()));
    }

    public UserPresenceStatus resolveStatus(long userId) {
        PresenceEntry entry = entries.get(userId);
        if (entry == null || entry.isStale()) {
            return UserPresenceStatus.OFFLINE;
        }
        return entry.status;
    }

    public String resolveRoomId(long userId) {
        PresenceEntry entry = entries.get(userId);
        if (entry == null || entry.isStale()) {
            return null;
        }
        return entry.roomId;
    }

    private static final class PresenceEntry {
        private final UserPresenceStatus status;
        private final String roomId;
        private final Instant updatedAt;

        private PresenceEntry(UserPresenceStatus status, String roomId, Instant updatedAt) {
            this.status = status;
            this.roomId = roomId;
            this.updatedAt = updatedAt;
        }

        private boolean isStale() {
            return Instant.now().isAfter(updatedAt.plusSeconds(STALE_SECONDS));
        }
    }
}

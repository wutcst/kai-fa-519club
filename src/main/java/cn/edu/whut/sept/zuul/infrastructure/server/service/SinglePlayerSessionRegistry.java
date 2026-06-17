package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cn.edu.whut.sept.zuul.Game;

/**
 * 单机会话注册表。
 */
@Component
public class SinglePlayerSessionRegistry {

    private final Map<String, SinglePlayerSession> sessions = new ConcurrentHashMap<>();

    public SinglePlayerSession createSession(String playerName) {
        Game game = new Game();
        if (playerName != null && !playerName.trim().isEmpty()) {
            game.getPlayer().setName(playerName.trim());
        }
        String sessionId = UUID.randomUUID().toString();
        SinglePlayerSession session = new SinglePlayerSession(sessionId, game);
        sessions.put(sessionId, session);
        return session;
    }

    public SinglePlayerSession findSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        SinglePlayerSession session = sessions.remove(sessionId);
        if (session != null) {
            session.shutdown();
        }
    }

    public void clearAllForTest() {
        for (SinglePlayerSession session : sessions.values()) {
            session.shutdown();
        }
        sessions.clear();
    }
}

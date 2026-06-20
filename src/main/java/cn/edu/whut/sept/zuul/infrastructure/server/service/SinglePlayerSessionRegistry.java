package cn.edu.whut.sept.zuul.infrastructure.server.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.level.LevelConfig;

/**
 * 单机会话注册表。
 */
@Component
public class SinglePlayerSessionRegistry {

    private final Map<String, SinglePlayerSession> sessions = new ConcurrentHashMap<>();

    public SinglePlayerSession createSession(String playerName, int levelNumber, int highestUnlocked, Long userId) {
        Game game = new Game();
        if (playerName != null && !playerName.trim().isEmpty()) {
            game.getPlayer().setName(playerName.trim());
        }
        game.getLevelManager().setHighestUnlockedLevel(highestUnlocked);
        if (levelNumber != LevelConfig.MIN_LEVEL) {
            game.getLevelManager().startLevel(levelNumber);
        }
        String sessionId = UUID.randomUUID().toString();
        SinglePlayerSession session = new SinglePlayerSession(sessionId, game, userId);
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

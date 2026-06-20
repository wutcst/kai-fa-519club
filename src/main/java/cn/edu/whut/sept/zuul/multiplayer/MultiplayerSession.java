package cn.edu.whut.sept.zuul.multiplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 联机客户端会话：管理房间连接、命令转发与状态轮询。
 */
public class MultiplayerSession {

    private final MultiplayerClient client;
    private final List<Consumer<GameStateSnapshot>> stateListeners = new CopyOnWriteArrayList<>();

    private String roomId;
    private String playerId;
    private String displayName;
    private GameStateSnapshot lastState;
    private ScheduledExecutorService poller;
    private volatile boolean connected;

    public MultiplayerSession(String serverUrl) {
        this.client = new MultiplayerClient(serverUrl);
    }

    public MultiplayerSession(MultiplayerClient client) {
        this.client = client;
    }

    public void addStateListener(Consumer<GameStateSnapshot> listener) {
        if (listener != null) {
            stateListeners.add(listener);
        }
    }

    public void createRoom(String roomName, String hostName) throws IOException {
        Map<String, Object> response = client.createRoom(roomName, hostName);
        bindSession(response);
    }

    public void joinRoom(String targetRoomId, String name) throws IOException {
        Map<String, Object> response = client.joinRoom(targetRoomId, name);
        bindSession(response);
    }

    @SuppressWarnings("unchecked")
    private void bindSession(Map<String, Object> response) throws IOException {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            throw new IOException("服务端未返回会话数据");
        }
        roomId = String.valueOf(data.get("roomId"));
        playerId = String.valueOf(data.get("playerId"));
        displayName = String.valueOf(data.get("displayName"));
        connected = true;
        applyStateMap((Map<String, Object>) data.get("state"));
    }

    public GameCommandResult executeCommand(String commandWord, String secondWord) throws IOException {
        Map<String, Object> response = client.executeCommand(roomId, playerId, commandWord, secondWord);
        return parseCommandResponse(response);
    }

    public GameStateSnapshot pollState() throws IOException {
        Map<String, Object> response = client.fetchState(roomId, playerId);
        applyStateResponse(response);
        return lastState;
    }

    public void startPolling() {
        startPolling(MultiplayerConfig.STATE_POLL_INTERVAL_MS);
    }

    public void startPolling(long intervalMs) {
        stopPolling();
        poller = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "multiplayer-state-poller");
            thread.setDaemon(true);
            return thread;
        });
        poller.scheduleAtFixedRate(() -> {
            if (!connected) {
                return;
            }
            try {
                pollState();
            } catch (IOException exception) {
                // 轮询失败时静默，下次继续
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void stopPolling() {
        if (poller != null) {
            poller.shutdownNow();
            poller = null;
        }
    }

    public void leave() throws IOException {
        if (!connected || roomId == null || playerId == null) {
            return;
        }
        try {
            client.leaveRoom(roomId, playerId);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        connected = false;
        stopPolling();
        roomId = null;
        playerId = null;
        lastState = null;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GameStateSnapshot getLastState() {
        return lastState;
    }

    public MultiplayerClient getClient() {
        return client;
    }

    @SuppressWarnings("unchecked")
    private GameCommandResult parseCommandResponse(Map<String, Object> response) throws IOException {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            throw new IOException("命令响应为空");
        }
        List<String> messages = new ArrayList<>();
        Object messagesObj = data.get("messages");
        if (messagesObj instanceof List) {
            for (Object message : (List<?>) messagesObj) {
                messages.add(String.valueOf(message));
            }
        }
        boolean quitRequested = Boolean.TRUE.equals(data.get("quitRequested"));
        GameStateSnapshot state = applyStateMap((Map<String, Object>) data.get("state"));
        return new GameCommandResult(messages, quitRequested, state);
    }

    @SuppressWarnings("unchecked")
    private void applyStateResponse(Map<String, Object> response) {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        applyStateMap(data);
    }

    private GameStateSnapshot applyStateMap(Map<String, Object> stateMap) {
        GameStateSnapshot snapshot = GameStateSnapshot.fromApiMap(stateMap);
        if (snapshot != null) {
            lastState = snapshot;
            notifyListeners(snapshot);
        }
        return snapshot;
    }

    private void notifyListeners(GameStateSnapshot snapshot) {
        for (Consumer<GameStateSnapshot> listener : stateListeners) {
            listener.accept(snapshot);
        }
    }
}

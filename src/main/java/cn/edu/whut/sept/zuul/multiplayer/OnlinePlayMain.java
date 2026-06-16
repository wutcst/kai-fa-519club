package cn.edu.whut.sept.zuul.multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 文本模式联机客户端演示：连接服务端并发送命令。
 *
 * <p>用法：先启动 {@link cn.edu.whut.sept.zuul.infrastructure.server.ServerApplication}，
 * 再运行本类。</p>
 * <p>参数示例：</p>
 * <ul>
 *   <li>{@code create 玩家A} — 连接本机默认服务端并建房</li>
 *   <li>{@code join <房间ID> 玩家B} — 加入房间</li>
 *   <li>{@code http://localhost:8080 create 玩家A} — 指定服务端地址</li>
 * </ul>
 */
public class OnlinePlayMain {

    private final MultiplayerClient client;
    private String roomId;
    private String playerId;
    private volatile boolean polling;
    private Thread pollThread;

    public OnlinePlayMain(String serverUrl) {
        this.client = new MultiplayerClient(serverUrl);
    }

    public static void main(String[] args) throws IOException {
        String serverUrl = MultiplayerConfig.DEFAULT_SERVER_URL;
        int commandIndex = 0;
        if (args.length > 0 && looksLikeServerUrl(args[0])) {
            serverUrl = args[0];
            commandIndex = 1;
        }

        OnlinePlayMain online = new OnlinePlayMain(serverUrl);
        if (args.length - commandIndex >= 1 && "create".equalsIgnoreCase(args[commandIndex])) {
            String hostName = args.length > commandIndex + 1 ? args[commandIndex + 1] : "房主";
            online.createAndPlay(hostName);
            return;
        }
        if (args.length - commandIndex >= 3 && "join".equalsIgnoreCase(args[commandIndex])) {
            online.joinAndPlay(args[commandIndex + 1], args[commandIndex + 2]);
            return;
        }
        online.interactiveSetup();
    }

    private static boolean looksLikeServerUrl(String value) {
        return value != null
            && (value.startsWith("http://") || value.startsWith("https://"));
    }

    private void createAndPlay(String hostName) throws IOException {
        Map<String, Object> response = client.createRoom("熄灯联机", hostName);
        bindSession(response);
        playLoop();
    }

    private void joinAndPlay(String targetRoomId, String displayName) throws IOException {
        Map<String, Object> response = client.joinRoom(targetRoomId, displayName);
        bindSession(response);
        playLoop();
    }

    private void interactiveSetup() throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("=== 《熄灯前归寝》联机客户端 ===");
        System.out.print("昵称: ");
        String name = reader.readLine();
        System.out.print("1=创建房间 2=加入房间: ");
        String choice = reader.readLine();
        if ("2".equals(choice == null ? "" : choice.trim())) {
            System.out.print("房间 ID: ");
            String id = reader.readLine();
            joinAndPlay(id.trim(), name);
        } else {
            createAndPlay(name);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindSession(Map<String, Object> response) throws IOException {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        roomId = String.valueOf(data.get("roomId"));
        playerId = String.valueOf(data.get("playerId"));
        System.out.println("已加入房间 " + roomId + "，玩家 ID: " + playerId);
        printState((Map<String, Object>) data.get("state"));
        startStatePolling();
    }

    private void startStatePolling() {
        polling = true;
        pollThread = new Thread(() -> {
            int lastRemaining = -1;
            while (polling) {
                try {
                    Thread.sleep(MultiplayerConfig.STATE_POLL_INTERVAL_MS);
                    Map<String, Object> polled = client.fetchState(roomId, playerId);
                    Map<String, Object> state = (Map<String, Object>) polled.get("data");
                    if (state == null) {
                        continue;
                    }
                    Number remaining = (Number) state.get("remainingSeconds");
                    int seconds = remaining == null ? -1 : remaining.intValue();
                    if (seconds >= 0 && seconds != lastRemaining) {
                        lastRemaining = seconds;
                        System.out.println("[同步] " + state.get("timerText")
                            + " | 第 " + state.get("level") + " 关");
                    }
                } catch (IOException | InterruptedException exception) {
                    if (exception instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "online-play-poller");
        pollThread.setDaemon(true);
        pollThread.start();
    }

    private void stopStatePolling() {
        polling = false;
        if (pollThread != null) {
            pollThread.interrupt();
        }
    }

    private void playLoop() throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("输入命令（quit 退出客户端，游戏内用 quit 结束）:");
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || "quit".equalsIgnoreCase(line.trim())) {
                stopStatePolling();
                try {
                    client.leaveRoom(roomId, playerId);
                } catch (IOException ignored) {
                    // 离开失败时仍退出客户端
                }
                break;
            }
            String[] parts = line.trim().split("\\s+", 2);
            String commandWord = parts[0];
            String secondWord = parts.length > 1 ? parts[1] : null;
            Map<String, Object> response = client.executeCommand(
                roomId, playerId, commandWord, secondWord);
            handleCommandResponse(response);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCommandResponse(Map<String, Object> response) throws IOException {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        List<String> messages = (List<String>) data.get("messages");
        if (messages != null) {
            for (String message : messages) {
                System.out.println(message);
            }
        }
        printState((Map<String, Object>) data.get("state"));
    }

    @SuppressWarnings("unchecked")
    private void printState(Map<String, Object> state) throws IOException {
        if (state == null) {
            Map<String, Object> polled = client.fetchState(roomId, playerId);
            state = (Map<String, Object>) polled.get("data");
        }
        if (state == null) {
            return;
        }
        System.out.println("--- " + state.get("timerText") + " | 第 " + state.get("level") + " 关 ---");
        List<Map<String, Object>> players = (List<Map<String, Object>>) state.get("players");
        if (players != null) {
            for (Map<String, Object> player : players) {
                System.out.println("  " + player.get("displayName") + " @ " + player.get("roomName"));
            }
        }
    }
}

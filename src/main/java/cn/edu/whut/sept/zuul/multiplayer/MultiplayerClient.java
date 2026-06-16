package cn.edu.whut.sept.zuul.multiplayer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * F6 联机 HTTP 客户端（Swing / 文本模式调用 Spring REST）。
 */
public class MultiplayerClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MultiplayerClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> createRoom(String roomName, String hostName) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("roomName", roomName);
        body.put("hostName", hostName);
        return post("/api/rooms", body);
    }

    public Map<String, Object> joinRoom(String roomId, String displayName) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("displayName", displayName);
        return post("/api/rooms/" + roomId + "/join", body);
    }

    public List<Map<String, Object>> listRooms() throws IOException {
        Map<String, Object> response = get("/api/rooms");
        Object data = response.get("data");
        return objectMapper.convertValue(data, new TypeReference<List<Map<String, Object>>>() { });
    }

    public Map<String, Object> executeCommand(String roomId, String playerId,
                                              String commandWord, String secondWord) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("roomId", roomId);
        body.put("playerId", playerId);
        body.put("commandWord", commandWord);
        if (secondWord != null) {
            body.put("secondWord", secondWord);
        }
        return post("/api/game/command", body);
    }

    public Map<String, Object> fetchState(String roomId, String playerId) throws IOException {
        String path = "/api/game/state?roomId=" + encode(roomId) + "&playerId=" + encode(playerId);
        return get(path);
    }

    public Map<String, Object> leaveRoom(String roomId, String playerId) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("playerId", playerId);
        return post("/api/rooms/" + roomId + "/leave", body);
    }

    private Map<String, Object> get(String path) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", exception);
        }
    }

    private Map<String, Object> post(String path, Object body) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", exception);
        }
    }

    private Map<String, Object> parseResponse(HttpResponse<String> response) throws IOException {
        Map<String, Object> parsed = objectMapper.readValue(
            response.body(), new TypeReference<Map<String, Object>>() { });
        Number code = (Number) parsed.get("code");
        if (code != null && code.intValue() != 0) {
            Object message = parsed.get("message");
            throw new IOException(message == null ? "请求失败" : message.toString());
        }
        return parsed;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "http://localhost:8080";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String encode(String value) {
        return value == null ? "" : value.replace(" ", "%20");
    }
}

package cn.edu.whut.sept.zuul.infrastructure.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import cn.edu.whut.sept.zuul.infrastructure.server.service.AuthServiceProvider;
import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F6 联机 REST API 集成测试。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
public class MultiplayerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MultiplayerRoomService multiplayerRoomService;

    @Autowired
    private AuthServiceProvider authServiceProvider;

    private int userCounter;

    @Before
    public void clearRooms() {
        multiplayerRoomService.clearAllRoomsForTest();
        userCounter++;
    }

    @Test
    public void testCreateJoinAndMove() throws Exception {
        String hostToken = signupToken("api_host_" + userCounter, "主机");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"API测试房\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.roomId").exists())
            .andExpect(jsonPath("$.data.playerId").exists())
            .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        String roomId = extractJsonValue(createBody, "\"roomId\":\"", "\"");
        String hostPlayerId = extractJsonValue(createBody, "\"playerId\":\"", "\"");

        String guestToken = signupToken("api_guest_" + userCounter, "客人");
        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .header("X-Auth-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.playerId").exists());

        mockMvc.perform(post("/api/game/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"" + roomId + "\",\"playerId\":\"" + hostPlayerId
                    + "\",\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.state.roomId").value("boxue_main"));

        mockMvc.perform(get("/api/game/state")
                .param("roomId", roomId)
                .param("playerId", hostPlayerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.remainingSeconds").isNumber())
            .andExpect(jsonPath("$.data.players").isArray());
    }

    @Test
    public void testMultiplayerTimerSyncedFromServerAfterStart() throws Exception {
        String hostToken = signupToken("timer_host_" + userCounter, "房主");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"计时同步房\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");
        String hostPlayerId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"playerId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/start")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"levelNumber\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/game/state")
                .param("roomId", roomId)
                .param("playerId", hostPlayerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.remainingSeconds").value(240))
            .andExpect(jsonPath("$.data.timerText").value("距熄灯（23:00）还有 240 秒"));

        mockMvc.perform(post("/api/game/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"" + roomId + "\",\"playerId\":\"" + hostPlayerId
                    + "\",\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state.remainingSeconds")
                .value(240 - ActionTimeCost.GO));
    }

    @Test
    public void testListRooms() throws Exception {
        String token = signupToken("list_host_" + userCounter, "房主");
        mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"大厅房\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[?(@.roomName == '大厅房')]").exists());
    }

    @Test
    public void testLeaveRoomApi() throws Exception {
        String token = signupToken("leave_host_" + userCounter, "房主");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"离开API\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String createBody = createResult.getResponse().getContentAsString();
        String roomId = extractJsonValue(createBody, "\"roomId\":\"", "\"");
        String hostPlayerId = extractJsonValue(createBody, "\"playerId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"" + hostPlayerId + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roomId == '" + roomId + "')]").doesNotExist());
    }

    @Test
    public void testStartGameThenHostRejoin() throws Exception {
        String hostToken = signupToken("start_host_" + userCounter, "房主");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"开局房\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        String roomId = extractJsonValue(createBody, "\"roomId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/start")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"levelNumber\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.inGame").value(true))
            .andExpect(jsonPath("$.data.state.roomId").exists());
    }

    @Test
    public void testJoinFullRoomFails() throws Exception {
        String hostToken = signupToken("full_p1_" + userCounter, "玩家1");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"满员房\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        for (int index = 2; index <= 4; index++) {
            String token = signupToken("full_p" + index + "_" + userCounter, "玩家" + index);
            mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                    .header("X-Auth-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        }

        String overflowToken = signupToken("full_p5_" + userCounter, "玩家5");
        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .header("X-Auth-Token", overflowToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    public void testRoomChatMessageVisibleToOtherPlayers() throws Exception {
        String hostToken = signupToken("chat_host_" + userCounter, "主机");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"聊天房\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String createBody = createResult.getResponse().getContentAsString();
        String roomId = extractJsonValue(createBody, "\"roomId\":\"", "\"");
        String hostPlayerId = extractJsonValue(createBody, "\"playerId\":\"", "\"");

        String guestToken = signupToken("chat_guest_" + userCounter, "客人");
        MvcResult joinResult = mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .header("X-Auth-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String guestPlayerId = extractJsonValue(
            joinResult.getResponse().getContentAsString(), "\"playerId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"" + hostPlayerId + "\",\"text\":\"大家好\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.text").value("大家好"));

        mockMvc.perform(get("/api/game/state")
                .param("roomId", roomId)
                .param("playerId", guestPlayerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.chatMessages[0].text").value("大家好"))
            .andExpect(jsonPath("$.data.roomItems").isArray())
            .andExpect(jsonPath("$.data.inventory").isArray());
    }

    @Test
    public void testHostAbandonLobbyThenCreateNewRoom() throws Exception {
        String token = signupToken("multi_host_" + userCounter, "房主");
        MvcResult first = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"房间一\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        String roomId = extractJsonValue(first.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/abandon-lobby")
                .header("X-Auth-Token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"房间二\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].roomName").value("房间二"));
    }

    @Test
    public void testHostLogoutDissolvesRoom() throws Exception {
        String hostToken = signupToken("logout_host_" + userCounter, "房主");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"登出解散房\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        mockMvc.perform(post("/api/auth/signout")
                .header("X-Auth-Token", hostToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roomId == '" + roomId + "')]").doesNotExist());
    }

    @Test
    public void testHostAbandonLobbyDissolvesInGameRoom() throws Exception {
        String hostToken = signupToken("abandon_ingame_" + userCounter, "房主");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"游戏中解散\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/start")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"levelNumber\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/rooms/" + roomId + "/abandon-lobby")
                .header("X-Auth-Token", hostToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roomId == '" + roomId + "')]").doesNotExist());
    }

    @Test
    public void testCreateRoomRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"未登录房\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.message").value("请先登录后再进入联机模式"));
    }

    @Test
    public void testNicknameBoundToAccountNotClientInput() throws Exception {
        String token = signupToken("bind_user_" + userCounter, "账号昵称");
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"绑定测试\",\"hostName\":\"伪造昵称\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.displayName").value("账号昵称"))
            .andReturn();
    }

    @Test
    public void testRoomInvitesVisibleExceptWhenGuestInOthersRoom() throws Exception {
        String hostToken = signupToken("invite_host_" + userCounter, "房主");
        String targetToken = signupToken("invite_target_" + userCounter, "受邀者");
        String guestToken = signupToken("invite_guest_" + userCounter, "队员");

        long targetUserId = extractUserId(targetToken);
        makeFriends(hostToken, targetToken);

        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"邀请测试房\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        mockMvc.perform(post("/api/presence/heartbeat")
                .header("X-Auth-Token", targetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ONLINE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/rooms/" + roomId + "/invite")
                .header("X-Auth-Token", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"friendUserId\":" + targetUserId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms/invites").header("X-Auth-Token", targetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].roomId").value(roomId));

        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .header("X-Auth-Token", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/rooms/invites").header("X-Auth-Token", guestToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    private void makeFriends(String tokenA, String tokenB) throws Exception {
        String usernameB = extractJsonValue(
            mockMvc.perform(get("/api/auth/profile").header("X-Auth-Token", tokenB))
                .andReturn().getResponse().getContentAsString(),
            "\"username\":\"", "\"");
        mockMvc.perform(post("/api/friends")
                .header("X-Auth-Token", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + usernameB + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
        long userIdA = extractUserId(tokenA);
        mockMvc.perform(post("/api/friends/requests/" + userIdA + "/accept")
                .header("X-Auth-Token", tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    private long extractUserId(String token) throws Exception {
        MvcResult profile = mockMvc.perform(get("/api/auth/profile").header("X-Auth-Token", token))
            .andExpect(status().isOk())
            .andReturn();
        String body = profile.getResponse().getContentAsString();
        String raw = extractJsonValue(body, "\"userId\":", ",");
        if (raw.endsWith("}")) {
            raw = raw.substring(0, raw.length() - 1);
        }
        return Long.parseLong(raw.trim());
    }

    private String signupToken(String usernamePrefix, String displayName) throws Exception {
        String username = usernamePrefix + "_" + System.nanoTime();
        String email = username + "@example.com";
        mockMvc.perform(post("/api/auth/register/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        String code = authServiceProvider.getAuthService().getVerificationRepository()
            .findLatestCodeForTest(email)
            .orElseThrow(() -> new AssertionError("未生成验证码"));

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"secret1\","
                    + "\"confirmPassword\":\"secret1\",\"displayName\":\"" + displayName + "\","
                    + "\"email\":\"" + email + "\",\"verificationCode\":\"" + code + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        return extractJsonValue(result.getResponse().getContentAsString(), "\"token\":\"", "\"");
    }

    private String extractJsonValue(String json, String prefix, String suffix) {
        int start = json.indexOf(prefix);
        assertTrue("JSON 中未找到字段: " + prefix, start >= 0);
        start += prefix.length();
        int end = json.indexOf(suffix, start);
        assertTrue(end > start);
        return json.substring(start, end);
    }
}

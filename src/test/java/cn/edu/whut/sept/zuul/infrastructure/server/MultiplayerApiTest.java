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

import cn.edu.whut.sept.zuul.infrastructure.server.service.MultiplayerRoomService;

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

    @Before
    public void clearRooms() {
        multiplayerRoomService.clearAllRoomsForTest();
    }

    @Test
    public void testCreateJoinAndMove() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"API测试房\",\"hostName\":\"主机\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.roomId").exists())
            .andExpect(jsonPath("$.data.playerId").exists())
            .andReturn();

        String createBody = createResult.getResponse().getContentAsString();
        String roomId = extractJsonValue(createBody, "\"roomId\":\"", "\"");
        String hostPlayerId = extractJsonValue(createBody, "\"playerId\":\"", "\"");

        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"客人\"}"))
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
    public void testListRooms() throws Exception {
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"大厅房\",\"hostName\":\"房主\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/rooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[?(@.roomName == '大厅房')]").exists());
    }

    @Test
    public void testLeaveRoomApi() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"离开API\",\"hostName\":\"房主\"}"))
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
    public void testJoinFullRoomFails() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomName\":\"满员房\",\"hostName\":\"玩家1\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String roomId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"roomId\":\"", "\"");

        for (int index = 2; index <= 4; index++) {
            mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"displayName\":\"玩家" + index + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        }

        mockMvc.perform(post("/api/rooms/" + roomId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"玩家5\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
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

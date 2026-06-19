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

import cn.edu.whut.sept.zuul.infrastructure.server.service.SinglePlayerGuiService;
import cn.edu.whut.sept.zuul.level.ActionTimeCost;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 单机 Vue REST API 集成测试。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
public class SinglePlayerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SinglePlayerGuiService singlePlayerGuiService;

    @Before
    public void clearSessions() {
        singlePlayerGuiService.clearAllSessionsForTest();
    }

    @Test
    public void testCreateSessionAndMove() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerName\":\"Vue玩家\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.sessionId").exists())
            .andExpect(jsonPath("$.data.state.level").value(1))
            .andReturn();

        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.state.roomId").value("boxue_main"))
            .andExpect(jsonPath("$.data.popupMessage").doesNotExist());

        mockMvc.perform(get("/api/solo/sessions/" + sessionId + "/state"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.inventory").isArray())
            .andExpect(jsonPath("$.data.inventoryWeight").value(0))
            .andExpect(jsonPath("$.data.maxInventoryWeight").value(3000))
            .andExpect(jsonPath("$.data.remainingCapacity").value(3000))
            .andExpect(jsonPath("$.data.exits.north").isBoolean());
    }

    @Test
    public void testTakeDoesNotPopupWeightNotice() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"take\",\"secondWord\":\"社团传单\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.popupMessage").doesNotExist())
            .andExpect(jsonPath("$.data.state.inventoryWeight").value(30))
            .andExpect(jsonPath("$.data.state.remainingCapacity").value(2970));
    }

    @Test
    public void testLookIncludesBulletin() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/look"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.popupMessage").exists());
    }

    @Test
    public void testGuiAssetsAreServed() throws Exception {
        mockMvc.perform(get("/assets/gui/rooms/gate.png"))
            .andExpect(status().isOk());
    }

    @Test
    public void testListLevelsGuestOnlyFirstUnlocked() throws Exception {
        mockMvc.perform(get("/api/solo/levels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.levels[0].levelNumber").value(1))
            .andExpect(jsonPath("$.data.levels[0].unlocked").value(true))
            .andExpect(jsonPath("$.data.levels[1].unlocked").value(false))
            .andExpect(jsonPath("$.data.comingSoonLabel").value("…"));
    }

    @Test
    public void testCreateSessionRejectsLockedLevel() throws Exception {
        mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerName\":\"Vue玩家\",\"levelNumber\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.message").value("第 2 关尚未解锁，请先通关上一关"));
    }

    @Test
    public void testCreateSessionWithLevelNumber() throws Exception {
        mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerName\":\"Vue玩家\",\"levelNumber\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.state.level").value(1));
    }

    @Test
    public void testStateExposesTimerTextForVueHud() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerName\":\"计时HUD\"}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(get("/api/solo/sessions/" + sessionId + "/state"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.remainingSeconds").value(240))
            .andExpect(jsonPath("$.data.timerText").value("距熄灯（23:00）还有 240 秒"))
            .andExpect(jsonPath("$.data.levelTitle").exists());
    }

    @Test
    public void testGoCommandDeductsRemainingSeconds() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state.remainingSeconds")
                .value(240 - ActionTimeCost.GO));
    }

    @Test
    public void testTalkEndpointReturnsNpcDialogue() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"north\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/talk"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.popupMessage").exists());
    }

    @Test
    public void testLockedExitSetsOverlayMessage() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/solo/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andReturn();
        String sessionId = extractJsonValue(
            createResult.getResponse().getContentAsString(), "\"sessionId\":\"", "\"");

        mockMvc.perform(post("/api/solo/sessions/" + sessionId + "/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandWord\":\"go\",\"secondWord\":\"west\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state.lockedOverlayMessage").exists())
            .andExpect(jsonPath("$.data.state.roomId").value("gate"));
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

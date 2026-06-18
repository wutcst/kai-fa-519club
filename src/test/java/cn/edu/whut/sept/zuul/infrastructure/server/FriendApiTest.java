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

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 好友 API 集成测试。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
public class FriendApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthServiceProvider authServiceProvider;

    private int counter;

    @Before
    public void resetCounter() {
        counter++;
    }

    @Test
    public void testFriendRequestAcceptAndListWithPresence() throws Exception {
        String tokenA = signupToken("friend_a_" + counter, "甲同学");
        String tokenB = signupToken("friend_b_" + counter, "乙同学");
        String usernameB = extractJsonValue(
            mockMvc.perform(get("/api/auth/profile").header("X-Auth-Token", tokenB))
                .andReturn().getResponse().getContentAsString(),
            "\"username\":\"", "\"");

        mockMvc.perform(post("/api/friends")
                .header("X-Auth-Token", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + usernameB + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.displayName").value("乙同学"));

        mockMvc.perform(get("/api/friends/requests/incoming").header("X-Auth-Token", tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].displayName").value("甲同学"));

        mockMvc.perform(get("/api/friends").header("X-Auth-Token", tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(post("/api/friends/requests/1/accept")
                .header("X-Auth-Token", tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));

        String userIdA = extractJsonValue(
            mockMvc.perform(get("/api/auth/profile").header("X-Auth-Token", tokenA))
                .andReturn().getResponse().getContentAsString(),
            "\"userId\":", ",");

        mockMvc.perform(post("/api/friends/requests/" + userIdA + "/accept")
                .header("X-Auth-Token", tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.displayName").value("甲同学"));

        mockMvc.perform(post("/api/presence/heartbeat")
                .header("X-Auth-Token", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ONLINE\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/friends").header("X-Auth-Token", tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("ONLINE"))
            .andExpect(jsonPath("$.data[0].statusLabel").value("在线"));
    }

    private String signupToken(String usernamePrefix, String displayName) throws Exception {
        String username = usernamePrefix + "_" + System.nanoTime();
        String email = username + "@example.com";
        mockMvc.perform(post("/api/auth/register/code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
            .andExpect(status().isOk());
        String code = authServiceProvider.getAuthService().getVerificationRepository()
            .findLatestCodeForTest(email)
            .orElseThrow(() -> new AssertionError("未生成验证码"));
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"secret1\","
                    + "\"confirmPassword\":\"secret1\",\"displayName\":\"" + displayName + "\","
                    + "\"email\":\"" + email + "\",\"verificationCode\":\"" + code + "\"}"))
            .andExpect(status().isOk())
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

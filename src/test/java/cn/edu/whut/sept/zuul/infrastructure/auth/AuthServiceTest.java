package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 用户注册登录服务单元测试。
 */
public class AuthServiceTest {

    private AuthService authService;

    @Before
    public void setUp() {
        String dbName = "zuul_auth_test_" + UUID.randomUUID().toString().replace("-", "");
        H2Database database = H2Database.createInMemoryDatabase(dbName);
        authService = AuthService.create(database);
    }

    @Test
    public void testRegisterAndLogin() {
        String email = "player01@example.com";
        authService.sendRegisterCode(email);
        String code = authService.getVerificationRepository().findLatestCodeForTest(email).orElse("");
        AuthResult registerResult = authService.register(
            "player01", "secret1", "secret1", "玩家一", email, code);
        assertTrue(registerResult.isSuccess());
        assertNotNull(registerResult.getSession().getToken());
        assertEquals("player01@example.com", registerResult.getAccount().getEmail());

        AuthResult loginResult = authService.login("player01", "secret1");
        assertTrue(loginResult.isSuccess());
        assertEquals("玩家一", loginResult.getSession().getDisplayName());
    }

    @Test
    public void testRegisterRequiresMatchingPassword() {
        String email = "player02@example.com";
        authService.sendRegisterCode(email);
        String code = authService.getVerificationRepository().findLatestCodeForTest(email).orElse("");
        AuthResult registerResult = authService.register(
            "player02", "secret1", "different", "玩家二", email, code);
        assertFalse(registerResult.isSuccess());
    }

    @Test
    public void testLoginWithWrongPassword() {
        authService.registerConsole("player03", "secret1", "玩家三");
        AuthResult loginResult = authService.login("player03", "wrong");
        assertFalse(loginResult.isSuccess());
    }

    @Test
    public void testValidateToken() {
        AuthResult registerResult = authService.registerConsole("player04", "secret1", "玩家四");
        String token = registerResult.getSession().getToken();
        assertTrue(authService.validateToken(token).isPresent());
    }

    @Test
    public void testLogoutInvalidatesToken() {
        AuthResult registerResult = authService.registerConsole("player05", "secret1", "玩家五");
        String token = registerResult.getSession().getToken();
        authService.logout(token);
        assertFalse(authService.validateToken(token).isPresent());
    }

    @Test
    public void testChangePassword() {
        AuthResult registerResult = authService.registerConsole("player06", "secret1", "玩家六");
        String token = registerResult.getSession().getToken();
        AuthResult changeResult = authService.changePassword(token, "secret1", "newpass1", "newpass1");
        assertTrue(changeResult.isSuccess());
        assertTrue(authService.login("player06", "newpass1").isSuccess());
    }
}

package cn.edu.whut.sept.zuul.regression;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import cn.edu.whut.sept.zuul.Main;
import cn.edu.whut.sept.zuul.infrastructure.server.ServerApplication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * T6 CI 回归：验证与 GitHub Actions 相同的关键入口与工程配置存在。
 */
public class CiRegressionTest {

    @Test
    public void regressionServerApplicationBootstraps() {
        assertNotNull(ServerApplication.class.getDeclaredMethods());
    }

    @Test
    public void regressionConsoleMainEntryExists() throws Exception {
        assertNotNull(Main.class.getDeclaredMethod("main", String[].class));
    }

    @Test
    public void regressionCiWorkflowAndPomConfigured() throws Exception {
        Path workflow = Paths.get(".github/workflows/maven.yml");
        Path pom = Paths.get("pom.xml");
        assertTrue("缺少 CI 工作流", Files.exists(workflow));
        assertTrue("缺少 pom.xml", Files.exists(pom));
        String workflowText = Files.readString(workflow);
        assertTrue(workflowText.contains("mvn checkstyle:check"));
        assertTrue(workflowText.contains("mvn test"));
        assertTrue(workflowText.contains("mvn package"));
        assertTrue(workflowText.contains("game-jar"));
    }
}

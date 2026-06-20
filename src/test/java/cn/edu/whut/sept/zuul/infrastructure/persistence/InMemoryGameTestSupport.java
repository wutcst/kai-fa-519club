package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.util.UUID;

import cn.edu.whut.sept.zuul.Game;

/**
 * T3/F8 测试辅助：为每个用例创建独立内存 H2，避免污染仓库文件库。
 */
public final class InMemoryGameTestSupport {

    private InMemoryGameTestSupport() {
    }

    /**
     * 创建绑定内存存档服务的游戏实例。
     *
     * @return 可存档/读档的 Game
     */
    public static Game createGameWithInMemoryPersistence() {
        String dbName = "zuul_mem_" + UUID.randomUUID().toString().replace("-", "");
        H2Database database = H2Database.createInMemoryDatabase(dbName);
        GamePersistenceService service = GamePersistenceService.create(database);
        Game game = new Game();
        game.setPersistenceService(service);
        return game;
    }

    /**
     * 模拟进程重启：新 Game 实例复用同一持久化服务。
     *
     * @param source 已绑定持久化服务的游戏
     * @return 全新 Game 实例（内核状态未初始化到存档）
     */
    public static Game createRestartedGame(Game source) {
        Game restarted = new Game();
        restarted.setPersistenceService(source.getPersistenceService());
        return restarted;
    }
}

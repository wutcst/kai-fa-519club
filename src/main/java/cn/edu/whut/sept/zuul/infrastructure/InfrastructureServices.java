package cn.edu.whut.sept.zuul.infrastructure;

import cn.edu.whut.sept.zuul.infrastructure.auth.AuthService;
import cn.edu.whut.sept.zuul.infrastructure.persistence.DatabaseProvider;
import cn.edu.whut.sept.zuul.infrastructure.persistence.GamePersistenceService;
import cn.edu.whut.sept.zuul.infrastructure.persistence.H2Database;

/**
 * 基础设施服务聚合：统一 H2 上的认证与存档服务。
 */
public final class InfrastructureServices {

    private final H2Database database;
    private final AuthService authService;
    private final GamePersistenceService persistenceService;

    private InfrastructureServices(H2Database database) {
        this.database = database;
        this.authService = AuthService.create(database);
        this.persistenceService = GamePersistenceService.create(database);
    }

    /**
     * 获取默认基础设施服务（共享 DatabaseProvider 单例）。
     *
     * @return InfrastructureServices 实例
     */
    public static InfrastructureServices getDefault() {
        return new InfrastructureServices(DatabaseProvider.getDefault());
    }

    /**
     * 基于指定数据库创建（测试用）。
     *
     * @param database H2 实例
     * @return InfrastructureServices 实例
     */
    public static InfrastructureServices fromDatabase(H2Database database) {
        return new InfrastructureServices(database);
    }

    public H2Database getDatabase() {
        return database;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public GamePersistenceService getPersistenceService() {
        return persistenceService;
    }
}

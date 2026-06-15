package cn.edu.whut.sept.zuul.infrastructure.persistence;

/**
 * 共享 H2 数据源：存档、登录注册共用同一连接配置（后续 Spring DataSource 可替换此入口）。
 */
public final class DatabaseProvider {

    private static volatile H2Database defaultDatabase;

    private DatabaseProvider() {
    }

    /**
     * 获取默认文件库单例（首次调用时初始化表结构）。
     *
     * @return H2Database 实例
     */
    public static H2Database getDefault() {
        if (defaultDatabase == null) {
            synchronized (DatabaseProvider.class) {
                if (defaultDatabase == null) {
                    H2Database database = H2Database.createDefaultFileDatabase();
                    database.initializeSchema();
                    defaultDatabase = database;
                }
            }
        }
        return defaultDatabase;
    }

    /**
     * 测试或自定义场景注入内存库。
     *
     * @param database 测试用 H2 实例
     */
    public static void overrideForTests(H2Database database) {
        synchronized (DatabaseProvider.class) {
            database.initializeSchema();
            defaultDatabase = database;
        }
    }

    /**
     * 测试结束后清理单例。
     */
    public static void reset() {
        synchronized (DatabaseProvider.class) {
            defaultDatabase = null;
        }
    }
}

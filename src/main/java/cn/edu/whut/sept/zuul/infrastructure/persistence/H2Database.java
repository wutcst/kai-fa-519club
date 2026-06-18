package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 数据库连接与表结构初始化（F8）。
 */
public class H2Database {

    /** 单机存档默认文件库路径（相对工作目录） */
    public static final String DEFAULT_FILE_JDBC_URL = "jdbc:h2:file:./data/zuul-save";

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private final String jdbcUrl;

    /**
     * 使用指定 JDBC URL 创建数据库访问对象。
     *
     * @param jdbcUrl H2 连接串
     */
    public H2Database(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * 创建默认文件库实例（答辩演示用）。
     *
     * @return H2Database 实例
     */
    public static H2Database createDefaultFileDatabase() {
        return new H2Database(DEFAULT_FILE_JDBC_URL);
    }

    /**
     * 创建内存库实例（单元测试用）。
     *
     * @param databaseName 内存库名称，须唯一
     * @return H2Database 实例
     */
    public static H2Database createInMemoryDatabase(String databaseName) {
        String url = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1";
        return new H2Database(url);
    }

    /**
     * 打开数据库连接。
     *
     * @return JDBC 连接
     * @throws SQLException 连接失败
     */
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    /**
     * 初始化存档与通关记录表结构。
     */
    public void initializeSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "CREATE TABLE IF NOT EXISTS game_save ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "player_name VARCHAR(64) NOT NULL, "
                    + "level_number INT NOT NULL, "
                    + "remaining_seconds INT NOT NULL, "
                    + "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS clear_record ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "player_name VARCHAR(64) NOT NULL, "
                    + "cleared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS app_user ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(64) NOT NULL UNIQUE, "
                    + "password_hash VARCHAR(256) NOT NULL, "
                    + "display_name VARCHAR(64) NOT NULL, "
                    + "email VARCHAR(128), "
                    + "avatar_url VARCHAR(256), "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")"
            );
            statement.execute("ALTER TABLE app_user ADD COLUMN IF NOT EXISTS email VARCHAR(128)");
            statement.execute("ALTER TABLE app_user ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(256)");
            statement.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email)"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS email_verification_code ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "email VARCHAR(128) NOT NULL, "
                    + "code VARCHAR(8) NOT NULL, "
                    + "purpose VARCHAR(32) NOT NULL DEFAULT 'register', "
                    + "expires_at TIMESTAMP NOT NULL, "
                    + "used BOOLEAN DEFAULT FALSE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS user_session ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT NOT NULL, "
                    + "token VARCHAR(64) NOT NULL UNIQUE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "expires_at TIMESTAMP NOT NULL"
                    + ")"
            );
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS user_id BIGINT");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS current_room_id VARCHAR(64)");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS dormitory_submit BOOLEAN DEFAULT FALSE");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS west_exit_locked BOOLEAN DEFAULT FALSE");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS west_lock_broken BOOLEAN DEFAULT FALSE");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS gym_unlocked BOOLEAN DEFAULT FALSE");
            statement.execute(
                "ALTER TABLE game_save ADD COLUMN IF NOT EXISTS dorm_password_unlocked BOOLEAN DEFAULT FALSE");
            statement.execute("ALTER TABLE game_save ADD COLUMN IF NOT EXISTS magic_cookie_used BOOLEAN DEFAULT FALSE");
            statement.execute(
                "CREATE TABLE IF NOT EXISTS user_friend ("
                    + "user_id BIGINT NOT NULL, "
                    + "friend_user_id BIGINT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (user_id, friend_user_id)"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS friend_request ("
                    + "from_user_id BIGINT NOT NULL, "
                    + "to_user_id BIGINT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (from_user_id, to_user_id)"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS user_level_progress ("
                    + "user_id BIGINT PRIMARY KEY, "
                    + "highest_cleared_level INT NOT NULL DEFAULT 0, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")"
            );
            statement.execute(
                "CREATE TABLE IF NOT EXISTS game_save_item ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "save_id BIGINT NOT NULL, "
                    + "item_name VARCHAR(128) NOT NULL, "
                    + "item_weight INT NOT NULL, "
                    + "long_description VARCHAR(512)"
                    + ")"
            );
        } catch (SQLException exception) {
            throw new PersistenceException("初始化 H2 表结构失败", exception);
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}

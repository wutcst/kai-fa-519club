package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.edu.whut.sept.zuul.infrastructure.auth.UserAccount;

/**
 * 好友关系数据访问（双向存储）。
 */
public class FriendRepository {

    private final H2Database database;

    public FriendRepository(H2Database database) {
        this.database = database;
    }

    public void addFriendship(long userId, long friendUserId) {
        if (userId == friendUserId) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        insertOneWay(userId, friendUserId);
        insertOneWay(friendUserId, userId);
    }

    public boolean areFriends(long userId, long friendUserId) {
        String sql = "SELECT 1 FROM user_friend WHERE user_id = ? AND friend_user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, friendUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询好友关系失败", exception);
        }
    }

    public List<UserAccount> listFriends(long userId) {
        String sql = "SELECT u.id, u.username, u.display_name, u.email, u.avatar_url, u.created_at "
            + "FROM user_friend f JOIN app_user u ON f.friend_user_id = u.id "
            + "WHERE f.user_id = ? ORDER BY u.display_name, u.username";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<UserAccount> friends = new ArrayList<>();
                while (resultSet.next()) {
                    friends.add(mapUser(resultSet));
                }
                return friends;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询好友列表失败", exception);
        }
    }

    public void removeFriendship(long userId, long friendUserId) {
        deleteOneWay(userId, friendUserId);
        deleteOneWay(friendUserId, userId);
    }

    public boolean hasPendingRequest(long fromUserId, long toUserId) {
        String sql = "SELECT 1 FROM friend_request WHERE from_user_id = ? AND to_user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, fromUserId);
            statement.setLong(2, toUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询好友申请失败", exception);
        }
    }

    public void createFriendRequest(long fromUserId, long toUserId) {
        String sql = "MERGE INTO friend_request (from_user_id, to_user_id) KEY (from_user_id, to_user_id) "
            + "VALUES (?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, fromUserId);
            statement.setLong(2, toUserId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("发送好友申请失败", exception);
        }
    }

    public List<UserAccount> listIncomingFriendRequests(long userId) {
        String sql = "SELECT u.id, u.username, u.display_name, u.email, u.avatar_url, u.created_at, "
            + "r.created_at AS request_created_at "
            + "FROM friend_request r JOIN app_user u ON r.from_user_id = u.id "
            + "WHERE r.to_user_id = ? ORDER BY r.created_at DESC";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<UserAccount> requests = new ArrayList<>();
                while (resultSet.next()) {
                    requests.add(mapUser(resultSet));
                }
                return requests;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询好友申请列表失败", exception);
        }
    }

    public Optional<UserAccount> findIncomingRequestSender(long userId, long fromUserId) {
        String sql = "SELECT u.id, u.username, u.display_name, u.email, u.avatar_url, u.created_at "
            + "FROM friend_request r JOIN app_user u ON r.from_user_id = u.id "
            + "WHERE r.to_user_id = ? AND r.from_user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, fromUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("查询好友申请失败", exception);
        }
    }

    public void deleteFriendRequest(long fromUserId, long toUserId) {
        String sql = "DELETE FROM friend_request WHERE from_user_id = ? AND to_user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, fromUserId);
            statement.setLong(2, toUserId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("删除好友申请失败", exception);
        }
    }

    public void deleteAllFriendRequestsBetween(long userId, long otherUserId) {
        deleteFriendRequest(userId, otherUserId);
        deleteFriendRequest(otherUserId, userId);
    }

    public Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT id, username, display_name, email, avatar_url, created_at "
            + "FROM app_user WHERE LOWER(username) = LOWER(?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("按用户名查询用户失败", exception);
        }
    }

    private void insertOneWay(long userId, long friendUserId) {
        String sql = "MERGE INTO user_friend (user_id, friend_user_id) KEY (user_id, friend_user_id) "
            + "VALUES (?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, friendUserId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("添加好友失败", exception);
        }
    }

    private void deleteOneWay(long userId, long friendUserId) {
        String sql = "DELETE FROM user_friend WHERE user_id = ? AND friend_user_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, friendUserId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("删除好友失败", exception);
        }
    }

    private UserAccount mapUser(ResultSet resultSet) throws SQLException {
        return new UserAccount(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            null,
            resultSet.getString("display_name"),
            resultSet.getString("email"),
            resultSet.getString("avatar_url"),
            resultSet.getTimestamp("created_at") != null
                ? resultSet.getTimestamp("created_at").toLocalDateTime() : null
        );
    }
}

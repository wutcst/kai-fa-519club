package cn.edu.whut.sept.zuul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.whut.sept.zuul.Item;

/**
 * 存档背包物品表数据访问。
 */
public class SaveItemRepository {

    private final H2Database database;

    public SaveItemRepository(H2Database database) {
        this.database = database;
    }

    /**
     * 保存某存档的全部背包物品（先删后插）。
     *
     * @param saveId 存档主键
     * @param inventory 背包物品
     */
    public void replaceItemsForSave(long saveId, List<Item> inventory) {
        deleteBySaveId(saveId);
        if (inventory == null || inventory.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO game_save_item (save_id, item_name, item_weight, long_description) "
            + "VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Item item : inventory) {
                statement.setLong(1, saveId);
                statement.setString(2, item.getDescription());
                statement.setInt(3, item.getWeight());
                statement.setString(4, item.getLongDescription());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new PersistenceException("保存背包物品失败", exception);
        }
    }

    /**
     * 查询某存档的背包物品。
     *
     * @param saveId 存档主键
     * @return 物品列表
     */
    public List<Item> findBySaveId(long saveId) {
        String sql = "SELECT item_name, item_weight, long_description FROM game_save_item "
            + "WHERE save_id = ? ORDER BY id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Item> items = new ArrayList<>();
                while (resultSet.next()) {
                    items.add(new Item(
                        resultSet.getString("item_name"),
                        resultSet.getInt("item_weight"),
                        resultSet.getString("long_description")
                    ));
                }
                return items;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("读取背包物品失败", exception);
        }
    }

    private void deleteBySaveId(long saveId) {
        String sql = "DELETE FROM game_save_item WHERE save_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, saveId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("清理存档物品失败", exception);
        }
    }
}

/**
 * 测试Item类的核心功能，包括属性初始化、getter方法、详情字符串格式化。
 * 验证物品描述和重量的正确性，以及详情展示格式符合预期。
 *
 * @author liujing
 * @version 1.2
 */
package cn.edu.whut.sept.zuul;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemTest {

    /**
     * 测试Item实例初始化是否正确赋值描述和重量。
     * 验证构造函数能正确存储传入的描述和重量，getter方法能准确返回。
     */
    @Test
    public void testItemInitialization() {
        // 准备测试数据
        String expectedDesc = "一把旧钥匙";
        int expectedWeight = 50;
        Item item = new Item(expectedDesc, expectedWeight);

        // 断言属性值正确
        assertEquals("物品描述初始化错误", expectedDesc, item.getDescription());
        assertEquals("物品重量初始化错误", expectedWeight, item.getWeight());
    }

    /**
     * 测试getDetails()方法是否返回格式化的物品详情。
     * 验证返回字符串包含描述和重量，格式为「描述 (重量: Xg)」。
     */
    @Test
    public void testGetDetails() {
        // 准备测试数据
        Item book = new Item("一本教科书", 800);
        String expectedDetails = "一本教科书 (重量: 800g)";

        // 断言详情格式正确
        assertEquals("物品详情格式化错误", expectedDetails, book.getDetails());
    }
}
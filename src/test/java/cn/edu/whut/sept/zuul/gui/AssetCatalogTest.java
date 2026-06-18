package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.Test;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.GatedRoom;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AssetCatalog 单元测试。
 */
public class AssetCatalogTest {

    @Test
    public void roomImagePathUsesRoomId() {
        assertEquals("/assets/gui/rooms/gate.png", AssetCatalog.roomImagePath("gate"));
        assertEquals("/assets/gui/rooms/boxue_main.png", AssetCatalog.roomImagePath("boxue_main"));
    }

    @Test
    public void itemSlugMapsEssentials() {
        assertEquals("money_30yuan", AssetCatalog.itemSlug(UseCommand.MONEY_ITEM));
        assertEquals("campus_card", AssetCatalog.itemSlug(GatedRoom.CAMPUS_CARD_ITEM));
        assertEquals("stick", AssetCatalog.itemSlug(CombineCommand.STICK_ITEM));
        assertEquals("magic_cookie", AssetCatalog.itemSlug(FoodItems.MAGIC_COOKIE));
    }

    @Test
    public void itemImagePathUsesSlug() {
        assertTrue(AssetCatalog.itemImagePath(UseCommand.MONEY_ITEM).endsWith("money_30yuan.png"));
    }

    @Test
    public void npcImagePathForVolunteerRooms() {
        assertTrue(AssetCatalog.npcImagePathForRoom("supermarket").endsWith("dorm_aunt.png"));
        assertTrue(AssetCatalog.npcImagePathForRoom("boxue_north").endsWith("volunteer.png"));
        assertTrue(AssetCatalog.npcImagePathForRoom("library").endsWith("volunteer.png"));
    }
}

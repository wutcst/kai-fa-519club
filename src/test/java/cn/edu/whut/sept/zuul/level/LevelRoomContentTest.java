package cn.edu.whut.sept.zuul.level;

import org.junit.Before;
import org.junit.Test;

import cn.edu.whut.sept.zuul.FoodItems;
import cn.edu.whut.sept.zuul.Game;
import cn.edu.whut.sept.zuul.Room;
import cn.edu.whut.sept.zuul.command.CombineCommand;
import cn.edu.whut.sept.zuul.command.UseCommand;
import cn.edu.whut.sept.zuul.unlock.UnlockService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * E16 按关公告与物品配置测试（干扰物与公告均按 1..当前关 累积合成）。
 */
public class LevelRoomContentTest {

    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    private void goToLevel(int level) {
        while (game.getLevelManager().getCurrentLevel() < level) {
            game.getLevelManager().completeCurrentLevel();
        }
    }

    @Test
    public void level1HasLevel1DistractionsOnly() {
        game.getLevelManager().startLevel(1);
        assertTrue(game.getRoomById("boxue_main").containsItem(FoodItems.MILK_TEA_ITEM));
        assertTrue(game.getRoomById("boxue_north").containsItem(UseCommand.MONEY_ITEM));
        assertFalse(game.getRoomById("gymnasium").containsItem("赛事纪念帽"));
    }

    @Test
    public void level2KeepsLevel1DistractionsAndAddsLevel2() {
        goToLevel(2);
        Room main = game.getRoomById("boxue_main");
        Room gym = game.getRoomById("gymnasium");
        assertTrue(main.containsItem(FoodItems.MILK_TEA_ITEM));
        assertTrue(main.containsItem("失物招领号码牌"));
        assertTrue(gym.containsItem("赛事纪念帽"));
        assertTrue(gym.containsItem("手电筒"));
        assertTrue(gym.getBulletin().contains("手电"));
        assertTrue(game.getRoomById("canteen").containsItem(UnlockService.CANTEEN_NOTE_ITEM));
    }

    @Test
    public void level3KeepsEarlierDistractionsAndAddsLevel3() {
        goToLevel(3);
        assertTrue(game.getRoomById("gymnasium").containsItem("赛事纪念帽"));
        assertTrue(game.getRoomById("boxue_main").containsItem(FoodItems.MILK_TEA_ITEM));
        assertTrue(game.getRoomById("boxue_west").containsItem(CombineCommand.STICK_ITEM));
        assertTrue(game.getRoomById("boxue_north").containsItem("晚安玛卡巴卡抱枕"));
        assertTrue(game.getRoomById("boxue_north").containsItem("志愿者马甲"));
        assertTrue(hasMagicCookieInRandomRoom());
        assertTrue(game.getRoomById("boxue_main").getBulletin().contains("停电"));
        assertTrue(game.getRoomById("boxue_north").getBulletin().contains("归寝单"));
        assertTrue(game.getRoomById("gymnasium").getBulletin().contains("手电"));
    }

    @Test
    public void level4KeepsEarlierDistractionsAndAddsLevel4() {
        goToLevel(4);
        assertTrue(game.getRoomById("boxue_north").containsItem("晚安玛卡巴卡抱枕"));
        assertTrue(hasMagicCookieInRandomRoom());
        assertTrue(game.getRoomById("gymnasium").containsItem(UseCommand.STOPWATCH_ITEM));
        assertTrue(game.getRoomById("gymnasium").containsItem("赛事纪念帽"));
        assertTrue(game.getRoomById("library").getBulletin().contains("2000年5月27日"));
    }

    @Test
    public void level5KeepsEarlierDistractionsAndAddsLevel5() {
        goToLevel(5);
        assertTrue(game.getRoomById("gymnasium").containsItem(UseCommand.STOPWATCH_ITEM));
        assertTrue(game.getRoomById("gymnasium").containsItem(UseCommand.FORTUNE_SLIP_ITEM));
        assertTrue(game.getRoomById("gate").containsItem("一份外卖"));
        assertFalse(game.getLevelManager().getCurrentLevelConfig().isMainBuildingDark());
        assertTrue(game.getRoomById("boxue_main").getBulletin().contains("一卡通与归寝单"));
        assertFalse(game.getRoomById("boxue_main").getBulletin().contains("停电"));
        assertTrue(game.getRoomById("library").getBulletin().contains("2000年5月27日"));
    }

    private boolean hasMagicCookieInRandomRoom() {
        for (String roomId : new String[]{"boxue_main", "boxue_east", "canteen"}) {
            Room room = game.getRoomById(roomId);
            if (room != null && room.containsItem(FoodItems.MAGIC_COOKIE)) {
                return true;
            }
        }
        return false;
    }
}

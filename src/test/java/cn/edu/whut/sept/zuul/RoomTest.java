package cn.edu.whut.sept.zuul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Room 类基础单元测试，用于 CI 冒烟验证。
 */
class RoomTest {

    @Test
    void createsRoomWithDescription() {
        Room room = new Room("in a test room");
        assertNotNull(room);
        assertEquals("in a test room", room.getShortDescription());
    }

    @Test
    void exitIsNullWhenNotSet() {
        Room room = new Room("empty room");
        assertNull(room.getExit("north"));
    }

    @Test
    void linksRoomsThroughExits() {
        Room a = new Room("room A");
        Room b = new Room("room B");
        a.setExit("east", b);
        assertEquals(b, a.getExit("east"));
    }
}

package cn.edu.whut.sept.zuul.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RoomLayoutDefaults 单元测试。
 */
public class RoomLayoutDefaultsTest {

    @Test
    public void gateHasAnchorsInUnitSquare() {
        double[][] anchors = RoomLayoutDefaults.getAnchors("gate");
        assertTrue(anchors.length >= 3);
        assertAnchorInRange(anchors[0]);
    }

    @Test
    public void unknownRoomUsesDefaultAnchors() {
        double[][] anchors = RoomLayoutDefaults.getAnchors("unknown_room");
        assertEquals(6, anchors.length);
    }

    @Test
    public void boxueWestHasEnoughSlotsForCombineItems() {
        double[][] anchors = RoomLayoutDefaults.getAnchors("boxue_west");
        assertTrue(anchors.length >= 5);
    }

    private void assertAnchorInRange(double[] anchor) {
        assertTrue(anchor[0] >= 0.0 && anchor[0] <= 1.0);
        assertTrue(anchor[1] >= 0.0 && anchor[1] <= 1.0);
    }
}

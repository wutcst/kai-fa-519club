package cn.edu.whut.sept.zuul.gui;

/**
 * 房间内物品锚点默认布局（相对坐标 0.0—1.0，F7 阶段 1）。
 */
public final class RoomLayoutDefaults {

    private static final double[][] DEFAULT_ANCHORS = {
        {0.18, 0.58},
        {0.38, 0.52},
        {0.58, 0.60},
        {0.72, 0.48},
        {0.82, 0.62},
        {0.45, 0.70}
    };

    private RoomLayoutDefaults() {
    }

    /**
     * 获取房间物品锚点。
     *
     * @param roomId 房间 ID
     * @return 锚点数组，每项为 {x, y}
     */
    public static double[][] getAnchors(String roomId) {
        if (roomId == null) {
            return copy(DEFAULT_ANCHORS);
        }
        switch (roomId) {
            case "gate":
                return anchors(
                    0.20, 0.65,
                    0.42, 0.58,
                    0.65, 0.52,
                    0.80, 0.68
                );
            case "boxue_main":
                return anchors(
                    0.15, 0.55,
                    0.35, 0.62,
                    0.55, 0.48,
                    0.70, 0.58,
                    0.85, 0.45
                );
            case "boxue_north":
                return anchors(
                    0.22, 0.60,
                    0.45, 0.55,
                    0.68, 0.50,
                    0.80, 0.65
                );
            case "supermarket":
                return anchors(
                    0.25, 0.58,
                    0.50, 0.52,
                    0.75, 0.60
                );
            case "dormitory":
                return anchors(
                    0.30, 0.55,
                    0.60, 0.62
                );
            case "boxue_west":
                return anchors(
                    0.18, 0.50,
                    0.38, 0.58,
                    0.58, 0.52,
                    0.75, 0.60,
                    0.85, 0.45
                );
            default:
                return copy(DEFAULT_ANCHORS);
        }
    }

    private static double[][] anchors(double... values) {
        double[][] result = new double[values.length / 2][2];
        for (int i = 0; i < values.length; i += 2) {
            result[i / 2][0] = values[i];
            result[i / 2][1] = values[i + 1];
        }
        return result;
    }

    private static double[][] copy(double[][] source) {
        double[][] copy = new double[source.length][2];
        for (int i = 0; i < source.length; i++) {
            copy[i][0] = source[i][0];
            copy[i][1] = source[i][1];
        }
        return copy;
    }
}

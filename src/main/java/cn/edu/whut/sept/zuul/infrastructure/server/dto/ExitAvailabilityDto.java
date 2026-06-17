package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 方向出口可用性 DTO。
 */
public class ExitAvailabilityDto {

    private boolean north;
    private boolean south;
    private boolean east;
    private boolean west;
    private boolean back;

    public boolean isNorth() {
        return north;
    }

    public void setNorth(boolean north) {
        this.north = north;
    }

    public boolean isSouth() {
        return south;
    }

    public void setSouth(boolean south) {
        this.south = south;
    }

    public boolean isEast() {
        return east;
    }

    public void setEast(boolean east) {
        this.east = east;
    }

    public boolean isWest() {
        return west;
    }

    public void setWest(boolean west) {
        this.west = west;
    }

    public boolean isBack() {
        return back;
    }

    public void setBack(boolean back) {
        this.back = back;
    }
}

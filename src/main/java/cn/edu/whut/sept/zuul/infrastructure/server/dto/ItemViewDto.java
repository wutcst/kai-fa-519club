package cn.edu.whut.sept.zuul.infrastructure.server.dto;

/**
 * 物品视图 DTO（GUI / Vue 展示用）。
 */
public class ItemViewDto {

    private String name;
    private int weight;
    private String longDescription;
    private boolean edible;

    public ItemViewDto() {
    }

    public ItemViewDto(String name, int weight, String longDescription, boolean edible) {
        this.name = name;
        this.weight = weight;
        this.longDescription = longDescription;
        this.edible = edible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public boolean isEdible() {
        return edible;
    }

    public void setEdible(boolean edible) {
        this.edible = edible;
    }
}

package app.db_proj.model;

public class Item {
    public final int itemId;
    public final String name;
    public final double price;
    public final double wholesalePrice;
    public final String itemType;
    public final String imagePath;
    public final Integer supplierId;
    public final String supplierName;

    public Item(int itemId, String name, double price, double wholesalePrice,
                String itemType, String imagePath, Integer supplierId, String supplierName) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.wholesalePrice = wholesalePrice;
        this.itemType = itemType;
        this.imagePath = imagePath;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }
}

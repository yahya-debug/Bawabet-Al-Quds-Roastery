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
    public final double branchQuantity; // 0 when not branch-specific

    public Item(int itemId, String name, double price, double wholesalePrice,
                String itemType, String imagePath, Integer supplierId, String supplierName) {
        this(itemId, name, price, wholesalePrice, itemType, imagePath, supplierId, supplierName, 0.0);
    }

    public Item(int itemId, String name, double price, double wholesalePrice,
                String itemType, String imagePath, Integer supplierId, String supplierName, double branchQuantity) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.wholesalePrice = wholesalePrice;
        this.itemType = itemType;
        this.imagePath = imagePath;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.branchQuantity = branchQuantity;
    }
}

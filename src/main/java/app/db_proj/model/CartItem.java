package app.db_proj.model;

public class CartItem {
    public final int itemId;
    public final String name;
    public final double price;
    public final int quantity;
    public final String itemType;
    public final String imagePath;

    public CartItem(int itemId, String name, double price, int quantity,
                    String itemType, String imagePath) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.itemType = itemType;
        this.imagePath = imagePath;
    }

    public double subtotal() {
        return price * quantity;
    }
}

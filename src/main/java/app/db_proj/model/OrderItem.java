package app.db_proj.model;

public class OrderItem {
    public final int orderId;
    public final int itemId;
    public final String itemName;
    public final int quantity;
    public final double unitPrice;

    public OrderItem(int orderId, int itemId, String itemName, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double subtotal() {
        return quantity * unitPrice;
    }
}

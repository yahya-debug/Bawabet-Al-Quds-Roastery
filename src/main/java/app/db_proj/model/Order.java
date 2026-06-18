package app.db_proj.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    public final int orderId;
    public final int personId;
    public final String personName;
    public final LocalDateTime orderDate;
    public final String status;
    public final double total;
    public final List<OrderItem> items;

    public Order(int orderId, int personId, String personName,
                 LocalDateTime orderDate, String status, double total,
                 List<OrderItem> items) {
        this.orderId = orderId;
        this.personId = personId;
        this.personName = personName;
        this.orderDate = orderDate;
        this.status = status;
        this.total = total;
        this.items = items;
    }
}

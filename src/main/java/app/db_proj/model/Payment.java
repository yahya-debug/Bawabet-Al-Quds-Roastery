package app.db_proj.model;

import java.time.LocalDateTime;

public class Payment {
    public final int paymentId;
    public final int orderId;
    public final double amount;
    public final String method;
    public final LocalDateTime paidAt;

    public Payment(int paymentId, int orderId, double amount, String method, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
    }
}

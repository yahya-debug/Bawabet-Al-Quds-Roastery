package app.db_proj;

import app.db_proj.model.Order;
import app.db_proj.model.OrderItem;
import app.db_proj.model.Payment;
import app.db_proj.model.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // place an order from the customer's current cart; returns the new order_id or -1 on failure
    public static int placeOrder(Connection conn, int personId) {
        try {
            conn.setAutoCommit(false);

            // sum the cart total first
            PreparedStatement totalPs = conn.prepareStatement(
                "SELECT SUM(I.price * C.quantity) AS total " +
                "FROM Cart C JOIN Item I ON C.item_id = I.item_id WHERE C.person_id = ?"
            );
            totalPs.setInt(1, personId);
            ResultSet totalRs = totalPs.executeQuery();
            double total = totalRs.next() ? totalRs.getDouble("total") : 0;

            // create the order row
            PreparedStatement orderPs = conn.prepareStatement(
                "INSERT INTO `Order` (person_id, total) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            orderPs.setInt(1, personId);
            orderPs.setDouble(2, total);
            orderPs.executeUpdate();
            ResultSet keys = orderPs.getGeneratedKeys();
            if (!keys.next()) { conn.rollback(); conn.setAutoCommit(true); return -1; }
            int orderId = keys.getInt(1);

            // copy cart rows into OrderItem using their current prices
            PreparedStatement itemsPs = conn.prepareStatement(
                "INSERT INTO OrderItem (order_id, item_id, quantity, unit_price) " +
                "SELECT ?, C.item_id, C.quantity, I.price " +
                "FROM Cart C JOIN Item I ON C.item_id = I.item_id WHERE C.person_id = ?"
            );
            itemsPs.setInt(1, orderId);
            itemsPs.setInt(2, personId);
            itemsPs.executeUpdate();

            // clear the cart
            PreparedStatement clearPs = conn.prepareStatement("DELETE FROM Cart WHERE person_id = ?");
            clearPs.setInt(1, personId);
            clearPs.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return orderId;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return -1;
        }
    }

    // fetch orders for a specific customer, newest first, with each order's items
    public static List<Order> getOrdersForPerson(Connection conn, int personId) {
        List<Order> orders = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT O.order_id, O.person_id, P.name AS person_name, " +
                "       O.order_date, O.status, O.total " +
                "FROM `Order` O JOIN Person P ON O.person_id = P.person_id " +
                "WHERE O.person_id = ? ORDER BY O.order_date DESC"
            );
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                orders.add(new Order(
                    orderId,
                    rs.getInt("person_id"),
                    rs.getString("person_name"),
                    rs.getTimestamp("order_date").toLocalDateTime(),
                    rs.getString("status"),
                    rs.getDouble("total"),
                    getOrderItems(conn, orderId)
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return orders;
    }

    // fetch all orders (admin view), newest first
    public static List<Order> getAllOrders(Connection conn) {
        List<Order> orders = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT O.order_id, O.person_id, P.name AS person_name, " +
                "       O.order_date, O.status, O.total " +
                "FROM `Order` O JOIN Person P ON O.person_id = P.person_id " +
                "ORDER BY O.order_date DESC"
            );
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                orders.add(new Order(
                    orderId,
                    rs.getInt("person_id"),
                    rs.getString("person_name"),
                    rs.getTimestamp("order_date").toLocalDateTime(),
                    rs.getString("status"),
                    rs.getDouble("total"),
                    getOrderItems(conn, orderId)
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return orders;
    }

    // fetch line items for one order, joined with item name
    public static List<OrderItem> getOrderItems(Connection conn, int orderId) {
        List<OrderItem> items = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT OI.order_id, OI.item_id, I.name AS item_name, OI.quantity, OI.unit_price " +
                "FROM OrderItem OI JOIN Item I ON OI.item_id = I.item_id " +
                "WHERE OI.order_id = ?"
            );
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new OrderItem(
                    rs.getInt("order_id"),
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return items;
    }

    // update order status (e.g. "pending" → "shipped" → "delivered")
    public static boolean updateOrderStatus(Connection conn, int orderId, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE `Order` SET status = ? WHERE order_id = ?"
            );
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // record a payment for an order
    public static boolean recordPayment(Connection conn, int orderId, double amount, String method) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Payment (order_id, amount, method) VALUES (?, ?, ?)"
            );
            ps.setInt(1, orderId);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // fetch payments for an order
    public static List<Payment> getPayments(Connection conn, int orderId) {
        List<Payment> payments = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT payment_id, order_id, amount, method, paid_at FROM Payment WHERE order_id = ?"
            );
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                payments.add(new Payment(
                    rs.getInt("payment_id"),
                    rs.getInt("order_id"),
                    rs.getDouble("amount"),
                    rs.getString("method"),
                    rs.getTimestamp("paid_at").toLocalDateTime()
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return payments;
    }

    // submit a review for an item; one review per person per item enforced at DB level
    public static boolean addReview(Connection conn, int personId, int itemId, int rating, String comment) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Review (person_id, item_id, rating, comment) VALUES (?, ?, ?, ?)"
            );
            ps.setInt(1, personId);
            ps.setInt(2, itemId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // fetch reviews for one item, joined with reviewer name, newest first
    public static List<Review> getReviewsForItem(Connection conn, int itemId) {
        List<Review> reviews = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT R.review_id, R.person_id, P.name AS person_name, " +
                "       R.item_id, I.name AS item_name, R.rating, R.comment, R.review_date " +
                "FROM Review R " +
                "JOIN Person P ON R.person_id = P.person_id " +
                "JOIN Item I   ON R.item_id   = I.item_id " +
                "WHERE R.item_id = ? ORDER BY R.review_date DESC"
            );
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(new Review(
                    rs.getInt("review_id"),
                    rs.getInt("person_id"),
                    rs.getString("person_name"),
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getInt("rating"),
                    rs.getString("comment"),
                    rs.getTimestamp("review_date").toLocalDateTime()
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return reviews;
    }
}

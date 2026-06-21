package app.db_proj;

import app.db_proj.model.Order;
import app.db_proj.model.OrderItem;
import app.db_proj.model.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Place orders from cart, one per branch. Returns number of orders created, or -1 on failure.
    public static int placeOrder(Connection conn, int personId) {
        try {
            conn.setAutoCommit(false);

            // Collect cart lines grouped by branch_id
            PreparedStatement cartPs = conn.prepareStatement(
                "SELECT C.item_id, C.quantity, COALESCE(C.branch_id, 0) AS branch_id, I.price " +
                "FROM Cart C JOIN Item I ON C.item_id = I.item_id WHERE C.person_id = ?"
            );
            cartPs.setInt(1, personId);
            ResultSet cartRs = cartPs.executeQuery();

            // Map<branchId, list of {itemId, qty}> and Map<branchId, Map<itemId, price>>
            Map<Integer, List<Object[]>>       linesByBranch = new LinkedHashMap<>();
            Map<Integer, Map<Integer, Double>> priceMap      = new LinkedHashMap<>();
            Map<Integer, Double>               totalByBranch = new LinkedHashMap<>();

            while (cartRs.next()) {
                int    itemId = cartRs.getInt("item_id");
                double qty    = cartRs.getDouble("quantity");
                int    bId    = cartRs.getInt("branch_id");
                double price  = cartRs.getDouble("price");
                linesByBranch.computeIfAbsent(bId, k -> new ArrayList<>()).add(new Object[]{itemId, qty});
                priceMap.computeIfAbsent(bId, k -> new HashMap<>()).put(itemId, price);
                totalByBranch.merge(bId, price * qty, Double::sum);
            }

            if (linesByBranch.isEmpty()) { conn.setAutoCommit(true); return -1; }

            int ordersCreated = 0;

            for (Map.Entry<Integer, List<Object[]>> entry : linesByBranch.entrySet()) {
                int branchId = entry.getKey();
                List<Object[]> lines = entry.getValue();
                double total = totalByBranch.get(branchId);

                PreparedStatement orderPs = conn.prepareStatement(
                    "INSERT INTO `Order` (person_id, branch_id, total) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                orderPs.setInt(1, personId);
                if (branchId > 0) orderPs.setInt(2, branchId);
                else orderPs.setNull(2, Types.INTEGER);
                orderPs.setDouble(3, total);
                orderPs.executeUpdate();
                ResultSet keys = orderPs.getGeneratedKeys();
                if (!keys.next()) { conn.rollback(); conn.setAutoCommit(true); return -1; }
                int orderId = keys.getInt(1);

                Map<Integer, Double> prices = priceMap.get(branchId);
                for (Object[] line : lines) {
                    int    lineItemId = (int)    line[0];
                    double lineQty    = (double) line[1];
                    PreparedStatement itemPs = conn.prepareStatement(
                        "INSERT INTO OrderItem (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)"
                    );
                    itemPs.setInt(1, orderId);
                    itemPs.setInt(2, lineItemId);
                    itemPs.setDouble(3, lineQty);
                    itemPs.setDouble(4, prices.get(lineItemId));
                    itemPs.executeUpdate();
                }

                // create delivery record (status defaults to 'pending')
                PreparedStatement delivPs = conn.prepareStatement(
                    "INSERT INTO Delivery (order_id, address) VALUES (?, ?)"
                );
                delivPs.setInt(1, orderId);
                String addr = branchId > 0
                    ? "Branch delivery – branch #" + branchId
                    : "Pickup";
                delivPs.setString(2, addr);
                delivPs.executeUpdate();

                ordersCreated++;
            }

            // clear the cart
            PreparedStatement clearPs = conn.prepareStatement("DELETE FROM Cart WHERE person_id = ?");
            clearPs.setInt(1, personId);
            clearPs.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return ordersCreated;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return -1;
        }
    }

    public static boolean hasPurchased(Connection conn, int personId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM OrderItem OI " +
                "JOIN `Order` O ON OI.order_id = O.order_id " +
                "WHERE O.person_id = ? AND OI.item_id = ? AND O.status = 'delivered' LIMIT 1"
            );
            ps.setInt(1, personId);
            ps.setInt(2, itemId);
            return ps.executeQuery().next();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return false;
    }

    public static boolean hasReviewed(Connection conn, int personId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM Review WHERE person_id = ? AND item_id = ? LIMIT 1"
            );
            ps.setInt(1, personId);
            ps.setInt(2, itemId);
            return ps.executeQuery().next();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return false;
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

    // fetch orders for a specific branch (employee view), newest first
    public static List<Order> getOrdersByBranch(Connection conn, int branchId) {
        List<Order> orders = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT O.order_id, O.person_id, P.name AS person_name, " +
                "       O.order_date, O.status, O.total " +
                "FROM `Order` O JOIN Person P ON O.person_id = P.person_id " +
                "WHERE O.branch_id = ? ORDER BY O.order_date DESC"
            );
            ps.setInt(1, branchId);
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

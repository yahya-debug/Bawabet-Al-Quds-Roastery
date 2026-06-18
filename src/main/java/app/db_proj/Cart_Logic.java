package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Cart_Logic {

    public static class CartItemRow {
        private final int itemId;
        private final String name;
        private final double price;
        private final int quantity;
        private final String itemType;
        private final String imagePath;

        public CartItemRow(int itemId, String name, double price, int quantity, String itemType, String imagePath) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.itemType = itemType;
            this.imagePath = imagePath;
        }

        public int getItemId()      { return itemId; }
        public String getName()     { return name; }
        public double getPrice()    { return price; }
        public int getQuantity()    { return quantity; }
        public String getItemType() { return itemType; }
        public String getImagePath() { return imagePath; }
    }

    public static void ensureCartTable(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Cart (" +
                "person_id INT NOT NULL, " +
                "item_id   INT NOT NULL, " +
                "quantity  INT NOT NULL DEFAULT 1, " +
                "PRIMARY KEY (person_id, item_id), " +
                "FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (item_id)   REFERENCES Item(item_id)       ON DELETE CASCADE" +
                ")"
            );
        } catch (SQLException ex) {
            System.out.println("Cart table init: " + ex.getMessage());
        }
    }

    public static List<CartItemRow> getCartItems(Connection conn, int userId) {
        return query(conn, userId, null);
    }

    public static List<CartItemRow> searchCartItems(Connection conn, int userId, String search) {
        return query(conn, userId, search);
    }

    private static List<CartItemRow> query(Connection conn, int userId, String search) {
        List<CartItemRow> list = new ArrayList<>();
        try {
            String sql = "SELECT I.item_id, I.name, I.price, C.quantity, I.item_type, I.image_path " +
                         "FROM Cart C JOIN Item I ON C.item_id = I.item_id WHERE C.person_id = ?";
            if (search != null && !search.isBlank()) sql += " AND I.name LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            if (search != null && !search.isBlank()) ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new CartItemRow(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("item_type"),
                    rs.getString("image_path")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    public static void addToCart(Connection conn, int userId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Cart (person_id, item_id, quantity) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + 1"
            );
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void removeFromCart(Connection conn, int userId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Cart WHERE person_id = ? AND item_id = ?"
            );
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static double getCartTotal(Connection conn, int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT SUM(I.price * C.quantity) AS total " +
                "FROM Cart C JOIN Item I ON C.item_id = I.item_id WHERE C.person_id = ?"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0.0;
    }
}

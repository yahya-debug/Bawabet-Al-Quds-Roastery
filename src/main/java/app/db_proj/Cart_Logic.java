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
        private final int branchId; // 0 = no specific branch

        public CartItemRow(int itemId, String name, double price, int quantity,
                           String itemType, String imagePath, int branchId) {
            this.itemId   = itemId;
            this.name     = name;
            this.price    = price;
            this.quantity = quantity;
            this.itemType = itemType;
            this.imagePath = imagePath;
            this.branchId = branchId;
        }

        public int getItemId()       { return itemId; }
        public String getName()      { return name; }
        public double getPrice()     { return price; }
        public int getQuantity()     { return quantity; }
        public String getItemType()  { return itemType; }
        public String getImagePath() { return imagePath; }
        public int getBranchId()     { return branchId; }
    }

    public static void ensureCartTable(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Cart (" +
                "person_id INT NOT NULL, " +
                "item_id   INT NOT NULL, " +
                "quantity  INT NOT NULL DEFAULT 1, " +
                "branch_id INT NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (person_id, item_id), " +
                "FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (item_id)   REFERENCES Item(item_id)       ON DELETE CASCADE" +
                ")"
            );
        } catch (SQLException ex) {
            System.out.println("Cart table init: " + ex.getMessage());
        }
        // add branch_id if it was missing from an older schema
        try {
            conn.createStatement().executeUpdate(
                "ALTER TABLE Cart ADD COLUMN IF NOT EXISTS branch_id INT NOT NULL DEFAULT 0"
            );
        } catch (SQLException ignored) {}
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
            String sql = "SELECT I.item_id, I.name, I.price, C.quantity, I.item_type, I.image_path, " +
                         "       COALESCE(C.branch_id, 0) AS branch_id " +
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
                    rs.getString("image_path"),
                    rs.getInt("branch_id")
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

    public static int getCartQuantity(Connection conn, int userId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT quantity FROM Cart WHERE person_id = ? AND item_id = ?"
            );
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("quantity");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }

    // branchId: the branch the item is being purchased from (0 = none)
    // Adjusts BranchInventory by the delta between old and new quantity.
    public static void setCartQuantity(Connection conn, int userId, int itemId, int qty, int branchId) {
        try {
            // read current state before changing anything
            int oldQty      = 0;
            int oldBranchId = 0;
            PreparedStatement getPs = conn.prepareStatement(
                "SELECT quantity, COALESCE(branch_id, 0) AS branch_id FROM Cart WHERE person_id = ? AND item_id = ?"
            );
            getPs.setInt(1, userId);
            getPs.setInt(2, itemId);
            ResultSet rs = getPs.executeQuery();
            if (rs.next()) {
                oldQty      = rs.getInt("quantity");
                oldBranchId = rs.getInt("branch_id");
            }

            int effectiveBranchId = Math.max(branchId, 0);

            if (qty <= 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Cart WHERE person_id = ? AND item_id = ?"
                );
                ps.setInt(1, userId);
                ps.setInt(2, itemId);
                ps.executeUpdate();
                // refill entire old quantity back to inventory
                if (oldQty > 0 && oldBranchId > 0)
                    adjustInventory(conn, oldBranchId, itemId, oldQty);
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Cart (person_id, item_id, quantity, branch_id) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = ?, branch_id = ?"
                );
                ps.setInt(1, userId); ps.setInt(2, itemId);
                ps.setInt(3, qty);    ps.setInt(4, effectiveBranchId);
                ps.setInt(5, qty);    ps.setInt(6, effectiveBranchId);
                ps.executeUpdate();

                if (effectiveBranchId > 0) {
                    if (oldBranchId != effectiveBranchId && oldQty > 0) {
                        // branch changed: return stock to old branch, take from new branch
                        if (oldBranchId > 0) adjustInventory(conn, oldBranchId, itemId, oldQty);
                        adjustInventory(conn, effectiveBranchId, itemId, -qty);
                    } else {
                        // same branch: subtract the net difference
                        int diff = qty - oldQty;
                        if (diff != 0) adjustInventory(conn, effectiveBranchId, itemId, -diff);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void removeFromCart(Connection conn, int userId, int itemId) {
        try {
            // read before deleting so we can refill inventory
            int oldQty      = 0;
            int oldBranchId = 0;
            PreparedStatement getPs = conn.prepareStatement(
                "SELECT quantity, COALESCE(branch_id, 0) AS branch_id FROM Cart WHERE person_id = ? AND item_id = ?"
            );
            getPs.setInt(1, userId);
            getPs.setInt(2, itemId);
            ResultSet rs = getPs.executeQuery();
            if (rs.next()) {
                oldQty      = rs.getInt("quantity");
                oldBranchId = rs.getInt("branch_id");
            }

            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Cart WHERE person_id = ? AND item_id = ?"
            );
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.executeUpdate();

            // return stock to inventory (only for cart removals, not for order placements)
            if (oldQty > 0 && oldBranchId > 0)
                adjustInventory(conn, oldBranchId, itemId, oldQty);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    // delta > 0 refills stock, delta < 0 subtracts — never goes below 0
    private static void adjustInventory(Connection conn, int branchId, int itemId, int delta) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE BranchInventory SET quantity = GREATEST(0, quantity + ?) " +
                "WHERE branch_id = ? AND item_id = ?"
            );
            ps.setInt(1, delta);
            ps.setInt(2, branchId);
            ps.setInt(3, itemId);
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

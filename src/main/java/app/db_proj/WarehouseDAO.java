package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    public static class WarehouseRow {
        public final int    warehouseId;
        public final String name;
        public final String street, city, zip;

        public WarehouseRow(int warehouseId, String name, String street, String city, String zip) {
            this.warehouseId = warehouseId;
            this.name = name;
            this.street = street;
            this.city   = city;
            this.zip    = zip;
        }

        @Override public String toString() { return name; }
    }

    public static class StockRow {
        public final int    itemId;
        public final String itemName;
        public final int    quantity;

        public StockRow(int itemId, String itemName, int quantity) {
            this.itemId   = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }

    public static void ensureTables(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Warehouse (" +
                "  warehouse_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  location_id  INT NOT NULL," +
                "  name         VARCHAR(100) NOT NULL," +
                "  FOREIGN KEY (location_id) REFERENCES Location(location_id) ON DELETE NO ACTION" +
                ")"
            );
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS WarehouseInventory (" +
                "  warehouse_id INT NOT NULL," +
                "  item_id      INT NOT NULL," +
                "  quantity     INT NOT NULL DEFAULT 0," +
                "  PRIMARY KEY (warehouse_id, item_id)," +
                "  FOREIGN KEY (warehouse_id) REFERENCES Warehouse(warehouse_id) ON DELETE CASCADE," +
                "  FOREIGN KEY (item_id)      REFERENCES Item(item_id)           ON DELETE CASCADE" +
                ")"
            );
        } catch (SQLException ex) {
            System.out.println("WarehouseDAO.ensureTables: " + ex.getMessage());
        }
    }

    public static WarehouseRow getById(Connection conn, int warehouseId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT W.warehouse_id, W.name, L.street, L.city, L.zip " +
                "FROM Warehouse W JOIN Location L ON W.location_id = L.location_id " +
                "WHERE W.warehouse_id = ?"
            );
            ps.setInt(1, warehouseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new WarehouseRow(rs.getInt("warehouse_id"), rs.getString("name"),
                    rs.getString("street"), rs.getString("city"), rs.getString("zip"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return null;
    }

    public static List<WarehouseRow> getAll(Connection conn) {
        List<WarehouseRow> list = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT W.warehouse_id, W.name, L.street, L.city, L.zip " +
                "FROM Warehouse W JOIN Location L ON W.location_id = L.location_id " +
                "ORDER BY W.name"
            );
            while (rs.next())
                list.add(new WarehouseRow(rs.getInt("warehouse_id"), rs.getString("name"),
                    rs.getString("street"), rs.getString("city"), rs.getString("zip")));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    public static List<StockRow> getStock(Connection conn, int warehouseId) {
        List<StockRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT I.item_id, I.name, WI.quantity " +
                "FROM WarehouseInventory WI JOIN Item I ON WI.item_id = I.item_id " +
                "WHERE WI.warehouse_id = ? ORDER BY I.name"
            );
            ps.setInt(1, warehouseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new StockRow(rs.getInt("item_id"), rs.getString("name"), rs.getInt("quantity")));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // total quantity of an item across all warehouses
    public static int getTotalStock(Connection conn, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(quantity), 0) AS total FROM WarehouseInventory WHERE item_id = ?"
            );
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return 0;
    }

    public static String addWarehouse(Connection conn, String name, String street, String city, String zip) {
        if (name.isBlank() || street.isBlank() || city.isBlank() || zip.isBlank()) return "empty";
        try {
            PreparedStatement locPs = conn.prepareStatement(
                "INSERT INTO Location (street, city, zip) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            locPs.setString(1, street); locPs.setString(2, city); locPs.setString(3, zip);
            locPs.executeUpdate();
            ResultSet keys = locPs.getGeneratedKeys();
            if (!keys.next()) return "error";
            int locationId = keys.getInt(1);

            PreparedStatement wPs = conn.prepareStatement(
                "INSERT INTO Warehouse (name, location_id) VALUES (?, ?)"
            );
            wPs.setString(1, name); wPs.setInt(2, locationId);
            wPs.executeUpdate();
            return "ok";
        } catch (SQLException ex) { System.out.println(ex.getMessage()); return "error"; }
    }

    // set or update qty for an item in a warehouse
    public static void setStock(Connection conn, int warehouseId, int itemId, int qty) {
        try {
            if (qty <= 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM WarehouseInventory WHERE warehouse_id = ? AND item_id = ?"
                );
                ps.setInt(1, warehouseId); ps.setInt(2, itemId);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO WarehouseInventory (warehouse_id, item_id, quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = ?"
                );
                ps.setInt(1, warehouseId); ps.setInt(2, itemId);
                ps.setInt(3, qty);         ps.setInt(4, qty);
                ps.executeUpdate();
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
    }

    // transfer qty units from warehouse to branch; returns "ok", "insufficient", or "error"
    public static String transferToBranch(Connection conn, int warehouseId, int branchId, int itemId, int qty) {
        if (qty <= 0) return "error";
        try {
            conn.setAutoCommit(false);

            // check warehouse has enough
            PreparedStatement checkPs = conn.prepareStatement(
                "SELECT quantity FROM WarehouseInventory WHERE warehouse_id = ? AND item_id = ?"
            );
            checkPs.setInt(1, warehouseId); checkPs.setInt(2, itemId);
            ResultSet rs = checkPs.executeQuery();
            int available = rs.next() ? rs.getInt("quantity") : 0;
            if (available < qty) { conn.setAutoCommit(true); return "insufficient"; }

            // deduct from warehouse
            PreparedStatement deductPs = conn.prepareStatement(
                "UPDATE WarehouseInventory SET quantity = GREATEST(0, quantity - ?) " +
                "WHERE warehouse_id = ? AND item_id = ?"
            );
            deductPs.setInt(1, qty); deductPs.setInt(2, warehouseId); deductPs.setInt(3, itemId);
            deductPs.executeUpdate();

            // add to branch
            PreparedStatement addPs = conn.prepareStatement(
                "INSERT INTO BranchInventory (branch_id, item_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + ?"
            );
            addPs.setInt(1, branchId); addPs.setInt(2, itemId);
            addPs.setInt(3, qty);      addPs.setInt(4, qty);
            addPs.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return "error";
        }
    }
}

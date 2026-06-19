package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO {

    public static class PackageRow {
        public final int packageId;
        public final String name;
        public final String description;
        public final double price;
        public final List<PackageItemRow> contents;

        public PackageRow(int packageId, String name, String description,
                          double price, List<PackageItemRow> contents) {
            this.packageId = packageId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.contents = contents;
        }
    }

    public static class PackageItemRow {
        public final int itemId;
        public final String itemName;
        public final int quantity;
        public final double unitPrice;

        public PackageItemRow(int itemId, String itemName, int quantity, double unitPrice) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    // Fetch all packages with their item lists
    public static List<PackageRow> getAll(Connection conn) {
        List<PackageRow> list = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT P.package_id, P.name, P.description, I.price " +
                "FROM Package P JOIN Item I ON P.package_id = I.item_id " +
                "ORDER BY P.name"
            );
            while (rs.next()) {
                int pid = rs.getInt("package_id");
                list.add(new PackageRow(
                    pid,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    getContents(conn, pid)
                ));
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // Fetch the items inside one package
    public static List<PackageItemRow> getContents(Connection conn, int packageId) {
        List<PackageItemRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT PI.item_id, I.name AS item_name, PI.quantity, I.price " +
                "FROM PackageItem PI JOIN Item I ON PI.item_id = I.item_id " +
                "WHERE PI.package_id = ?"
            );
            ps.setInt(1, packageId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new PackageItemRow(
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                ));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // Create a package: inserts an Item row then a Package row, returns packageId or -1
    public static int createPackage(Connection conn, String name, String description, String priceStr) {
        if (name.isBlank() || priceStr.isBlank()) return -1;
        try {
            double price = Double.parseDouble(priceStr);
            // Package IS an Item (ISA) — insert base Item row first
            PreparedStatement itemPs = conn.prepareStatement(
                "INSERT INTO Item (name, item_type, price) VALUES (?, 'Package', ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            itemPs.setString(1, name);
            itemPs.setDouble(2, price);
            itemPs.executeUpdate();
            ResultSet keys = itemPs.getGeneratedKeys();
            if (!keys.next()) return -1;
            int packageId = keys.getInt(1);

            PreparedStatement pkgPs = conn.prepareStatement(
                "INSERT INTO Package (package_id, name, description) VALUES (?, ?, ?)"
            );
            pkgPs.setInt(1, packageId);
            pkgPs.setString(2, name);
            pkgPs.setString(3, description.isBlank() ? null : description);
            pkgPs.executeUpdate();
            return packageId;
        } catch (NumberFormatException ex) {
            return -2; // price_error
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return -1;
        }
    }

    // Add an item to a package (or update quantity if already there)
    public static boolean addItemToPackage(Connection conn, int packageId, int itemId, int quantity) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO PackageItem (package_id, item_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = ?"
            );
            ps.setInt(1, packageId);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // Check if an item is a package
    public static boolean isPackage(Connection conn, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM Package WHERE package_id = ?"
            );
            ps.setInt(1, itemId);
            return ps.executeQuery().next();
        } catch (SQLException ex) { return false; }
    }
}

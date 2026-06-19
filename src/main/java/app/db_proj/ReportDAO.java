package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced reporting queries used by the Reports screen.
 * Every method returns a Map or List suited for direct chart binding.
 */
public class ReportDAO {

    // Revenue split by item category  (PieChart)
    public static Map<String, Double> revenueByCategory(Connection conn) {
        Map<String, Double> map = new LinkedHashMap<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT I.item_type AS cat, SUM(OI.quantity * OI.unit_price) AS rev " +
                "FROM OrderItem OI JOIN Item I ON OI.item_id = I.item_id " +
                "GROUP BY I.item_type ORDER BY rev DESC"
            );
            while (rs.next()) {
                String cat = rs.getString("cat");
                map.put(cat != null ? cat : "Other", rs.getDouble("rev"));
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Top N best-selling items by total units sold  (BarChart)
    public static Map<String, Integer> topSellingItems(Connection conn, int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT I.name, SUM(OI.quantity) AS sold " +
                "FROM OrderItem OI JOIN Item I ON OI.item_id = I.item_id " +
                "GROUP BY I.item_id, I.name ORDER BY sold DESC LIMIT ?"
            );
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("sold"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Monthly revenue for the last 12 months  (BarChart)
    public static Map<String, Double> monthlyRevenue(Connection conn) {
        Map<String, Double> map = new LinkedHashMap<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT DATE_FORMAT(order_date, '%Y-%m') AS mo, SUM(total) AS rev " +
                "FROM `Order` " +
                "WHERE order_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                "GROUP BY mo ORDER BY mo"
            );
            while (rs.next()) map.put(rs.getString("mo"), rs.getDouble("rev"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Order count grouped by status  (PieChart)
    public static Map<String, Integer> ordersByStatus(Connection conn) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT status, COUNT(*) AS cnt FROM `Order` GROUP BY status"
            );
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("cnt"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Revenue split by customer type (Individual vs Business)  (BarChart)
    public static Map<String, Double> revenueByCustomerType(Connection conn) {
        Map<String, Double> map = new LinkedHashMap<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT C.type, SUM(O.total) AS rev " +
                "FROM `Order` O JOIN Customer C ON O.person_id = C.person_id " +
                "GROUP BY C.type ORDER BY rev DESC"
            );
            while (rs.next()) map.put(rs.getString("type"), rs.getDouble("rev"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Top wholesale/B2B customers by spending  (BarChart)
    public static Map<String, Double> topBusinessCustomers(Connection conn, int limit) {
        Map<String, Double> map = new LinkedHashMap<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT P.name, SUM(O.total) AS spent " +
                "FROM `Order` O " +
                "JOIN Person   P ON O.person_id = P.person_id " +
                "JOIN Customer C ON O.person_id = C.person_id " +
                "WHERE C.type = 'business' " +
                "GROUP BY O.person_id, P.name ORDER BY spent DESC LIMIT ?"
            );
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("name"), rs.getDouble("spent"));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return map;
    }

    // Summary stats: total revenue, order count, avg order value
    public static double[] summaryStats(Connection conn) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT SUM(total) AS total_rev, COUNT(*) AS cnt, AVG(total) AS avg_val " +
                "FROM `Order`"
            );
            if (rs.next()) return new double[]{
                rs.getDouble("total_rev"),
                rs.getDouble("cnt"),
                rs.getDouble("avg_val")
            };
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return new double[]{0, 0, 0};
    }

    // Items with stock below a threshold (for admin low-stock alerts)
    public static class StockRow {
        public final String itemName;
        public final String branchName;
        public final int quantity;
        public StockRow(String itemName, String branchName, int quantity) {
            this.itemName = itemName; this.branchName = branchName; this.quantity = quantity;
        }
    }

    public static List<StockRow> lowStock(Connection conn, int threshold) {
        List<StockRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT I.name AS item_name, B.branch_name, BI.quantity " +
                "FROM BranchInventory BI " +
                "JOIN Item   I ON BI.item_id   = I.item_id " +
                "JOIN Branch B ON BI.branch_id = B.branch_id " +
                "WHERE BI.quantity <= ? ORDER BY BI.quantity ASC"
            );
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new StockRow(rs.getString("item_name"), rs.getString("branch_name"), rs.getInt("quantity")));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }
}

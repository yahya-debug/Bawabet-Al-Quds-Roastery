package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoastBatchDAO {

    public static final String[] ROAST_LEVELS = {"Light", "Medium-Light", "Medium", "Medium-Dark", "Dark"};
    public static final String[] ITEM_TYPES    = {"Coffee", "Roasts", "Spice", "Package"};

    public static class RoastBatchRow {
        public final int    batchId;
        public final int    branchId;
        public final String branchName;
        public final String roastDate;
        public final double kgGreen;
        public final double kgRoasted;
        public final String roastLevel;

        public RoastBatchRow(int batchId, int branchId, String branchName,
                             String roastDate, double kgGreen, double kgRoasted, String roastLevel) {
            this.batchId    = batchId;
            this.branchId   = branchId;
            this.branchName = branchName;
            this.roastDate  = roastDate;
            this.kgGreen    = kgGreen;
            this.kgRoasted  = kgRoasted;
            this.roastLevel = roastLevel;
        }
    }

    public static void ensureTables(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS RoastBatch (" +
                "  batch_id    INT AUTO_INCREMENT PRIMARY KEY," +
                "  branch_id   INT NOT NULL," +
                "  roast_date  DATE NOT NULL," +
                "  kg_green    DECIMAL(8,2) NOT NULL," +
                "  kg_roasted  DECIMAL(8,2) NOT NULL," +
                "  roast_level VARCHAR(20) NOT NULL," +
                "  FOREIGN KEY (branch_id) REFERENCES Branch(branch_id) ON DELETE CASCADE" +
                ")"
            );
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Coffee (" +
                "  item_id  INT PRIMARY KEY," +
                "  batch_id INT," +
                "  FOREIGN KEY (item_id)  REFERENCES Item(item_id)       ON DELETE CASCADE," +
                "  FOREIGN KEY (batch_id) REFERENCES RoastBatch(batch_id) ON DELETE SET NULL" +
                ")"
            );
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Roasts (" +
                "  item_id  INT PRIMARY KEY," +
                "  batch_id INT," +
                "  FOREIGN KEY (item_id)  REFERENCES Item(item_id)       ON DELETE CASCADE," +
                "  FOREIGN KEY (batch_id) REFERENCES RoastBatch(batch_id) ON DELETE SET NULL" +
                ")"
            );
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Spice (" +
                "  item_id INT PRIMARY KEY," +
                "  FOREIGN KEY (item_id) REFERENCES Item(item_id) ON DELETE CASCADE" +
                ")"
            );
        } catch (SQLException ex) {
            System.out.println("RoastBatchDAO.ensureTables: " + ex.getMessage());
        }
    }

    public static List<RoastBatchRow> getByBranch(Connection conn, int branchId) {
        return query(conn,
            "SELECT RB.batch_id, RB.branch_id, B.branch_name, RB.roast_date, " +
            "       RB.kg_green, RB.kg_roasted, RB.roast_level " +
            "FROM RoastBatch RB JOIN Branch B ON RB.branch_id = B.branch_id " +
            "WHERE RB.branch_id = ? ORDER BY RB.roast_date DESC",
            branchId);
    }

    public static List<RoastBatchRow> getAll(Connection conn) {
        return query(conn,
            "SELECT RB.batch_id, RB.branch_id, B.branch_name, RB.roast_date, " +
            "       RB.kg_green, RB.kg_roasted, RB.roast_level " +
            "FROM RoastBatch RB JOIN Branch B ON RB.branch_id = B.branch_id " +
            "ORDER BY RB.roast_date DESC",
            -1);
    }

    private static List<RoastBatchRow> query(Connection conn, String sql, int branchId) {
        List<RoastBatchRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (branchId >= 0) ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new RoastBatchRow(
                    rs.getInt("batch_id"),
                    rs.getInt("branch_id"),
                    rs.getString("branch_name"),
                    rs.getString("roast_date"),
                    rs.getDouble("kg_green"),
                    rs.getDouble("kg_roasted"),
                    rs.getString("roast_level")
                ));
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // returns new batch_id on success, -1 on failure
    public static int addBatch(Connection conn, int branchId, String roastDate,
                               String kgGreenStr, String kgRoastedStr, String roastLevel) {
        if (roastDate.isBlank() || kgGreenStr.isBlank() || kgRoastedStr.isBlank() || roastLevel == null)
            return -1;
        try {
            double kgGreen    = Double.parseDouble(kgGreenStr);
            double kgRoasted  = Double.parseDouble(kgRoastedStr);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO RoastBatch (branch_id, roast_date, kg_green, kg_roasted, roast_level) " +
                "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, branchId);
            ps.setString(2, roastDate);
            ps.setDouble(3, kgGreen);
            ps.setDouble(4, kgRoasted);
            ps.setString(5, roastLevel);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (NumberFormatException | SQLException ex) {
            System.out.println(ex.getMessage());
            return -1;
        }
    }

    public static boolean updateBatch(Connection conn, int batchId, String roastDate,
                                      String kgGreenStr, String kgRoastedStr, String roastLevel) {
        if (roastDate.isBlank() || kgGreenStr.isBlank() || kgRoastedStr.isBlank() || roastLevel == null)
            return false;
        try {
            double kgGreen   = Double.parseDouble(kgGreenStr);
            double kgRoasted = Double.parseDouble(kgRoastedStr);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE RoastBatch SET roast_date=?, kg_green=?, kg_roasted=?, roast_level=? " +
                "WHERE batch_id=?"
            );
            ps.setString(1, roastDate);
            ps.setDouble(2, kgGreen);
            ps.setDouble(3, kgRoasted);
            ps.setString(4, roastLevel);
            ps.setInt(5, batchId);
            return ps.executeUpdate() > 0;
        } catch (NumberFormatException | SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
}

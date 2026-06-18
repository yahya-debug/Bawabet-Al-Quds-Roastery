package app.db_proj;

import app.db_proj.model.Item;
import app.db_proj.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public static class ItemStats {
        public final int reviewCount;
        public final double avgRating;
        public final int totalSold;

        public ItemStats(int reviewCount, double avgRating, int totalSold) {
            this.reviewCount = reviewCount;
            this.avgRating = avgRating;
            this.totalSold = totalSold;
        }
    }

    public static List<Item> getAll(Connection conn) {
        return query(conn, null);
    }

    public static List<Item> search(Connection conn, String q) {
        return query(conn, q);
    }

    private static List<Item> query(Connection conn, String search) {
        List<Item> list = new ArrayList<>();
        try {
            String sql =
                "SELECT I.item_id, I.name, I.price, I.wholesale_price, I.item_type, " +
                "       I.image_path, I.supplier_id, S.name AS supplier_name " +
                "FROM Item I LEFT JOIN Supplier S ON I.supplier_id = S.supplier_id";
            if (search != null && !search.isBlank()) sql += " WHERE I.name LIKE ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            if (search != null && !search.isBlank()) ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int rawSupId = rs.getInt("supplier_id");
                Integer supId = rs.wasNull() ? null : rawSupId;
                list.add(new Item(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getDouble("wholesale_price"),
                    rs.getString("item_type"),
                    rs.getString("image_path"),
                    supId,
                    rs.getString("supplier_name")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // Single query: avg rating, review count, total units sold from orders
    public static ItemStats getStats(Connection conn, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(DISTINCT R.review_id) AS review_count, " +
                "       AVG(R.rating)              AS avg_rating, " +
                "       SUM(OI.quantity)            AS total_sold " +
                "FROM Item I " +
                "LEFT JOIN Review    R  ON I.item_id = R.item_id " +
                "LEFT JOIN OrderItem OI ON I.item_id = OI.item_id " +
                "WHERE I.item_id = ?"
            );
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new ItemStats(
                rs.getInt("review_count"),
                rs.getDouble("avg_rating"),
                rs.getInt("total_sold")
            );
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return new ItemStats(0, 0, 0);
    }

    // Fetch reviews for one item joined with reviewer name, newest first
    public static List<Review> getReviews(Connection conn, int itemId) {
        List<Review> reviews = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT R.review_id, R.person_id, P.name AS person_name, " +
                "       R.item_id, I.name AS item_name, R.rating, R.comment, R.review_date " +
                "FROM Review R " +
                "JOIN Person P ON R.person_id = P.person_id " +
                "JOIN Item   I ON R.item_id   = I.item_id " +
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

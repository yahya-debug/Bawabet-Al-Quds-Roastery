package app.db_proj;

import java.sql.*;

public class Profile_Logic {

    // small holder for the customer details shown in the profile screen
    public static class CustomerInfo {
        public final String name;
        public final String email;
        public final String type;
        public final String phone;
        public final String address;

        public CustomerInfo(String name, String email, String type, String phone, String address) {
            this.name    = name;
            this.email   = email;
            this.type    = type;
            this.phone   = phone;
            this.address = address;
        }
    }

    // check whether the given person id is listed in the Admin table
    public static boolean isAdmin(Connection conn, int personId) {
        try {
            Statement stmt = conn.createStatement();
            System.out.println("looking for person_id: " + personId);
            ResultSet rs = stmt.executeQuery("SELECT * FROM Admin WHERE person_id = " + personId + ";");
            return rs.next();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // load the name, email, type, phone, and address for the logged-in person
    public static CustomerInfo getCustomer(Connection conn, int personId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT P.name, P.email, C.type, P.phone, L.street AS address " +
                "FROM Person P " +
                "LEFT JOIN Customer C ON P.person_id = C.person_id " +
                "LEFT JOIN Location L ON C.location_id = L.location_id " +
                "WHERE P.person_id = ?"
            );
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CustomerInfo(
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("type"),
                    rs.getString("phone"),
                    rs.getString("address")
                );
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    // update name, email, and phone in Person; returns "ok", "empty", "duplicate", or "error"
    public static String updateProfile(Connection conn, int personId, String name, String email, String phone) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) return "empty";
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Person SET name=?, email=?, phone=? WHERE person_id=?"
            );
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, (phone == null || phone.isBlank()) ? null : phone.trim());
            ps.setInt(4, personId);
            ps.executeUpdate();
            return "ok";
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) return "duplicate";
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // upsert the customer's address into Location and link it via Customer.location_id
    public static String updateAddress(Connection conn, int personId, String address) {
        if (address == null || address.isBlank()) return "ok";
        try {
            PreparedStatement check = conn.prepareStatement(
                "SELECT location_id FROM Customer WHERE person_id=?"
            );
            check.setInt(1, personId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int locId = rs.getInt("location_id");
                if (!rs.wasNull() && locId > 0) {
                    PreparedStatement upd = conn.prepareStatement(
                        "UPDATE Location SET street=? WHERE location_id=?"
                    );
                    upd.setString(1, address.trim());
                    upd.setInt(2, locId);
                    upd.executeUpdate();
                } else {
                    PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO Location (street, city, zip) VALUES (?, '-', '-')",
                        Statement.RETURN_GENERATED_KEYS
                    );
                    ins.setString(1, address.trim());
                    ins.executeUpdate();
                    ResultSet keys = ins.getGeneratedKeys();
                    if (keys.next()) {
                        int newLocId = keys.getInt(1);
                        PreparedStatement link = conn.prepareStatement(
                            "UPDATE Customer SET location_id=? WHERE person_id=?"
                        );
                        link.setInt(1, newLocId);
                        link.setInt(2, personId);
                        link.executeUpdate();
                    }
                }
            }
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // verify current password then update to newPw
    // returns "ok", "empty", "wrong_password", "mismatch", or "error"
    public static String updatePassword(Connection conn, int personId, String current, String newPw, String confirm) {
        if (current == null || current.isBlank() || newPw == null || newPw.isBlank() || confirm == null || confirm.isBlank())
            return "empty";
        if (!newPw.equals(confirm)) return "mismatch";
        try {
            PreparedStatement check = conn.prepareStatement(
                "SELECT password FROM Person WHERE person_id=?"
            );
            check.setInt(1, personId);
            ResultSet rs = check.executeQuery();
            if (!rs.next() || !current.equals(rs.getString("password"))) return "wrong_password";
            PreparedStatement upd = conn.prepareStatement(
                "UPDATE Person SET password=? WHERE person_id=?"
            );
            upd.setString(1, newPw);
            upd.setInt(2, personId);
            upd.executeUpdate();
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }
}

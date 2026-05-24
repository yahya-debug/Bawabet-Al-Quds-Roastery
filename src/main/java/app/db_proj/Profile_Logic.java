package app.db_proj;

import java.sql.*;

public class Profile_Logic {

    // small holder for the customer details shown in the profile screen
    public static class CustomerInfo {
        public final String name;
        public final String email;
        public final String type;

        public CustomerInfo(String name, String email, String type) {
            this.name = name;
            this.email = email;
            this.type = type;
        }
    }

    // check whether the given person id is listed in the Admin table
    // used by the profile menu to decide if the Admin Panel entry should be shown
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

    // load the name email and customer type for the logged in person
    // returns null when no person matches the given id
    public static CustomerInfo getCustomer(Connection conn, int personId) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT P.name, P.email, C.type " +
                "FROM Person P LEFT JOIN Customer C ON P.person_id = C.person_id " +
                "WHERE P.person_id = " + personId + ";"
            );
            if (rs.next()) {
                return new CustomerInfo(
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("type")
                );
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
}

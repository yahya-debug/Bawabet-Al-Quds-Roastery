package app.db_proj;

import java.sql.*;

public class Auth_Logic {

    // check if a user exists with matching name email and password
    // also stores the logged in person id so other screens can read it
    // returns ok, empty, not_found, or error
    public static String login(SystemHandling sys, String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty())
            return "empty";
        try {
            Statement stmt = sys.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT person_id FROM Person WHERE email = '" + email + "' AND name = '" + name + "' AND password = '" + password + "';");
            if (rs.next()) {
                int id = rs.getInt("person_id");
                sys.setCurrentUser(id, name, email);
                return "ok";
            }
            return "not_found";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // insert a new personal account
    // adds the row to Person then marks the same person_id as a customer of type individual
    // also stores the new user as the current session so the home page opens already signed in
    // phone is captured by the form but the schema has no place for it yet so it is ignored
    // returns ok, empty, duplicate, or error
    public static String signupPersonal(SystemHandling sys, String name, String email, String phone, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty())
            return "empty";
        try {
            // insert the person row and grab the generated id
            PreparedStatement personStmt = sys.getConn().prepareStatement(
                "INSERT INTO Person (name, email, password) VALUES ('" + name + "','" + email + "','" + password + "');",
                Statement.RETURN_GENERATED_KEYS
            );
            personStmt.executeUpdate();
            ResultSet keys = personStmt.getGeneratedKeys();
            int personId = -1;
            if (keys.next()) personId = keys.getInt(1);

            // mark this person as a customer of type individual in both subtype tables
            Statement stmt = sys.getConn().createStatement();
            stmt.executeUpdate("INSERT INTO Customer (person_id, type) VALUES (" + personId + ",'individual');");
            stmt.executeUpdate("INSERT INTO Individual (person_id) VALUES (" + personId + ");");

            // flip the sys auth attribute so the user is treated as signed in right away
            sys.setCurrentUser(personId, name, email);
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            if ("23000".equals(ex.getSQLState())) return "duplicate";
            return "error";
        }
    }

    // insert a new business account
    // adds the address into Location first then Person then Customer with type business then Business
    // also stores the new user as the current session so the home page opens already signed in
    // location string is expected to look like street, city, zip and is split on commas
    // returns ok, empty, duplicate, or error
    public static String signupBusiness(SystemHandling sys, String name, String email, String phone, String password, String location, String kindOfBusiness, String taxId, String regNumber) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || location.isEmpty() || taxId.isEmpty() || regNumber.isEmpty() || kindOfBusiness.isEmpty())
            return "empty";
        try {
            // split the address into the three pieces the Location table expects
            String[] loc = location.split(",");
            String street = loc.length > 0 ? loc[0].trim() : "";
            String city   = loc.length > 1 ? loc[1].trim() : "";
            String zip    = loc.length > 2 ? loc[2].trim() : "";

            // insert the location first because customer needs to reference it
            PreparedStatement locStmt = sys.getConn().prepareStatement(
                "INSERT INTO Location (street, city, zip) VALUES ('" + street + "','" + city + "','" + zip + "');",
                Statement.RETURN_GENERATED_KEYS
            );
            locStmt.executeUpdate();
            ResultSet locKeys = locStmt.getGeneratedKeys();
            int locationId = -1;
            if (locKeys.next()) locationId = locKeys.getInt(1);

            // then the person row and grab the generated person id
            PreparedStatement personStmt = sys.getConn().prepareStatement(
                "INSERT INTO Person (name, email, password) VALUES ('" + name + "','" + email + "','" + password + "');",
                Statement.RETURN_GENERATED_KEYS
            );
            personStmt.executeUpdate();
            ResultSet keys = personStmt.getGeneratedKeys();
            int personId = -1;
            if (keys.next()) personId = keys.getInt(1);

            // mark this person as a customer of type business linked to the location
            // then add the business specific fields in the Business table
            Statement stmt = sys.getConn().createStatement();
            stmt.executeUpdate("INSERT INTO Customer (person_id, type, location_id) VALUES (" + personId + ",'business'," + locationId + ");");
            stmt.executeUpdate("INSERT INTO Business (person_id, registration_number, TAX_id, business_type) VALUES (" + personId + ",'" + regNumber + "','" + taxId + "','" + kindOfBusiness + "');");

            // flip the sys auth attribute so the user is treated as signed in right away
            sys.setCurrentUser(personId, name, email);
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            if ("23000".equals(ex.getSQLState())) return "duplicate";
            return "error";
        }
    }
}

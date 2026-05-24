package app.db_proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Admin_Logic {

    // small holder for one row in the customers table view
    public static class CustomerRow {
        private final int personId;
        private final String name;
        private final String email;
        private final String type;

        public CustomerRow(int personId, String name, String email, String type) {
            this.personId = personId;
            this.name = name;
            this.email = email;
            this.type = type;
        }

        public int getPersonId() { return personId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getType() { return type; }
    }

    // small holder for one row in the employees table view
    public static class EmployeeRow {
        private final int personId;
        private final String name;
        private final String email;
        private final String role;
        private final double salary;
        private final String hireDate;
        private final int branchId;

        public EmployeeRow(int personId, String name, String email, String role, double salary, String hireDate, int branchId) {
            this.personId = personId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.salary = salary;
            this.hireDate = hireDate;
            this.branchId = branchId;
        }

        public int getPersonId() { return personId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public double getSalary() { return salary; }
        public String getHireDate() { return hireDate; }
        public int getBranchId() { return branchId; }
    }

    // small holder for one branch entry used by the branch combobox
    public static class BranchRow {
        public final int branchId;
        public final String name;

        public BranchRow(int branchId, String name) {
            this.branchId = branchId;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // fetch all customers joined with their person info
    public static List<CustomerRow> getCustomers(Connection conn) {
        List<CustomerRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT P.person_id, P.name, P.email, C.type " +
                "FROM Person P JOIN Customer C ON P.person_id = C.person_id;"
            );
            while (rs.next()) {
                list.add(new CustomerRow(
                    rs.getInt("person_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // fetch all employees joined with their person info
    public static List<EmployeeRow> getEmployees(Connection conn) {
        List<EmployeeRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT P.person_id, P.name, P.email, E.role, E.salary, E.hire_date, E.branch_id " +
                "FROM Person P JOIN Employee E ON P.person_id = E.person_id;"
            );
            while (rs.next()) {
                list.add(new EmployeeRow(
                    rs.getInt("person_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getDouble("salary"),
                    rs.getString("hire_date"),
                    rs.getInt("branch_id")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // fetch all branches so they can populate the branch combobox in the form
    public static List<BranchRow> getBranches(Connection conn) {
        List<BranchRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT branch_id, branch_name FROM Branch;");
            while (rs.next()) {
                list.add(new BranchRow(rs.getInt("branch_id"), rs.getString("branch_name")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // insert a new admin in two steps, first into Person then into Admin
    // returns ok, empty, duplicate, or error
    public static String registerAdmin(Connection conn, String name, String email, String password, Integer branchId) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || branchId == null)
            return "empty";
        try {
            // insert the person row first and grab the generated id
            PreparedStatement personStmt = conn.prepareStatement(
                "INSERT INTO Person (name, email, password) VALUES ('" + name + "','" + email + "','" + password + "');",
                Statement.RETURN_GENERATED_KEYS
            );
            personStmt.executeUpdate();
            ResultSet keys = personStmt.getGeneratedKeys();
            int personId = -1;
            if (keys.next()) personId = keys.getInt(1);

            // then insert the admin row pointing at that person id and chosen branch
            Statement adminStmt = conn.createStatement();
            adminStmt.executeUpdate(
                "INSERT INTO Admin (person_id, branch_id) VALUES (" + personId + "," + branchId + ");"
            );
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            if ("23000".equals(ex.getSQLState())) return "duplicate";
            return "error";
        }
    }

    // insert a new employee in two steps, first into Person then into Employee
    // returns ok, empty, duplicate, or error
    public static String registerEmployee(Connection conn, String name, String email, String password,
                                          String role, String salary, String hireDate, Integer branchId) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()
                || salary.isEmpty() || hireDate.isEmpty() || branchId == null)
            return "empty";
        try {
            // insert the person row first and grab the generated id
            PreparedStatement personStmt = conn.prepareStatement(
                "INSERT INTO Person (name, email, password) VALUES ('" + name + "','" + email + "','" + password + "');",
                Statement.RETURN_GENERATED_KEYS
            );
            personStmt.executeUpdate();
            ResultSet keys = personStmt.getGeneratedKeys();
            int personId = -1;
            if (keys.next()) personId = keys.getInt(1);

            // then insert the employee row pointing at that person id
            Statement empStmt = conn.createStatement();
            empStmt.executeUpdate(
                "INSERT INTO Employee (person_id, role, salary, hire_date, branch_id) VALUES (" +
                personId + ",'" + role + "'," + salary + ",'" + hireDate + "'," + branchId + ");"
            );
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            if ("23000".equals(ex.getSQLState())) return "duplicate";
            return "error";
        }
    }
}

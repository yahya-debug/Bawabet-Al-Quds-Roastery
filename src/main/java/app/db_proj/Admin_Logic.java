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

    // small holder for one item row
    public static class ItemRow {
        private final int itemId;
        private final String name;
        private final double price;
        private final String itemType;
        private final String imagePath;
        private final String supplierName;

        public ItemRow(int itemId, String name, double price, String itemType, String imagePath, String supplierName) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.itemType = itemType;
            this.imagePath = imagePath;
            this.supplierName = supplierName;
        }

        public int getItemId()          { return itemId; }
        public String getName()         { return name; }
        public double getPrice()        { return price; }
        public String getItemType()     { return itemType; }
        public String getImagePath()    { return imagePath; }
        public String getSupplierName() { return supplierName; }
    }

    // small holder for one supplier row
    public static class SupplierRow {
        private final int supplierId;
        private final String name;
        private final String email;
        private final String phone;

        public SupplierRow(int supplierId, String name, String email, String phone) {
            this.supplierId = supplierId;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public int getSupplierId()  { return supplierId; }
        public String getName()     { return name; }
        public String getEmail()    { return email; }
        public String getPhone()    { return phone; }
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

    // small holder for branch info joined with its location row
    public static class BranchDetailRow {
        public final int branchId;
        public final String name;
        public final String street;
        public final String city;
        public final String zip;

        public BranchDetailRow(int branchId, String name, String street, String city, String zip) {
            this.branchId = branchId;
            this.name = name;
            this.street = street;
            this.city = city;
            this.zip = zip;
        }
    }

    // return the branch_id where the given employee works, or -1 if not found
    public static int getEmployeeBranchId(Connection conn, int personId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT branch_id FROM Employee WHERE person_id = ?"
            );
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("branch_id");
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return -1;
    }

    // items that exist in the catalog but have no BranchInventory row for the given branch
    public static List<ItemRow> getItemsNotAtBranch(Connection conn, int branchId) {
        List<ItemRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT I.item_id, I.name, I.price, I.item_type, I.image_path, S.name AS supplier_name " +
                "FROM Item I " +
                "LEFT JOIN Supplier S ON I.supplier_id = S.supplier_id " +
                "LEFT JOIN BranchInventory BI ON I.item_id = BI.item_id AND BI.branch_id = ? " +
                "WHERE BI.item_id IS NULL ORDER BY I.name"
            );
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ItemRow(
                    rs.getInt("item_id"), rs.getString("name"), rs.getDouble("price"),
                    rs.getString("item_type"), rs.getString("image_path"), rs.getString("supplier_name")
                ));
            }
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // return the branch_id managed by the given admin, or -1 if not found
    public static int getAdminBranchId(Connection conn, int adminPersonId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT branch_id FROM Admin WHERE person_id = ?"
            );
            ps.setInt(1, adminPersonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("branch_id");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return -1;
    }

    // fetch branch details joined with location for the given branch_id
    public static BranchDetailRow getBranchDetail(Connection conn, int branchId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT B.branch_id, B.branch_name, L.street, L.city, L.zip " +
                "FROM Branch B JOIN Location L ON B.location_id = L.location_id " +
                "WHERE B.branch_id = ?"
            );
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new BranchDetailRow(
                    rs.getInt("branch_id"),
                    rs.getString("branch_name"),
                    rs.getString("street"),
                    rs.getString("city"),
                    rs.getString("zip")
                );
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    // fetch employees that belong to the given branch
    public static List<EmployeeRow> getEmployeesByBranch(Connection conn, int branchId) {
        List<EmployeeRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT P.person_id, P.name, P.email, E.role, E.salary, E.hire_date, E.branch_id " +
                "FROM Person P JOIN Employee E ON P.person_id = E.person_id " +
                "WHERE E.branch_id = ?"
            );
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
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

    // count employees assigned to a branch (used in the branch info card)
    public static int getEmployeeCountByBranch(Connection conn, int branchId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM Employee WHERE branch_id = ?"
            );
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
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

    // fetch all branches with their location details
    public static List<BranchDetailRow> getAllBranches(Connection conn) {
        List<BranchDetailRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT B.branch_id, B.branch_name, L.street, L.city, L.zip " +
                "FROM Branch B JOIN Location L ON B.location_id = L.location_id;"
            );
            while (rs.next()) {
                list.add(new BranchDetailRow(
                    rs.getInt("branch_id"),
                    rs.getString("branch_name"),
                    rs.getString("street"),
                    rs.getString("city"),
                    rs.getString("zip")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // insert a new branch — first creates a location row, then the branch row
    public static String addBranch(Connection conn, String name, String street, String city, String zip) {
        if (name.isBlank() || street.isBlank() || city.isBlank() || zip.isBlank()) return "empty";
        try {
            PreparedStatement locStmt = conn.prepareStatement(
                "INSERT INTO Location (street, city, zip) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            locStmt.setString(1, street);
            locStmt.setString(2, city);
            locStmt.setString(3, zip);
            locStmt.executeUpdate();
            ResultSet keys = locStmt.getGeneratedKeys();
            if (!keys.next()) return "error";
            int locationId = keys.getInt(1);

            PreparedStatement branchStmt = conn.prepareStatement(
                "INSERT INTO Branch (branch_name, location_id) VALUES (?, ?)"
            );
            branchStmt.setString(1, name);
            branchStmt.setInt(2, locationId);
            branchStmt.executeUpdate();
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // fetch all items joined with their supplier name
    public static List<ItemRow> getItems(Connection conn) {
        List<ItemRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT I.item_id, I.name, I.price, I.item_type, I.image_path, S.name AS supplier_name " +
                "FROM Item I LEFT JOIN Supplier S ON I.supplier_id = S.supplier_id;"
            );
            while (rs.next()) {
                list.add(new ItemRow(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("item_type"),
                    rs.getString("image_path"),
                    rs.getString("supplier_name")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // update an existing item's fields; imagePath null means keep the old value
    public static String updateItem(Connection conn, int itemId,
                                    String name, String priceStr, String wholesaleStr,
                                    String itemType, String imagePath) {
        if (name.isBlank() || priceStr.isBlank()) return "empty";
        try {
            double price     = Double.parseDouble(priceStr);
            double wholesale = wholesaleStr.isBlank() ? 0 : Double.parseDouble(wholesaleStr);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Item SET name=?, price=?, wholesale_price=?, item_type=?, image_path=? " +
                "WHERE item_id=?"
            );
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setDouble(3, wholesale);
            ps.setString(4, itemType.isBlank() ? null : itemType);
            ps.setString(5, imagePath != null && !imagePath.isBlank() ? imagePath : null);
            ps.setInt(6, itemId);
            ps.executeUpdate();
            return "ok";
        } catch (NumberFormatException ex) {
            return "price_error";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // insert a new item; returns the new item_id (>0), -1 for empty, -2 for price error, -3 for SQL error
    // always assigns a supplier: uses defaultSupplierId if supplierId <= 0
    public static int addItem(Connection conn, String name, String priceStr, String itemType,
                              String imagePath, int supplierId) {
        if (name.isBlank() || priceStr.isBlank() || itemType.isBlank()) return -1;
        try {
            double price = Double.parseDouble(priceStr);
            int effectiveSupplier = supplierId > 0 ? supplierId : ensureDefaultSupplier(conn);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Item (name, price, item_type, image_path, supplier_id) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, itemType);
            ps.setString(4, (imagePath != null && !imagePath.isBlank()) ? imagePath : null);
            if (effectiveSupplier > 0) ps.setInt(5, effectiveSupplier);
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -3;
        } catch (NumberFormatException ex) {
            return -2;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return -3;
        }
    }

    // backward-compat overload — uses default supplier
    public static int addItem(Connection conn, String name, String priceStr, String itemType, String imagePath) {
        return addItem(conn, name, priceStr, itemType, imagePath, -1);
    }

    // ── BRANCH INVENTORY ─────────────────────────────────────────────────────

    public static class StockRow {
        public final int itemId;
        public final String itemName;
        public final int quantity;
        public StockRow(int itemId, String itemName, int quantity) {
            this.itemId = itemId; this.itemName = itemName; this.quantity = quantity;
        }
        public int getItemId()     { return itemId; }
        public String getItemName(){ return itemName; }
        public int getQuantity()   { return quantity; }
    }

    public static List<StockRow> getBranchStock(Connection conn, int branchId) {
        List<StockRow> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT I.item_id, I.name, BI.quantity " +
                "FROM BranchInventory BI JOIN Item I ON BI.item_id = I.item_id " +
                "WHERE BI.branch_id = ? ORDER BY I.name"
            );
            ps.setInt(1, branchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new StockRow(rs.getInt("item_id"), rs.getString("name"), rs.getInt("quantity")));
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return list;
    }

    // Returns total stock across all branches for one item
    public static double getTotalBranchStock(Connection conn, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(quantity), 0) AS total FROM BranchInventory WHERE item_id = ?"
            );
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return 0.0;
    }

    // kept for callers that used the old name
    public static double getTotalStock(Connection conn, int itemId) {
        return getTotalBranchStock(conn, itemId);
    }

    // stock of one item at a specific branch (0 if not stocked there)
    public static double getBranchItemStock(Connection conn, int branchId, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(quantity, 0) AS qty FROM BranchInventory WHERE branch_id = ? AND item_id = ?"
            );
            ps.setInt(1, branchId); ps.setInt(2, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("qty");
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
        return 0.0;
    }

    public static void setStock(Connection conn, int branchId, int itemId, double quantity) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO BranchInventory (branch_id, item_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = ?"
            );
            ps.setInt(1, branchId); ps.setInt(2, itemId);
            ps.setDouble(3, quantity); ps.setDouble(4, quantity);
            ps.executeUpdate();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
    }

    public static boolean deleteItem(Connection conn, int itemId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM Item WHERE item_id = ?"
            );
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.out.println("deleteItem: " + ex.getMessage());
            return false;
        }
    }

    // adds image_path column to Item if it does not already exist
    public static void ensureItemImageColumn(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "ALTER TABLE Item ADD COLUMN image_path VARCHAR(500) NULL"
            );
        } catch (SQLException ex) {
            // duplicate column error (1060) means it already exists — safe to ignore
        }
    }

    // creates the Supplier table if it does not exist yet, then ensures the default company supplier row exists
    public static void ensureSupplierTable(Connection conn) {
        try {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS Supplier (" +
                "supplier_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name        VARCHAR(100) NOT NULL, " +
                "email       VARCHAR(100), " +
                "phone       VARCHAR(45)" +
                ")"
            );
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        ensureDefaultSupplier(conn);
    }

    // guarantees the in-house "Bawabet Al-Quds" supplier exists; returns its supplier_id
    public static int ensureDefaultSupplier(Connection conn) {
        try {
            PreparedStatement check = conn.prepareStatement(
                "SELECT supplier_id FROM Supplier WHERE name = 'Bawabet Al-Quds' LIMIT 1"
            );
            ResultSet rs = check.executeQuery();
            if (rs.next()) return rs.getInt("supplier_id");

            PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO Supplier (name, email) VALUES ('Bawabet Al-Quds', 'info@bawabet-alquds.com')",
                Statement.RETURN_GENERATED_KEYS
            );
            ins.executeUpdate();
            ResultSet keys = ins.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return -1;
        }
    }

    // fetch all suppliers
    public static List<SupplierRow> getSuppliers(Connection conn) {
        List<SupplierRow> list = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT supplier_id, name, email, phone FROM Supplier;");
            while (rs.next()) {
                list.add(new SupplierRow(
                    rs.getInt("supplier_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    // insert a new supplier; phone is optional
    public static String addSupplier(Connection conn, String name, String email, String phone) {
        if (name.isBlank() || email.isBlank()) return "empty";
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Supplier (name, email, phone) VALUES (?, ?, ?)"
            );
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone.isBlank() ? null : phone);
            ps.executeUpdate();
            return "ok";
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "error";
        }
    }

    // insert a new personal customer (Person + Customer rows)
    public static String addCustomer(Connection conn, String name, String email, String password, String type) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return "empty";
        try {
            PreparedStatement personStmt = conn.prepareStatement(
                "INSERT INTO Person (name, email, password) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            personStmt.setString(1, name);
            personStmt.setString(2, email);
            personStmt.setString(3, password);
            personStmt.executeUpdate();
            ResultSet keys = personStmt.getGeneratedKeys();
            if (!keys.next()) return "error";
            int personId = keys.getInt(1);

            PreparedStatement custStmt = conn.prepareStatement(
                "INSERT INTO Customer (person_id, type) VALUES (?, ?)"
            );
            custStmt.setInt(1, personId);
            custStmt.setString(2, type);
            custStmt.executeUpdate();
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

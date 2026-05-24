package app.db_proj;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class Admin_UI {
    private HBox screen;
    private VBox left_side, right_side;
    private TableView<Admin_Logic.CustomerRow> customers_tv;
    private TableView<Admin_Logic.EmployeeRow> employees_tv;
    private VBox table_holder;
    private SystemHandling sys;

    public Admin_UI(SystemHandling sys) {
        this.sys = sys;
        screen = new HBox(10);
        screen.setPrefWidth(Double.MAX_VALUE);

        make_left_side();
        make_right_side();

        screen.getChildren().addAll(left_side, right_side);
    }

    // build the left side with the title a toggle and the active table
    private void make_left_side() {
        left_side = new VBox(10);
        left_side.setPadding(new Insets(0, 15, 15, 15));
        left_side.prefWidthProperty().bind(screen.widthProperty().multiply(0.60));

        // toggle row built the same way as the in store and online buttons in cart
        HBox toggle = new HBox(0);
        toggle.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(10), null)));
        toggle.setMaxWidth(Region.USE_PREF_SIZE);
        toggle.setMaxHeight(Region.USE_PREF_SIZE);

        Button customers_btn = new Button("Customers");
        Button employees_btn = new Button("Employees");

        for (Button b : new Button[]{customers_btn, employees_btn}) {
            b.setFont(Font.font("Nunito", 15));
            b.setPadding(new Insets(5, 14, 5, 14));
            b.setCursor(Cursor.HAND);
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        }

        // default selected tab is Customers
        customers_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
        customers_btn.setTextFill(Color.BLACK);

        toggle.getChildren().addAll(customers_btn, employees_btn);

        // build both tables once so swapping is just a children swap
        build_customers_table();
        build_employees_table();

        table_holder = new VBox();
        table_holder.getChildren().add(customers_tv);
        VBox.setVgrow(customers_tv, Priority.ALWAYS);
        VBox.setVgrow(employees_tv, Priority.ALWAYS);
        VBox.setVgrow(table_holder, Priority.ALWAYS);

        customers_btn.setOnAction(e -> {
            customers_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            customers_btn.setTextFill(Color.BLACK);
            employees_btn.setBackground(Background.EMPTY);
            employees_btn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            refresh_customers();
            table_holder.getChildren().setAll(customers_tv);
        });

        employees_btn.setOnAction(e -> {
            employees_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            employees_btn.setTextFill(Color.BLACK);
            customers_btn.setBackground(Background.EMPTY);
            customers_btn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            refresh_employees();
            table_holder.getChildren().setAll(employees_tv);
        });

        Label title = new Labels("Admin Panel", Font.font("Adwaita Mono", FontWeight.BOLD, 30), Color.hsb(48, 1, 0.92, 1)).getLabel();

        HBox top = new HBox(15);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(5, 0, 5, 0));
        top.getChildren().addAll(title, toggle);

        left_side.getChildren().addAll(top, table_holder);

        // first load so the customers table is not empty when the page opens
        refresh_customers();
    }

    // build the customers table once with the columns from the schema
    private void build_customers_table() {
        customers_tv = new TableView<>();
        styleTable(customers_tv);

        TableColumn<Admin_Logic.CustomerRow, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPersonId()));

        TableColumn<Admin_Logic.CustomerRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Admin_Logic.CustomerRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Admin_Logic.CustomerRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));

        customers_tv.getColumns().add(idCol);
        customers_tv.getColumns().add(nameCol);
        customers_tv.getColumns().add(emailCol);
        customers_tv.getColumns().add(typeCol);
        customers_tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customers_tv.setPlaceholder(new Labels("No customers yet", Font.font("Nunito", 16), Color.hsb(30, 0.12, 0.78, 1)).getLabel());
    }

    // build the employees table once with the columns from the schema
    private void build_employees_table() {
        employees_tv = new TableView<>();
        styleTable(employees_tv);

        TableColumn<Admin_Logic.EmployeeRow, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPersonId()));

        TableColumn<Admin_Logic.EmployeeRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Admin_Logic.EmployeeRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Admin_Logic.EmployeeRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));

        TableColumn<Admin_Logic.EmployeeRow, Number> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSalary()));

        TableColumn<Admin_Logic.EmployeeRow, String> hireCol = new TableColumn<>("Hire Date");
        hireCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHireDate()));

        TableColumn<Admin_Logic.EmployeeRow, Number> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getBranchId()));

        employees_tv.getColumns().add(idCol);
        employees_tv.getColumns().add(nameCol);
        employees_tv.getColumns().add(emailCol);
        employees_tv.getColumns().add(roleCol);
        employees_tv.getColumns().add(salaryCol);
        employees_tv.getColumns().add(hireCol);
        employees_tv.getColumns().add(branchCol);
        employees_tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        employees_tv.setPlaceholder(new Labels("No employees yet", Font.font("Nunito", 16), Color.hsb(30, 0.12, 0.78, 1)).getLabel());
    }

    // apply the project color scheme to a table view
    private void styleTable(TableView<?> tv) {
        tv.setStyle(
            "-fx-background-color: hsb(35, 8%, 46%);" +
            "-fx-table-cell-border-color: hsb(35, 8%, 38%);" +
            "-fx-control-inner-background: hsb(35, 8%, 46%);" +
            "-fx-control-inner-background-alt: hsb(35, 8%, 42%);" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Nunito';" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 12;"
        );
    }

    // load customers from the database and push them into the table
    private void refresh_customers() {
        List<Admin_Logic.CustomerRow> rows = Admin_Logic.getCustomers(sys.getConn());
        customers_tv.setItems(FXCollections.observableArrayList(rows));
    }

    // load employees from the database and push them into the table
    private void refresh_employees() {
        List<Admin_Logic.EmployeeRow> rows = Admin_Logic.getEmployees(sys.getConn());
        employees_tv.setItems(FXCollections.observableArrayList(rows));
    }

    // build the right side which holds a toggle and the active register form
    private void make_right_side() {
        right_side = new VBox(10);
        right_side.setPadding(new Insets(35, 15, 15, 15));
        right_side.prefWidthProperty().bind(screen.widthProperty().multiply(0.40));

        // toggle to swap the register employee form with the register admin form
        HBox toggle = new HBox(0);
        toggle.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(10), null)));
        toggle.setMaxWidth(Region.USE_PREF_SIZE);
        toggle.setMaxHeight(Region.USE_PREF_SIZE);

        Button emp_btn = new Button("Employee");
        Button admin_btn = new Button("Admin");

        for (Button b : new Button[]{emp_btn, admin_btn}) {
            b.setFont(Font.font("Nunito", 15));
            b.setPadding(new Insets(5, 14, 5, 14));
            b.setCursor(Cursor.HAND);
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        }

        // default selected form is Employee
        emp_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
        emp_btn.setTextFill(Color.BLACK);

        toggle.getChildren().addAll(emp_btn, admin_btn);

        // build both forms up front so the toggle just swaps the holder children
        VBox employee_form = make_employee_form();
        VBox admin_form = make_admin_form();

        VBox form_holder = new VBox();
        form_holder.getChildren().add(employee_form);

        emp_btn.setOnAction(e -> {
            emp_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            emp_btn.setTextFill(Color.BLACK);
            admin_btn.setBackground(Background.EMPTY);
            admin_btn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            form_holder.getChildren().setAll(employee_form);
        });

        admin_btn.setOnAction(e -> {
            admin_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            admin_btn.setTextFill(Color.BLACK);
            emp_btn.setBackground(Background.EMPTY);
            emp_btn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            form_holder.getChildren().setAll(admin_form);
        });

        HBox top = new HBox(15);
        top.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Labels("Register", Font.font("Adwaita Mono", FontWeight.BOLD, 25), Color.hsb(48, 1, 0.92, 1)).getLabel();
        top.getChildren().addAll(heading, toggle);

        right_side.getChildren().addAll(top, form_holder);
    }

    // build the register employee form inside its own card
    private VBox make_employee_form() {
        VBox form_box = new VBox(10);
        form_box.setPadding(new Insets(12));
        form_box.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));

        Label title = new Labels("Register Employee", Font.font("Adwaita Mono", FontWeight.BOLD, 22), Color.hsb(48, 1, 0.92, 1)).getLabel();

        // warning shown when any required field is left empty
        Label warn_empty = new Label("Fill in all fields");
        warn_empty.setFont(Font.font("Nunito", 16));
        warn_empty.setTextFill(Color.RED);
        warn_empty.setVisible(false);
        warn_empty.managedProperty().bind(warn_empty.visibleProperty());

        // warning shown when the name or email is already taken
        Label warn_duplicate = new Label("Name or email already exists");
        warn_duplicate.setFont(Font.font("Nunito", 16));
        warn_duplicate.setTextFill(Color.RED);
        warn_duplicate.setVisible(false);
        warn_duplicate.managedProperty().bind(warn_duplicate.visibleProperty());

        // confirmation shown after a successful insert
        Label ok_msg = new Label("Employee added");
        ok_msg.setFont(Font.font("Nunito", 16));
        ok_msg.setTextFill(Color.hsb(120, 0.5, 0.85, 1));
        ok_msg.setVisible(false);
        ok_msg.managedProperty().bind(ok_msg.visibleProperty());

        TextField name = formField("Name");
        TextField email = formField("Email");
        TextField password = formField("Password");
        TextField role = formField("Role");
        TextField salary = formField("Salary");

        DatePicker hire_date = new DatePicker();
        hire_date.setPromptText("Hire Date");
        hire_date.setStyle("-fx-background-color: hsb(0, 0%, 25%);");
        hire_date.setMaxWidth(Double.MAX_VALUE);

        // load branches into the combobox so the user picks one instead of typing
        ComboBox<Admin_Logic.BranchRow> branch_cb = branchCombo();

        Button submit = new Buttons("Register", new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)).getBtn();
        submit.setFont(Font.font("Nunito", FontWeight.BOLD, 18));
        submit.setTextFill(Color.BLACK);
        submit.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(submit, new Insets(7, 0, 0, 0));

        submit.setOnAction(e -> {
            // hide any previous messages before checking
            warn_empty.setVisible(false);
            warn_duplicate.setVisible(false);
            ok_msg.setVisible(false);

            Integer branchId = branch_cb.getValue() != null ? branch_cb.getValue().branchId : null;
            String dateStr = hire_date.getValue() != null ? hire_date.getValue().toString() : "";

            String result = Admin_Logic.registerEmployee(
                sys.getConn(),
                name.getText(), email.getText(), password.getText(),
                role.getText(), salary.getText(), dateStr, branchId
            );

            if (result.equals("empty")) warn_empty.setVisible(true);
            else if (result.equals("duplicate")) warn_duplicate.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                // clear the form so the admin can register another employee right away
                name.clear();
                email.clear();
                password.clear();
                role.clear();
                salary.clear();
                hire_date.setValue(null);
                branch_cb.getSelectionModel().clearSelection();
                // refresh the employees table so the new row shows up if it is open
                refresh_employees();
            }
        });

        form_box.getChildren().addAll(title, warn_empty, warn_duplicate, ok_msg,
                name, email, password, role, salary, hire_date, branch_cb, submit);
        return form_box;
    }

    // build the register admin form inside its own card
    private VBox make_admin_form() {
        VBox form_box = new VBox(10);
        form_box.setPadding(new Insets(12));
        form_box.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));

        Label title = new Labels("Register Admin", Font.font("Adwaita Mono", FontWeight.BOLD, 22), Color.hsb(48, 1, 0.92, 1)).getLabel();

        // same three status labels we use on the employee form
        Label warn_empty = new Label("Fill in all fields");
        warn_empty.setFont(Font.font("Nunito", 16));
        warn_empty.setTextFill(Color.RED);
        warn_empty.setVisible(false);
        warn_empty.managedProperty().bind(warn_empty.visibleProperty());

        Label warn_duplicate = new Label("Name or email already exists");
        warn_duplicate.setFont(Font.font("Nunito", 16));
        warn_duplicate.setTextFill(Color.RED);
        warn_duplicate.setVisible(false);
        warn_duplicate.managedProperty().bind(warn_duplicate.visibleProperty());

        Label ok_msg = new Label("Admin added");
        ok_msg.setFont(Font.font("Nunito", 16));
        ok_msg.setTextFill(Color.hsb(120, 0.5, 0.85, 1));
        ok_msg.setVisible(false);
        ok_msg.managedProperty().bind(ok_msg.visibleProperty());

        TextField name = formField("Name");
        TextField email = formField("Email");
        TextField password = formField("Password");

        // branch is the only extra field admins need on top of the person info
        ComboBox<Admin_Logic.BranchRow> branch_cb = branchCombo();

        Button submit = new Buttons("Register", new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)).getBtn();
        submit.setFont(Font.font("Nunito", FontWeight.BOLD, 18));
        submit.setTextFill(Color.BLACK);
        submit.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(submit, new Insets(7, 0, 0, 0));

        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            warn_duplicate.setVisible(false);
            ok_msg.setVisible(false);

            Integer branchId = branch_cb.getValue() != null ? branch_cb.getValue().branchId : null;

            String result = Admin_Logic.registerAdmin(
                sys.getConn(),
                name.getText(), email.getText(), password.getText(), branchId
            );

            if (result.equals("empty")) warn_empty.setVisible(true);
            else if (result.equals("duplicate")) warn_duplicate.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear();
                email.clear();
                password.clear();
                branch_cb.getSelectionModel().clearSelection();
            }
        });

        form_box.getChildren().addAll(title, warn_empty, warn_duplicate, ok_msg,
                name, email, password, branch_cb, submit);
        return form_box;
    }

    // styled combobox preloaded with the branches from the database
    private ComboBox<Admin_Logic.BranchRow> branchCombo() {
        ComboBox<Admin_Logic.BranchRow> cb = new ComboBox<>();
        cb.setPromptText("Branch");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(7), null)));
        cb.setPrefHeight(35);
        cb.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Admin_Logic.BranchRow item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : item.name);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");
            }
        });
        List<Admin_Logic.BranchRow> branches = Admin_Logic.getBranches(sys.getConn());
        cb.setItems(FXCollections.observableArrayList(branches));
        return cb;
    }

    // styled text field that matches the look used in the auth form
    private TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Nunito", 17));
        tf.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(7), null)));
        tf.setStyle("-fx-text-fill: white;");
        return tf;
    }

    public HBox getScreen() {
        return screen;
    }
}

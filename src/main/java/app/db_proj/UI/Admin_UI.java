package app.db_proj.UI;

import app.db_proj.Admin_Logic;
import app.db_proj.Labels;
import app.db_proj.OrderDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Order;
import app.db_proj.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class Admin_UI {
    private StackPane screenRoot;
    private HBox screen;
    private VBox main_content, left_nav;
    private SystemHandling sys;
    private int adminBranchId = -1;

    private Button[] navBtns;

    // section roots
    private VBox branchSection, employeesSection, usersSection, itemsSection, suppliersSection, ordersSection;

    // card list containers
    private VBox branch_cards_box;
    private VBox emp_cards_box;
    private VBox customer_cards_box;
    private VBox all_emp_cards_box;
    private VBox item_cards_box;
    private VBox supplier_cards_box;
    private VBox order_cards_box;

    // branch section detail box (for admin's own branch)
    private VBox branch_detail_box;

    public Admin_UI(SystemHandling sys) {
        this.sys = sys;
        screenRoot = new StackPane();
        screen = new HBox(0);
        screen.setMaxWidth(Double.MAX_VALUE);
        screen.setMaxHeight(Double.MAX_VALUE);

        if (sys.getCurrentUserId() != null) {
            adminBranchId = Admin_Logic.getAdminBranchId(sys.getConn(), sys.getCurrentUserId());
        }

        make_main_content();
        make_left_nav();

        HBox.setMargin(left_nav, new Insets(14, 0, 14, 14));
        screen.getChildren().addAll(left_nav, main_content);
        screenRoot.getChildren().add(screen);
        select_section(0);
    }

    // ── LEFT NAV ─────────────────────────────────────────────────────────────

    private void make_left_nav() {
        left_nav = new VBox(6);
        left_nav.setPrefWidth(320);
        left_nav.setMinWidth(320);
        left_nav.setMaxWidth(320);
        left_nav.setPadding(new Insets(22, 16, 22, 16));
        left_nav.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(14), null
        )));
        left_nav.setMaxHeight(Double.MAX_VALUE);

        Label title = new Labels("Admin Panel",
            Font.font("Adwaita Mono", FontWeight.BOLD, 21),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        title.setPadding(new Insets(0, 0, 10, 4));

        Separator sep = new Separator();

        String[] names = {"Branch", "Employees", "Users", "Items", "Suppliers", "Orders"};
        navBtns = new Button[names.length];
        VBox btns = new VBox(4);
        btns.setPadding(new Insets(10, 0, 0, 0));

        for (int i = 0; i < names.length; i++) {
            Button b = makeNavBtn(names[i]);
            navBtns[i] = b;
            final int idx = i;
            b.setOnAction(e -> select_section(idx));
            btns.getChildren().add(b);
        }

        left_nav.getChildren().addAll(title, sep, btns);
    }

    private Button makeNavBtn(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 14));
        btn.setBackground(Background.EMPTY);
        btn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> {
            if (!Color.BLACK.equals(btn.getTextFill()))
                btn.setBackground(new Background(new BackgroundFill(
                    Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(8), null)));
        });
        btn.setOnMouseExited(e -> {
            if (!Color.BLACK.equals(btn.getTextFill()))
                btn.setBackground(Background.EMPTY);
        });
        return btn;
    }

    private void select_section(int idx) {
        for (Button b : navBtns) {
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        }
        navBtns[idx].setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(8), null)));
        navBtns[idx].setTextFill(Color.BLACK);

        switch (idx) {
            case 0 -> refresh_branches();
            case 1 -> refresh_branch_employees();
            case 2 -> refresh_customers();
            case 3 -> refresh_items();
            case 4 -> refresh_suppliers();
            case 5 -> refresh_orders();
        }

        VBox[] sections = {branchSection, employeesSection, usersSection, itemsSection, suppliersSection, ordersSection};
        main_content.getChildren().setAll(sections[idx]);
        VBox.setVgrow(sections[idx], Priority.ALWAYS);
    }

    // ── MAIN CONTENT ─────────────────────────────────────────────────────────

    private void make_main_content() {
        main_content = new VBox();
        main_content.setPadding(new Insets(0, 14, 14, 0));
        HBox.setHgrow(main_content, Priority.ALWAYS);
        main_content.setMaxHeight(Double.MAX_VALUE);

        build_branch_section();
        build_employees_section();
        build_users_section();
        build_items_section();
        build_suppliers_section();
        build_orders_section();
    }

    // ── BRANCH SECTION ───────────────────────────────────────────────────────

    private void build_branch_section() {
        branchSection = new VBox(12);
        branchSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(branchSection, Priority.ALWAYS);

        HBox header = sectionHeader("Branches",
            e -> showFormOverlay(make_branch_form()));

        branch_cards_box = new VBox(10);
        branch_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(branch_cards_box);

        branchSection.getChildren().addAll(header, scroll);
    }

    private void refresh_branches() {
        if (branch_cards_box == null) return;
        branch_cards_box.getChildren().clear();
        List<Admin_Logic.BranchDetailRow> rows = Admin_Logic.getAllBranches(sys.getConn());
        if (rows.isEmpty()) {
            branch_cards_box.getChildren().add(emptyLabel("No branches found"));
            return;
        }
        for (Admin_Logic.BranchDetailRow b : rows)
            branch_cards_box.getChildren().add(makeBranchCard(b));
    }

    // ── EMPLOYEES SECTION ────────────────────────────────────────────────────

    private void build_employees_section() {
        employeesSection = new VBox(12);
        employeesSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(employeesSection, Priority.ALWAYS);

        HBox header = sectionHeader("Employees",
            e -> showFormOverlay(makeRegistrationModalContent()));

        emp_cards_box = new VBox(10);
        emp_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(emp_cards_box);

        employeesSection.getChildren().addAll(header, scroll);
    }

    private void refresh_branch_employees() {
        if (emp_cards_box == null || adminBranchId == -1) return;
        emp_cards_box.getChildren().clear();
        List<Admin_Logic.EmployeeRow> rows =
            Admin_Logic.getEmployeesByBranch(sys.getConn(), adminBranchId);
        if (rows.isEmpty()) {
            emp_cards_box.getChildren().add(emptyLabel("No employees in this branch"));
            return;
        }
        for (Admin_Logic.EmployeeRow row : rows)
            emp_cards_box.getChildren().add(makeEmployeeCard(row));
    }

    // ── USERS SECTION ────────────────────────────────────────────────────────

    private void build_users_section() {
        usersSection = new VBox(12);
        usersSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(usersSection, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Labels("Users",
            Font.font("Adwaita Mono", FontWeight.BOLD, 28),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        header.getChildren().add(title);

        customer_cards_box = new VBox(10);
        customer_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(customer_cards_box);
        usersSection.getChildren().addAll(header, scroll);
    }

    private void refresh_customers() {
        if (customer_cards_box == null) return;
        customer_cards_box.getChildren().clear();
        List<Admin_Logic.CustomerRow> rows = Admin_Logic.getCustomers(sys.getConn());
        if (rows.isEmpty()) {
            customer_cards_box.getChildren().add(emptyLabel("No customers yet"));
            return;
        }
        for (Admin_Logic.CustomerRow row : rows)
            customer_cards_box.getChildren().add(makeCustomerCard(row));
    }

    private void refresh_all_employees() {
        if (all_emp_cards_box == null) return;
        all_emp_cards_box.getChildren().clear();
        List<Admin_Logic.EmployeeRow> rows = Admin_Logic.getEmployees(sys.getConn());
        if (rows.isEmpty()) {
            all_emp_cards_box.getChildren().add(emptyLabel("No employees yet"));
            return;
        }
        for (Admin_Logic.EmployeeRow row : rows)
            all_emp_cards_box.getChildren().add(makeEmployeeCard(row));
    }

    // ── ITEMS SECTION ────────────────────────────────────────────────────────

    private void build_items_section() {
        itemsSection = new VBox(12);
        itemsSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(itemsSection, Priority.ALWAYS);

        HBox header = sectionHeader("Items",
            e -> showFormOverlay(make_item_form()));

        item_cards_box = new VBox(10);
        item_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(item_cards_box);

        itemsSection.getChildren().addAll(header, scroll);
    }

    private void refresh_items() {
        if (item_cards_box == null) return;
        item_cards_box.getChildren().clear();
        List<Admin_Logic.ItemRow> rows = Admin_Logic.getItems(sys.getConn());
        if (rows.isEmpty()) {
            item_cards_box.getChildren().add(emptyLabel("No items yet"));
            return;
        }
        for (Admin_Logic.ItemRow row : rows)
            item_cards_box.getChildren().add(makeItemCard(row));
    }

    // ── CARD BUILDERS ────────────────────────────────────────────────────────

    private HBox makeBranchCard(Admin_Logic.BranchDetailRow b) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(35, 0.80, 0.70, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = cardTitle(b.name);
        Label loc  = cardSub(b.street + ", " + b.city + "  " + b.zip);

        info.getChildren().addAll(name, loc);

        Label badge = badge("Branch " + b.branchId, Color.hsb(35, 0.80, 0.70, 1));

        card.getChildren().addAll(accent, info, badge);
        return card;
    }

    private HBox makeEmployeeCard(Admin_Logic.EmployeeRow row) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(48, 1, 0.92, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = cardTitle(row.getName());

        HBox details = new HBox(18);
        details.setAlignment(Pos.CENTER_LEFT);

        Label role   = new Label(row.getRole());
        role.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        role.setTextFill(Color.hsb(48, 0.7, 0.88, 1));

        Label salary = new Label(String.format("₪ %.0f / mo", row.getSalary()));
        salary.setFont(Font.font("Nunito", 14));
        salary.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        Label hire   = new Label(row.getHireDate());
        hire.setFont(Font.font("Nunito", 13));
        hire.setTextFill(Color.hsb(30, 0.08, 0.58, 1));

        details.getChildren().addAll(role, salary, hire);
        info.getChildren().addAll(name, details);

        Label branchBadge = badge("Branch " + row.getBranchId(), Color.hsb(48, 1, 0.92, 1));
        branchBadge.setTextFill(Color.BLACK);

        card.getChildren().addAll(accent, info, branchBadge);
        return card;
    }

    private HBox makeCustomerCard(Admin_Logic.CustomerRow row) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(210, 0.58, 0.82, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name  = cardTitle(row.getName());
        Label email = cardSub(row.getEmail());

        info.getChildren().addAll(name, email);

        Label typeBadge = badge(row.getType(), Color.hsb(210, 0.58, 0.82, 1));

        card.getChildren().addAll(accent, info, typeBadge);
        return card;
    }

    private HBox makeItemCard(Admin_Logic.ItemRow row) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(120, 0.50, 0.72, 1));

        if (row.getImagePath() != null) {
            try {
                ImageView iv = new ImageView(new Image(new File(row.getImagePath()).toURI().toString()));
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                card.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name  = cardTitle(row.getName());
        Label price = new Label(String.format("₪ %.2f", row.getPrice()));
        price.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        price.setTextFill(Color.hsb(48, 1, 0.92, 1));

        info.getChildren().addAll(name, price);

        Label typeBadge = badge(row.getItemType(), Color.hsb(120, 0.50, 0.72, 1));
        typeBadge.setTextFill(Color.BLACK);

        card.getChildren().addAll(accent, info, typeBadge);
        return card;
    }

    // ── CARD PRIMITIVES ──────────────────────────────────────────────────────

    private HBox baseCard() {
        HBox card = new HBox(14);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.12, 0.36, 1), new CornerRadii(12), null)));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.43, 1), new CornerRadii(12), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.12, 0.36, 1), new CornerRadii(12), null))));
        return card;
    }

    private Region accentBar(Color color) {
        Region r = new Region();
        r.setPrefWidth(4);
        r.setPrefHeight(54);
        r.setMinHeight(54);
        r.setBackground(new Background(new BackgroundFill(color, new CornerRadii(2), null)));
        return r;
    }

    private Label cardTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 17));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Label cardSub(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Nunito", 14));
        l.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        return l;
    }

    private Label badge(String text, Color bg) {
        Label l = new Label(text);
        l.setFont(Font.font("Nunito", FontWeight.BOLD, 12));
        l.setTextFill(Color.WHITE);
        l.setPadding(new Insets(3, 9, 3, 9));
        l.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(8), null)));
        return l;
    }

    private Label emptyLabel(String text) {
        return new Labels(text, Font.font("Nunito", 16),
            Color.hsb(30, 0.12, 0.78, 1)).getLabel();
    }

    // ── SECTION HELPERS ──────────────────────────────────────────────────────

    private HBox sectionHeader(String titleText, javafx.event.EventHandler<javafx.event.ActionEvent> addAction) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Labels(titleText,
            Font.font("Adwaita Mono", FontWeight.BOLD, 28),
            Color.hsb(48, 1, 0.92, 1)).getLabel();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = makePlusButton();
        addBtn.setOnAction(addAction);

        header.getChildren().addAll(title, spacer, addBtn);
        return header;
    }

    private ScrollPane cardScroll(VBox contentBox) {
        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setMaxWidth(Double.MAX_VALUE);
        return scroll;
    }

    // ── OVERLAY FORM ─────────────────────────────────────────────────────────

    private void showFormOverlay(VBox formContent) {
        StackPane overlay = new StackPane();
        overlay.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0, 0.68), null, null)));
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) screenRoot.getChildren().remove(overlay);
        });

        VBox modal = new VBox(0);
        modal.setMaxWidth(440);
        modal.setMaxHeight(Region.USE_PREF_SIZE);
        modal.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(16), null)));
        modal.setEffect(new DropShadow(32, 0, 8, Color.hsb(0, 0, 0, 0.65)));

        HBox modalHeader = new HBox();
        modalHeader.setPadding(new Insets(14, 18, 14, 20));
        modalHeader.setAlignment(Pos.CENTER_LEFT);
        modalHeader.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.22, 0.17, 1), new CornerRadii(16, 16, 0, 0, false), null)));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setPadding(new Insets(4, 8, 4, 8));
        closeBtn.setOnAction(e -> screenRoot.getChildren().remove(overlay));
        closeBtn.setOnMouseEntered(e2 -> closeBtn.setTextFill(Color.WHITE));
        closeBtn.setOnMouseExited(e2 -> closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1)));
        modalHeader.getChildren().addAll(hSpacer, closeBtn);

        ScrollPane formScroll = new ScrollPane(formContent);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setMaxHeight(540);
        formScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        modal.getChildren().addAll(modalHeader, formScroll);
        overlay.getChildren().add(modal);
        screenRoot.getChildren().add(overlay);
    }

    // ── FORMS ────────────────────────────────────────────────────────────────

    private VBox make_branch_form() {
        VBox form = formShell();

        Label title = formTitle("Add Branch");
        Label warn_empty = statusLabel("Fill in all fields", Color.RED);
        Label ok_msg     = statusLabel("Branch added!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg    = statusLabel("An error occurred", Color.RED);

        TextField name   = formField("Branch Name");
        TextField street = formField("Street (e.g. 12 Al-Rashid St)");
        TextField city   = formField("City");
        TextField zip    = formField("ZIP Code");

        Button submit = submitBtn("Add Branch");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            ok_msg.setVisible(false);
            err_msg.setVisible(false);

            String result = Admin_Logic.addBranch(
                sys.getConn(), name.getText(), street.getText(), city.getText(), zip.getText());

            if (result.equals("empty"))   warn_empty.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); street.clear(); city.clear(); zip.clear();
                refresh_branches();
            } else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, warn_empty, ok_msg, err_msg, name, street, city, zip, submit);
        return form;
    }

    private VBox makeRegistrationModalContent() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(14));

        HBox toggle = new HBox(0);
        toggle.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0.18, 1), new CornerRadii(10), null)));
        toggle.setMaxWidth(Region.USE_PREF_SIZE);

        Button emp_btn   = new Button("Employee");
        Button admin_btn = new Button("Admin");
        for (Button b : new Button[]{emp_btn, admin_btn}) {
            b.setFont(Font.font("Nunito", 14));
            b.setPadding(new Insets(7, 18, 7, 18));
            b.setCursor(Cursor.HAND);
            b.setStyle("-fx-background-color: transparent;");
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            b.setOnMouseEntered(e -> { if (!Color.BLACK.equals(b.getTextFill())) b.setStyle("-fx-background-color: hsb(0, 0%, 28%); -fx-background-radius: 10;"); });
            b.setOnMouseExited(e -> { if (!Color.BLACK.equals(b.getTextFill())) b.setStyle("-fx-background-color: transparent;"); });
        }
        emp_btn.setStyle("-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 10;");
        emp_btn.setTextFill(Color.BLACK);
        toggle.getChildren().addAll(emp_btn, admin_btn);

        VBox form_holder = new VBox();
        VBox emp_form   = make_employee_form();
        VBox admin_form = make_admin_form();
        form_holder.getChildren().add(emp_form);

        emp_btn.setOnAction(e -> {
            set_active_toggle(emp_btn, admin_btn);
            form_holder.getChildren().setAll(emp_form);
        });
        admin_btn.setOnAction(e -> {
            set_active_toggle(admin_btn, emp_btn);
            form_holder.getChildren().setAll(admin_form);
        });

        content.getChildren().addAll(toggle, form_holder);
        return content;
    }

    private VBox make_employee_form() {
        VBox form = formShell();

        Label title      = formTitle("Register Employee");
        Label warn_empty = statusLabel("Fill in all fields", Color.RED);
        Label warn_dup   = statusLabel("Name or email already exists", Color.RED);
        Label ok_msg     = statusLabel("Employee added!", Color.hsb(120, 0.5, 0.85, 1));

        TextField name     = formField("Name");
        TextField email    = formField("Email");
        TextField password = formField("Password");
        TextField role     = formField("Role");
        TextField salary   = formField("Salary");

        DatePicker hire_date = new DatePicker();
        hire_date.setPromptText("Hire Date");
        hire_date.setStyle("-fx-background-color: hsb(35, 12%, 20%); -fx-font-size: 14px;");
        hire_date.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Admin_Logic.BranchRow> branch_cb = branchCombo();

        Button submit = submitBtn("Register Employee");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            warn_dup.setVisible(false);
            ok_msg.setVisible(false);

            Integer bId = branch_cb.getValue() != null ? branch_cb.getValue().branchId : null;
            String dateStr = hire_date.getValue() != null ? hire_date.getValue().toString() : "";

            String result = Admin_Logic.registerEmployee(
                sys.getConn(), name.getText(), email.getText(), password.getText(),
                role.getText(), salary.getText(), dateStr, bId);

            if (result.equals("empty"))          warn_empty.setVisible(true);
            else if (result.equals("duplicate")) warn_dup.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); email.clear(); password.clear();
                role.clear(); salary.clear();
                hire_date.setValue(null);
                branch_cb.getSelectionModel().clearSelection();
                refresh_branch_employees();
            }
        });

        form.getChildren().addAll(title, warn_empty, warn_dup, ok_msg,
            name, email, password, role, salary, hire_date, branch_cb, submit);
        return form;
    }

    private VBox make_admin_form() {
        VBox form = formShell();

        Label title      = formTitle("Register Admin");
        Label warn_empty = statusLabel("Fill in all fields", Color.RED);
        Label warn_dup   = statusLabel("Name or email already exists", Color.RED);
        Label ok_msg     = statusLabel("Admin added!", Color.hsb(120, 0.5, 0.85, 1));

        TextField name     = formField("Name");
        TextField email    = formField("Email");
        TextField password = formField("Password");
        ComboBox<Admin_Logic.BranchRow> branch_cb = branchCombo();

        Button submit = submitBtn("Register Admin");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            warn_dup.setVisible(false);
            ok_msg.setVisible(false);

            Integer bId = branch_cb.getValue() != null ? branch_cb.getValue().branchId : null;
            String result = Admin_Logic.registerAdmin(
                sys.getConn(), name.getText(), email.getText(), password.getText(), bId);

            if (result.equals("empty"))          warn_empty.setVisible(true);
            else if (result.equals("duplicate")) warn_dup.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); email.clear(); password.clear();
                branch_cb.getSelectionModel().clearSelection();
            }
        });

        form.getChildren().addAll(title, warn_empty, warn_dup, ok_msg,
            name, email, password, branch_cb, submit);
        return form;
    }

    private VBox make_customer_form() {
        VBox form = formShell();

        Label title      = formTitle("Add Customer");
        Label warn_empty = statusLabel("Fill in all fields", Color.RED);
        Label warn_dup   = statusLabel("Email already exists", Color.RED);
        Label ok_msg     = statusLabel("Customer added!", Color.hsb(120, 0.5, 0.85, 1));

        HBox typeToggle = new HBox(0);
        typeToggle.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0.18, 1), new CornerRadii(10), null)));
        typeToggle.setMaxWidth(Region.USE_PREF_SIZE);

        Button persBtn = new Button("Personal");
        Button bizBtn  = new Button("Business");
        for (Button b : new Button[]{persBtn, bizBtn}) {
            b.setFont(Font.font("Nunito", 14));
            b.setPadding(new Insets(6, 16, 6, 16));
            b.setCursor(Cursor.HAND);
            b.setStyle("-fx-background-color: transparent;");
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            b.setOnMouseEntered(e -> { if (!Color.BLACK.equals(b.getTextFill())) b.setStyle("-fx-background-color: hsb(0, 0%, 28%); -fx-background-radius: 10;"); });
            b.setOnMouseExited(e -> { if (!Color.BLACK.equals(b.getTextFill())) b.setStyle("-fx-background-color: transparent;"); });
        }
        persBtn.setStyle("-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 10;");
        persBtn.setTextFill(Color.BLACK);
        typeToggle.getChildren().addAll(persBtn, bizBtn);

        String[] selectedType = {"Personal"};

        persBtn.setOnAction(e -> {
            set_active_toggle(persBtn, bizBtn);
            selectedType[0] = "Personal";
        });
        bizBtn.setOnAction(e -> {
            set_active_toggle(bizBtn, persBtn);
            selectedType[0] = "Business";
        });

        TextField name     = formField("Name");
        TextField email    = formField("Email");
        TextField password = formField("Password");

        Button submit = submitBtn("Add Customer");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            warn_dup.setVisible(false);
            ok_msg.setVisible(false);

            String result = Admin_Logic.addCustomer(
                sys.getConn(), name.getText(), email.getText(), password.getText(), selectedType[0]);

            if (result.equals("empty"))          warn_empty.setVisible(true);
            else if (result.equals("duplicate")) warn_dup.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); email.clear(); password.clear();
                refresh_customers();
            }
        });

        form.getChildren().addAll(title, warn_empty, warn_dup, ok_msg,
            typeToggle, name, email, password, submit);
        return form;
    }

    private VBox make_item_form() {
        VBox form = formShell();

        Label title      = formTitle("Add Item");
        Label warn_empty = statusLabel("Fill in all fields", Color.RED);
        Label warn_price = statusLabel("Price must be a number", Color.RED);
        Label ok_msg     = statusLabel("Item added!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg    = statusLabel("An error occurred", Color.RED);

        TextField name      = formField("Item Name");
        TextField price     = formField("Price (e.g. 12.50)");
        TextField item_type = formField("Type (e.g. Food, Craft)");

        // image chooser row
        String btnNormal = "-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        String btnHover  = "-fx-background-color: hsb(35, 16%, 38%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        Button chooserBtn = new Button("Choose Image");
        chooserBtn.setStyle(btnNormal);
        chooserBtn.setCursor(Cursor.HAND);
        chooserBtn.setMaxWidth(Double.MAX_VALUE);
        chooserBtn.setOnMouseEntered(e -> chooserBtn.setStyle(btnHover));
        chooserBtn.setOnMouseExited(e -> chooserBtn.setStyle(btnNormal));

        Label imageLbl = new Label("No image selected");
        imageLbl.setFont(Font.font("Nunito", 13));
        imageLbl.setTextFill(Color.hsb(30, 0.10, 0.55, 1));

        ImageView preview = new ImageView();
        preview.setFitWidth(80);
        preview.setFitHeight(80);
        preview.setPreserveRatio(true);
        preview.setVisible(false);
        preview.setManaged(false);

        String[] imagePath = {null};
        chooserBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Item Image");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
            File file = fc.showOpenDialog(chooserBtn.getScene().getWindow());
            if (file != null) {
                imagePath[0] = file.getAbsolutePath();
                imageLbl.setText(file.getName());
                preview.setImage(new Image(file.toURI().toString()));
                preview.setVisible(true);
                preview.setManaged(true);
            }
        });

        Button submit = submitBtn("Add Item");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            warn_price.setVisible(false);
            ok_msg.setVisible(false);
            err_msg.setVisible(false);

            String result = Admin_Logic.addItem(
                sys.getConn(), name.getText(), price.getText(), item_type.getText(), imagePath[0]);

            if (result.equals("empty"))            warn_empty.setVisible(true);
            else if (result.equals("price_error")) warn_price.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); price.clear(); item_type.clear();
                imagePath[0] = null;
                imageLbl.setText("No image selected");
                preview.setImage(null);
                preview.setVisible(false);
                preview.setManaged(false);
                refresh_items();
            } else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, warn_empty, warn_price, ok_msg, err_msg,
            name, price, item_type, chooserBtn, imageLbl, preview, submit);
        return form;
    }

    // ── FORM PRIMITIVES ──────────────────────────────────────────────────────

    private VBox formShell() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(14));
        v.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(0, 0, 12, 12, false), null)));
        return v;
    }

    private Label formTitle(String text) {
        Label l = new Labels(text,
            Font.font("Adwaita Mono", FontWeight.BOLD, 18),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        l.setPadding(new Insets(0, 0, 4, 0));
        return l;
    }

    private Label statusLabel(String text, Color color) {
        Label l = new Label(text);
        l.setFont(Font.font("Nunito", 15));
        l.setTextFill(color);
        l.setVisible(false);
        l.managedProperty().bind(l.visibleProperty());
        return l;
    }

    private TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Nunito", 15));
        tf.setStyle(
            "-fx-control-inner-background: #1a1a1a; " +
            "-fx-text-fill: #d4c0a0; " +
            "-fx-prompt-text-fill: #5a4a38; " +
            "-fx-background-radius: 7; " +
            "-fx-font-size: 15px;"
        );
        tf.setPadding(new Insets(7, 10, 7, 10));
        return tf;
    }

    private Button submitBtn(String text) {
        String normal = "-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 12; -fx-font-size: 17px; -fx-text-fill: black;";
        String hover  = "-fx-background-color: hsb(48, 100%, 75%); -fx-background-radius: 12; -fx-font-size: 17px; -fx-text-fill: black;";
        Button btn = new Button(text);
        btn.setStyle(normal);
        btn.setFont(Font.font("Nunito", FontWeight.BOLD, 17));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        VBox.setMargin(btn, new Insets(6, 0, 0, 0));
        return btn;
    }

    private Button makePlusButton() {
        Button btn = new Button("+ Add");
        btn.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        btn.setPadding(new Insets(8, 20, 8, 20));
        btn.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
        btn.setTextFill(Color.BLACK);
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.75, 1), new CornerRadii(10), null))));
        btn.setOnMouseExited(e -> btn.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null))));
        return btn;
    }

    private ComboBox<Admin_Logic.BranchRow> branchCombo() {
        ComboBox<Admin_Logic.BranchRow> cb = new ComboBox<>();
        cb.setPromptText("Branch");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.12, 0.20, 1), new CornerRadii(7), null)));
        cb.setPrefHeight(36);
        cb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Admin_Logic.BranchRow item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : item.name);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 15px; -fx-background-color: transparent;");
            }
        });
        cb.setItems(FXCollections.observableArrayList(Admin_Logic.getBranches(sys.getConn())));
        return cb;
    }

    // ── SUPPLIERS SECTION ────────────────────────────────────────────────────

    private void build_suppliers_section() {
        suppliersSection = new VBox(12);
        suppliersSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(suppliersSection, Priority.ALWAYS);

        HBox header = sectionHeader("Suppliers",
            e -> showFormOverlay(make_supplier_form()));

        supplier_cards_box = new VBox(10);
        supplier_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(supplier_cards_box);
        suppliersSection.getChildren().addAll(header, scroll);
    }

    private void refresh_suppliers() {
        if (supplier_cards_box == null) return;
        supplier_cards_box.getChildren().clear();
        List<Admin_Logic.SupplierRow> rows = Admin_Logic.getSuppliers(sys.getConn());
        if (rows.isEmpty()) {
            supplier_cards_box.getChildren().add(emptyLabel("No suppliers yet"));
            return;
        }
        for (Admin_Logic.SupplierRow row : rows)
            supplier_cards_box.getChildren().add(makeSupplierCard(row));
    }

    private HBox makeSupplierCard(Admin_Logic.SupplierRow row) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(200, 0.55, 0.75, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name  = cardTitle(row.getName());
        Label email = cardSub(row.getEmail() != null ? row.getEmail() : "");
        info.getChildren().addAll(name, email);

        String phoneText = (row.getPhone() != null && !row.getPhone().isBlank())
            ? row.getPhone() : "—";
        Label phoneBadge = badge(phoneText, Color.hsb(200, 0.55, 0.75, 1));

        card.getChildren().addAll(accent, info, phoneBadge);
        return card;
    }

    private VBox make_supplier_form() {
        VBox form = formShell();

        Label title      = formTitle("Add Supplier");
        Label warn_empty = statusLabel("Name and email are required", Color.RED);
        Label ok_msg     = statusLabel("Supplier added!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg    = statusLabel("An error occurred", Color.RED);

        TextField name  = formField("Supplier Name");
        TextField email = formField("Email");
        TextField phone = formField("Phone (optional)");

        Button submit = submitBtn("Add Supplier");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false);
            ok_msg.setVisible(false);
            err_msg.setVisible(false);

            String result = Admin_Logic.addSupplier(
                sys.getConn(), name.getText(), email.getText(), phone.getText());

            if (result.equals("empty"))  warn_empty.setVisible(true);
            else if (result.equals("ok")) {
                ok_msg.setVisible(true);
                name.clear(); email.clear(); phone.clear();
                refresh_suppliers();
            } else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, warn_empty, ok_msg, err_msg,
            name, email, phone, submit);
        return form;
    }

    // ── ORDERS SECTION ───────────────────────────────────────────────────────

    private void build_orders_section() {
        ordersSection = new VBox(12);
        ordersSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(ordersSection, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Labels("Orders",
            Font.font("Adwaita Mono", FontWeight.BOLD, 28),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        header.getChildren().add(title);

        order_cards_box = new VBox(10);
        order_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(order_cards_box);
        ordersSection.getChildren().addAll(header, scroll);
    }

    private void refresh_orders() {
        if (order_cards_box == null) return;
        order_cards_box.getChildren().clear();
        List<Order> orders = OrderDAO.getAllOrders(sys.getConn());
        if (orders.isEmpty()) {
            order_cards_box.getChildren().add(emptyLabel("No orders yet"));
            return;
        }
        for (Order order : orders)
            order_cards_box.getChildren().add(makeOrderCard(order));
    }

    private HBox makeOrderCard(Order order) {
        HBox card = baseCard();

        Region accent = accentBar(statusColor(order.status));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label idLabel = cardTitle("#" + order.orderId + "  –  " + order.personName);
        Label sub = cardSub(order.orderDate.toLocalDate() + "   |   ₪ " + String.format("%.2f", order.total));
        info.getChildren().addAll(idLabel, sub);

        Label statusBadge = badge(order.status, statusColor(order.status));
        if (order.status.equals("delivered")) statusBadge.setTextFill(Color.BLACK);

        card.setOnMouseClicked(e -> showOrderDetail(order));
        card.getChildren().addAll(accent, info, statusBadge);
        return card;
    }

    private void showOrderDetail(Order order) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(14));

        Label title = formTitle("Order #" + order.orderId);

        Label customerLbl = new Label("Customer:  " + order.personName);
        customerLbl.setFont(Font.font("Nunito", 15));
        customerLbl.setTextFill(Color.hsb(30, 0.12, 0.78, 1));

        Label dateLbl = new Label("Date:  " + order.orderDate.toLocalDate());
        dateLbl.setFont(Font.font("Nunito", 15));
        dateLbl.setTextFill(Color.hsb(30, 0.12, 0.78, 1));

        Label totalLbl = new Label(String.format("Total:  ₪ %.2f", order.total));
        totalLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        totalLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));

        Separator sep = new Separator();

        Label itemsHeading = new Label("Items");
        itemsHeading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 14));
        itemsHeading.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        VBox itemsList = new VBox(6);
        for (OrderItem oi : order.items) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setBackground(new Background(new BackgroundFill(
                Color.hsb(35, 0.10, 0.28, 1), new CornerRadii(8), null)));

            Label nameLbl = new Label(oi.itemName);
            nameLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
            nameLbl.setTextFill(Color.WHITE);
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label qtyLbl = new Label("×" + oi.quantity);
            qtyLbl.setFont(Font.font("Nunito", 14));
            qtyLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));

            Label priceLbl = new Label(String.format("₪ %.2f", oi.unitPrice * oi.quantity));
            priceLbl.setFont(Font.font("Nunito", 14));
            priceLbl.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

            row.getChildren().addAll(nameLbl, qtyLbl, priceLbl);
            itemsList.getChildren().add(row);
        }

        Separator sep2 = new Separator();

        // Status update row
        Label statusHeading = new Label("Update Status");
        statusHeading.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        statusHeading.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        javafx.scene.control.ComboBox<String> statusBox = new javafx.scene.control.ComboBox<>(
            javafx.collections.FXCollections.observableArrayList("pending", "processing", "shipped", "delivered", "cancelled")
        );
        statusBox.setValue(order.status);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle("-fx-background-color: hsb(35, 12%, 20%); -fx-font-size: 14px;");

        Label feedbackLbl = statusLabel("Status updated!", Color.hsb(120, 0.5, 0.85, 1));

        Button updateBtn = submitBtn("Update Status");
        updateBtn.setOnAction(e -> {
            boolean ok = OrderDAO.updateOrderStatus(sys.getConn(), order.orderId, statusBox.getValue());
            if (ok) { feedbackLbl.setVisible(true); refresh_orders(); }
        });

        content.getChildren().addAll(
            title, customerLbl, dateLbl, totalLbl,
            sep, itemsHeading, itemsList,
            sep2, statusHeading, statusBox, feedbackLbl, updateBtn
        );
        showFormOverlay(content);
    }

    private Color statusColor(String status) {
        return switch (status) {
            case "pending"    -> Color.hsb(30, 0.85, 0.85, 1);
            case "processing" -> Color.hsb(48, 0.90, 0.88, 1);
            case "shipped"    -> Color.hsb(200, 0.65, 0.80, 1);
            case "delivered"  -> Color.hsb(120, 0.50, 0.72, 1);
            case "cancelled"  -> Color.hsb(0, 0.70, 0.72, 1);
            default           -> Color.hsb(0, 0, 0.55, 1);
        };
    }

    // ── SHARED HELPERS ───────────────────────────────────────────────────────

    private void set_active_toggle(Button active, Button inactive) {
        active.setStyle("-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 10;");
        active.setTextFill(Color.BLACK);
        inactive.setStyle("-fx-background-color: transparent;");
        inactive.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
    }

    public StackPane getScreen() {
        return screenRoot;
    }
}

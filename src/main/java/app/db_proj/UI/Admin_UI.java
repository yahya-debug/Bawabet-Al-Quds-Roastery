package app.db_proj.UI;

import app.db_proj.Admin_Logic;
import app.db_proj.Labels;
import app.db_proj.OrderDAO;
import app.db_proj.PackageDAO;
import app.db_proj.RoastBatchDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Order;
import app.db_proj.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.util.Callback;
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
import java.util.ArrayList;
import java.util.List;

public class Admin_UI {
    private StackPane screenRoot;
    private HBox screen;
    private VBox main_content, left_nav;
    private SystemHandling sys;
    private int adminBranchId = -1;
    private boolean isFullAdmin;

    private Button[] navBtns;
    private int[] sectionIndices;

    // section roots
    private VBox branchSection, employeesSection, usersSection, itemsSection,
                 suppliersSection, ordersSection, reportsSection, packagesSection, roastBatchSection;

    // card list containers
    private VBox branch_cards_box;
    private VBox emp_cards_box;
    private VBox customer_cards_box;
    private VBox all_emp_cards_box;
    private VBox item_cards_box;
    private VBox supplier_cards_box;
    private VBox order_cards_box;
    private VBox package_cards_box;
    private VBox roast_batch_cards_box;

    // branch section detail box (for admin's own branch)
    private VBox branch_detail_box;

    public Admin_UI(SystemHandling sys) {
        this.sys = sys;
        screenRoot = new StackPane();
        screen = new HBox(0);
        screen.setMaxWidth(Double.MAX_VALUE);
        screen.setMaxHeight(Double.MAX_VALUE);

        if (sys.getCurrentUserId() != null) {
            isFullAdmin = sys.isUserAdmin();
            adminBranchId = isFullAdmin
                ? Admin_Logic.getAdminBranchId(sys.getConn(), sys.getCurrentUserId())
                : Admin_Logic.getEmployeeBranchId(sys.getConn(), sys.getCurrentUserId());
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

        String panelTitle = isFullAdmin ? "Admin Panel" : "Employee Portal";
        Label title = new Labels(panelTitle,
            Font.font("Adwaita Mono", FontWeight.BOLD, 21),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        title.setPadding(new Insets(0, 0, 10, 4));

        Separator sep = new Separator();

        String[] names;
        if (isFullAdmin) {
            names = new String[]{"Branch", "Employees", "Users", "Items", "Suppliers", "Orders", "Reports", "Packages", "Roast Batches"};
            sectionIndices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        } else {
            names = new String[]{"Branch", "Employees", "Suppliers", "Orders"};
            sectionIndices = new int[]{0, 1, 4, 5};
        }
        navBtns = new Button[names.length];
        VBox btns = new VBox(4);
        btns.setPadding(new Insets(10, 0, 0, 0));

        for (int i = 0; i < names.length; i++) {
            Button b = makeNavBtn(names[i]);
            navBtns[i] = b;
            final int secIdx = sectionIndices[i];
            b.setOnAction(e -> select_section(secIdx));
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

    private void select_section(int sectionIdx) {
        for (Button b : navBtns) {
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        }
        for (int i = 0; i < sectionIndices.length; i++) {
            if (sectionIndices[i] == sectionIdx) {
                navBtns[i].setBackground(new Background(new BackgroundFill(
                    Color.hsb(48, 1, 0.92, 1), new CornerRadii(8), null)));
                navBtns[i].setTextFill(Color.BLACK);
                break;
            }
        }

        switch (sectionIdx) {
            case 0 -> refresh_branches();
            case 1 -> refresh_branch_employees();
            case 2 -> refresh_customers();
            case 3 -> refresh_items();
            case 4 -> refresh_suppliers();
            case 5 -> refresh_orders();
            case 6 -> refresh_reports();
            case 7 -> refresh_packages();
            case 8 -> refresh_roast_batches();
        }

        VBox[] sections = {branchSection, employeesSection, usersSection, itemsSection,
                           suppliersSection, ordersSection, reportsSection, packagesSection, roastBatchSection};
        main_content.getChildren().setAll(sections[sectionIdx]);
        VBox.setVgrow(sections[sectionIdx], Priority.ALWAYS);
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
        build_reports_section();
        build_packages_section();
        build_roast_batches_section();
    }

    // ── BRANCH SECTION ───────────────────────────────────────────────────────

    private void build_branch_section() {
        branchSection = new VBox(12);
        branchSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(branchSection, Priority.ALWAYS);

        HBox header = isFullAdmin
            ? sectionHeader("Branches", e -> showFormOverlay(make_branch_form()))
            : sectionTitleOnly("My Branch");

        branch_cards_box = new VBox(10);
        branch_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(branch_cards_box);

        branchSection.getChildren().addAll(header, scroll);
    }

    private void refresh_branches() {
        if (branch_cards_box == null) return;
        branch_cards_box.getChildren().clear();

        if (!isFullAdmin) {
            if (adminBranchId == -1) {
                branch_cards_box.getChildren().add(emptyLabel("No branch assigned"));
                return;
            }
            Admin_Logic.BranchDetailRow b = Admin_Logic.getBranchDetail(sys.getConn(), adminBranchId);
            if (b != null) branch_cards_box.getChildren().add(makeBranchCard(b));
            return;
        }

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
        card.setOnMouseClicked(e -> showBranchDetail(b));
        return card;
    }

    private void showBranchDetail(Admin_Logic.BranchDetailRow b) {
        StackPane overlay = new StackPane();
        overlay.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0, 0.68), null, null)));
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) screenRoot.getChildren().remove(overlay);
        });
        StackPane.setMargin(overlay, Insets.EMPTY);

        VBox modal = new VBox(0);
        modal.setMaxWidth(700);
        modal.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(16), null)));
        modal.setEffect(new DropShadow(32, 0, 8, Color.hsb(0, 0, 0, 0.65)));
        StackPane.setMargin(modal, new Insets(30));

        // header
        HBox modalHeader = new HBox();
        modalHeader.setPadding(new Insets(14, 18, 14, 20));
        modalHeader.setAlignment(Pos.CENTER_LEFT);
        modalHeader.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.22, 0.17, 1), new CornerRadii(16, 16, 0, 0, false), null)));
        Label titleLbl = new Label(b.name);
        titleLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        titleLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> screenRoot.getChildren().remove(overlay));
        closeBtn.setOnMouseEntered(e -> closeBtn.setTextFill(Color.WHITE));
        closeBtn.setOnMouseExited(e -> closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1)));
        modalHeader.getChildren().addAll(titleLbl, hSpacer, closeBtn);

        // body
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 18, 18, 18));

        Label locLbl = new Label(b.street + ", " + b.city + "  " + b.zip);
        locLbl.setFont(Font.font("Nunito", 14));
        locLbl.setTextFill(Color.hsb(30, 0.12, 0.65, 1));
        body.getChildren().add(locLbl);

        // ── Roast Batches for this branch ─────────────────────────────────────
        body.getChildren().add(new Separator());
        HBox batchHeader = new HBox(8);
        batchHeader.setAlignment(Pos.CENTER_LEFT);
        Label batchHeadLbl = new Label("Roast Batches");
        batchHeadLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        batchHeadLbl.setTextFill(Color.hsb(25, 0.75, 0.80, 1));
        HBox.setHgrow(batchHeadLbl, Priority.ALWAYS);
        Button addBatchBtn = new Button("+ New Batch");
        addBatchBtn.setStyle("-fx-background-color: hsb(25, 55%, 60%); -fx-background-radius: 7;"
            + "-fx-font-size: 12px; -fx-text-fill: white;");
        addBatchBtn.setCursor(Cursor.HAND);
        addBatchBtn.setOnAction(ev -> showFormOverlay(make_roast_batch_form(b.branchId)));
        batchHeader.getChildren().addAll(batchHeadLbl, addBatchBtn);
        body.getChildren().add(batchHeader);

        List<RoastBatchDAO.RoastBatchRow> batches = RoastBatchDAO.getByBranch(sys.getConn(), b.branchId);
        if (batches.isEmpty()) {
            Label none = new Label("No roast batches recorded for this branch");
            none.setFont(Font.font("Nunito", 13));
            none.setTextFill(Color.hsb(30, 0.10, 0.55, 1));
            body.getChildren().add(none);
        } else {
            for (RoastBatchDAO.RoastBatchRow rb : batches) {
                HBox bRow = new HBox(10);
                bRow.setAlignment(Pos.CENTER_LEFT);
                bRow.setPadding(new Insets(8, 12, 8, 12));
                bRow.setBackground(new Background(new BackgroundFill(
                    Color.hsb(25, 0.18, 0.28, 1), new CornerRadii(8), null)));

                VBox bInfo = new VBox(2);
                HBox.setHgrow(bInfo, Priority.ALWAYS);
                Label bId = new Label("Batch #" + rb.batchId + "  ·  " + rb.roastDate);
                bId.setFont(Font.font("Nunito", FontWeight.BOLD, 13));
                bId.setTextFill(Color.WHITE);
                Label bDet = new Label(rb.roastLevel + "  ·  " + rb.kgGreen + " kg green → " + rb.kgRoasted + " kg roasted");
                bDet.setFont(Font.font("Nunito", 12));
                bDet.setTextFill(Color.hsb(30, 0.12, 0.65, 1));
                bInfo.getChildren().addAll(bId, bDet);

                Button editBatch = new Button("Edit");
                editBatch.setStyle("-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 6;"
                    + "-fx-font-size: 12px; -fx-text-fill: #d4c0a0;");
                editBatch.setCursor(Cursor.HAND);
                editBatch.setOnAction(ev -> showFormOverlay(make_roast_batch_edit_form(rb)));

                bRow.getChildren().addAll(bInfo, editBatch);
                body.getChildren().add(bRow);
            }
        }
        body.getChildren().add(new Separator());

        // ── Stock ─────────────────────────────────────────────────────────────
        List<Admin_Logic.StockRow> stock = Admin_Logic.getBranchStock(sys.getConn(), b.branchId);
        List<Admin_Logic.StockRow> needsRefill = new ArrayList<>();
        List<Admin_Logic.StockRow> inStock     = new ArrayList<>();
        for (Admin_Logic.StockRow s : stock) {
            if (s.getQuantity() == 0) needsRefill.add(s);
            else inStock.add(s);
        }

        if (!needsRefill.isEmpty()) {
            Label head = new Label("⚠  Needs Refill");
            head.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
            head.setTextFill(Color.hsb(0, 0.8, 0.85, 1));
            VBox box = new VBox(6);
            for (Admin_Logic.StockRow s : needsRefill)
                box.getChildren().add(makeStockRow(s, b.branchId));
            body.getChildren().addAll(head, box);
        }

        if (!inStock.isEmpty()) {
            Label head = new Label("In Stock");
            head.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
            head.setTextFill(Color.hsb(120, 0.5, 0.72, 1));
            VBox box = new VBox(6);
            for (Admin_Logic.StockRow s : inStock)
                box.getChildren().add(makeStockRow(s, b.branchId));
            body.getChildren().addAll(head, box);
        }

        if (stock.isEmpty()) {
            body.getChildren().add(emptyLabel("No items stocked at this branch yet"));
        }

        // add catalog item to branch
        body.getChildren().add(new Separator());
        Label addHead = new Label("Add Item to Branch");
        addHead.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        addHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        ComboBox<Admin_Logic.ItemRow> itemCombo = new ComboBox<>();
        itemCombo.setMaxWidth(Double.MAX_VALUE);
        itemCombo.setPromptText("Select Item from Catalog");
        itemCombo.getItems().addAll(Admin_Logic.getItemsNotAtBranch(sys.getConn(), b.branchId));

        Callback<ListView<Admin_Logic.ItemRow>, ListCell<Admin_Logic.ItemRow>> cellFactory = lv -> new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                if (empty || it == null) { setText(null); }
                else {
                    setText("#" + it.getItemId() + "  –  " + it.getName());
                    setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                    setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
                }
            }
        };
        itemCombo.setCellFactory(cellFactory);
        itemCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it == null ? "Select Item from Catalog"
                    : "#" + it.getItemId() + "  –  " + it.getName());
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
            }
        });

        TextField addQty = formField("Quantity");
        Label addErr = statusLabel("Select an item and enter a valid quantity", Color.RED);
        Label addOk  = statusLabel("Item added to branch!", Color.hsb(120, 0.5, 0.85, 1));

        Button addBtn = submitBtn("Add to Branch");
        addBtn.setOnAction(e -> {
            addErr.setVisible(false); addOk.setVisible(false);
            Admin_Logic.ItemRow sel = itemCombo.getValue();
            if (sel == null || addQty.getText().isBlank()) { addErr.setVisible(true); return; }
            try {
                int qty = Integer.parseInt(addQty.getText().trim());
                if (qty <= 0) { addErr.setVisible(true); return; }
                Admin_Logic.setStock(sys.getConn(), b.branchId, sel.getItemId(), qty);
                addOk.setVisible(true);
                itemCombo.getItems().remove(sel);
                itemCombo.setValue(null);
                addQty.clear();
            } catch (NumberFormatException ex) { addErr.setVisible(true); }
        });

        body.getChildren().addAll(addHead, itemCombo, addQty, addErr, addOk, addBtn);

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxHeight(580);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        modal.getChildren().addAll(modalHeader, scroll);
        overlay.getChildren().add(modal);
        screenRoot.getChildren().add(overlay);
    }

    private HBox makeStockRow(Admin_Logic.StockRow s, int branchId) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.10, 0.28, 1), new CornerRadii(8), null)));

        Label name = new Label(s.getItemName());
        name.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        name.setTextFill(Color.WHITE);
        HBox.setHgrow(name, Priority.ALWAYS);

        Color qtyColor = s.getQuantity() == 0
            ? Color.hsb(0, 0.8, 0.85, 1) : Color.hsb(120, 0.5, 0.72, 1);
        Label qtyBadge = badge(s.getQuantity() + " in stock", qtyColor);

        TextField qtyField = new TextField(String.valueOf(s.getQuantity()));
        qtyField.setPrefWidth(70);
        qtyField.setFont(Font.font("Nunito", 13));
        qtyField.setStyle("-fx-control-inner-background: hsb(35, 12%, 18%);"
            + "-fx-text-fill: #d4c0a0; -fx-background-radius: 6; -fx-font-size: 13px;");

        String setNorm = "-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7;"
                       + "-fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        String setHov  = "-fx-background-color: hsb(35, 16%, 40%); -fx-background-radius: 7;"
                       + "-fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        Button setBtn = new Button("Set");
        setBtn.setStyle(setNorm); setBtn.setCursor(Cursor.HAND);
        setBtn.setOnMouseEntered(e -> setBtn.setStyle(setHov));
        setBtn.setOnMouseExited(e -> setBtn.setStyle(setNorm));
        setBtn.setOnAction(ev -> {
            try {
                int newQty = Integer.parseInt(qtyField.getText().trim());
                Admin_Logic.setStock(sys.getConn(), branchId, s.getItemId(), newQty);
                qtyBadge.setText(newQty + " in stock");
                Color c = newQty == 0 ? Color.hsb(0, 0.8, 0.85, 1) : Color.hsb(120, 0.5, 0.72, 1);
                qtyBadge.setBackground(new Background(new BackgroundFill(c, new CornerRadii(8), null)));
            } catch (NumberFormatException ignored) {}
        });

        row.getChildren().addAll(name, qtyBadge, qtyField, setBtn);
        return row;
    }

    // ── ROAST BATCHES SECTION ────────────────────────────────────────────────

    private void build_roast_batches_section() {
        roastBatchSection = new VBox(12);
        roastBatchSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(roastBatchSection, Priority.ALWAYS);

        HBox header = sectionHeader("Roast Batches", e -> showFormOverlay(make_roast_batch_form(-1)));

        roast_batch_cards_box = new VBox(10);
        roast_batch_cards_box.setPadding(new Insets(2, 0, 10, 0));

        ScrollPane scroll = cardScroll(roast_batch_cards_box);
        roastBatchSection.getChildren().addAll(header, scroll);
    }

    private void refresh_roast_batches() {
        if (roast_batch_cards_box == null) return;
        roast_batch_cards_box.getChildren().clear();
        List<RoastBatchDAO.RoastBatchRow> rows = RoastBatchDAO.getAll(sys.getConn());
        if (rows.isEmpty()) {
            roast_batch_cards_box.getChildren().add(emptyLabel("No roast batches yet"));
            return;
        }
        for (RoastBatchDAO.RoastBatchRow row : rows)
            roast_batch_cards_box.getChildren().add(makeRoastBatchCard(row));
    }

    private HBox makeRoastBatchCard(RoastBatchDAO.RoastBatchRow row) {
        HBox card = baseCard();

        Region accent = accentBar(Color.hsb(25, 0.75, 0.65, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label batchLbl = cardTitle("Batch #" + row.batchId + "  –  " + row.branchName);
        Label details  = cardSub(row.roastDate + "  ·  " + row.roastLevel
            + "  ·  " + row.kgGreen + " kg green → " + row.kgRoasted + " kg roasted");
        info.getChildren().addAll(batchLbl, details);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7;"
            + "-fx-font-size: 13px; -fx-text-fill: #d4c0a0;");
        editBtn.setCursor(Cursor.HAND);
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: hsb(35, 16%, 40%);"
            + "-fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: hsb(35, 14%, 30%);"
            + "-fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;"));
        editBtn.setOnAction(e -> showFormOverlay(make_roast_batch_edit_form(row)));

        card.getChildren().addAll(accent, info, editBtn);
        return card;
    }

    private VBox make_roast_batch_form(int forcedBranchId) {
        VBox form = formShell();

        Label title   = formTitle(forcedBranchId > 0 ? "New Roast Batch" : "Add Roast Batch");
        Label ok_msg  = statusLabel("Batch added!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg = statusLabel("Fill all fields with valid values", Color.RED);

        // Branch selector (only shown for full admin adding from the global section)
        ComboBox<Admin_Logic.BranchRow> branchSel = new ComboBox<>();
        if (forcedBranchId < 0 && isFullAdmin) {
            branchSel.setMaxWidth(Double.MAX_VALUE);
            branchSel.setPromptText("Select Branch");
            branchSel.getItems().addAll(Admin_Logic.getBranches(sys.getConn()));
            branchSel.setBackground(new Background(new BackgroundFill(
                Color.hsb(35, 0.12, 0.20, 1), new CornerRadii(7), null)));
            branchSel.setPrefHeight(36);
        }

        TextField dateField      = formField("Roast Date (YYYY-MM-DD)");
        TextField kgGreenField   = formField("Kg Green (e.g. 50.0)");
        TextField kgRoastedField = formField("Kg Roasted (e.g. 42.5)");

        ComboBox<String> levelCombo = roastLevelCombo(null);

        Button submit = submitBtn("Add Batch");
        submit.setOnAction(e -> {
            ok_msg.setVisible(false); err_msg.setVisible(false);
            int targetBranch = forcedBranchId > 0 ? forcedBranchId
                : (branchSel.getValue() != null ? branchSel.getValue().branchId : -1);
            if (targetBranch < 0 || levelCombo.getValue() == null) { err_msg.setVisible(true); return; }
            int id = RoastBatchDAO.addBatch(sys.getConn(), targetBranch,
                dateField.getText(), kgGreenField.getText(), kgRoastedField.getText(), levelCombo.getValue());
            if (id > 0) {
                ok_msg.setVisible(true);
                dateField.clear(); kgGreenField.clear(); kgRoastedField.clear();
                levelCombo.setValue(null);
                if (forcedBranchId < 0) refresh_roast_batches();
            } else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, ok_msg, err_msg);
        if (forcedBranchId < 0 && isFullAdmin) form.getChildren().add(branchSel);
        form.getChildren().addAll(dateField, kgGreenField, kgRoastedField, levelCombo, submit);
        return form;
    }

    private VBox make_roast_batch_edit_form(RoastBatchDAO.RoastBatchRow row) {
        VBox form = formShell();

        Label title   = formTitle("Edit Batch #" + row.batchId);
        Label ok_msg  = statusLabel("Saved!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg = statusLabel("Fill all fields with valid values", Color.RED);

        TextField dateField      = formField("Roast Date (YYYY-MM-DD)");
        TextField kgGreenField   = formField("Kg Green");
        TextField kgRoastedField = formField("Kg Roasted");

        dateField.setText(row.roastDate != null ? row.roastDate : "");
        kgGreenField.setText(String.valueOf(row.kgGreen));
        kgRoastedField.setText(String.valueOf(row.kgRoasted));

        ComboBox<String> levelCombo = roastLevelCombo(row.roastLevel);

        Button save = submitBtn("Save Changes");
        save.setOnAction(e -> {
            ok_msg.setVisible(false); err_msg.setVisible(false);
            if (levelCombo.getValue() == null) { err_msg.setVisible(true); return; }
            boolean ok = RoastBatchDAO.updateBatch(sys.getConn(), row.batchId,
                dateField.getText(), kgGreenField.getText(), kgRoastedField.getText(), levelCombo.getValue());
            if (ok) { ok_msg.setVisible(true); refresh_roast_batches(); }
            else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, ok_msg, err_msg, dateField, kgGreenField, kgRoastedField, levelCombo, save);
        return form;
    }

    // ── ITEM TYPE / ROAST LEVEL COMBOS ───────────────────────────────────────

    private ComboBox<String> itemTypeCombo(String preselect) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(RoastBatchDAO.ITEM_TYPES);
        cb.setPromptText("Item Type");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(36);
        cb.setStyle("-fx-background-color: hsb(35, 12%, 20%); -fx-background-radius: 7;"
            + "-fx-font-size: 14px; -fx-text-fill: #d4c0a0;");
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Item Type" : s);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
            }
        });
        if (preselect != null) {
            for (String t : RoastBatchDAO.ITEM_TYPES) {
                if (t.equalsIgnoreCase(preselect)) { cb.setValue(t); break; }
            }
        }
        return cb;
    }

    private ComboBox<String> roastLevelCombo(String preselect) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(RoastBatchDAO.ROAST_LEVELS);
        cb.setPromptText("Roast Level");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(36);
        cb.setStyle("-fx-background-color: hsb(35, 12%, 20%); -fx-background-radius: 7;"
            + "-fx-font-size: 14px; -fx-text-fill: #d4c0a0;");
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Roast Level" : s);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
            }
        });
        if (preselect != null) cb.setValue(preselect);
        return cb;
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

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name  = cardTitle(row.getName());

        HBox priceRow = new HBox(12);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label price = new Label(String.format("₪ %.2f", row.getPrice()));
        price.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        price.setTextFill(Color.hsb(48, 1, 0.92, 1));

        // Stock at admin's branch
        int branchStock = adminBranchId >= 0
            ? Admin_Logic.getTotalStock(sys.getConn(), row.getItemId()) : -1;
        Label stockLbl = new Label(branchStock >= 0 ? "Stock: " + branchStock : "");
        stockLbl.setFont(Font.font("Nunito", 13));
        stockLbl.setTextFill(branchStock > 0
            ? Color.hsb(120, 0.5, 0.70, 1) : Color.hsb(0, 0.65, 0.72, 1));

        priceRow.getChildren().addAll(price, stockLbl);
        info.getChildren().addAll(name, priceRow);

        Label typeBadge = badge(row.getItemType() != null ? row.getItemType() : "—",
                                Color.hsb(120, 0.50, 0.72, 1));
        typeBadge.setTextFill(Color.BLACK);

        // Supplier sub-label
        if (row.getSupplierName() != null) {
            Label sup = cardSub("by " + row.getSupplierName());
            info.getChildren().add(sup);
        }

        card.setOnMouseClicked(e -> showFormOverlay(make_item_edit_form(row)));
        card.getChildren().addAll(accent, info, typeBadge);
        return card;
    }

    private VBox make_item_edit_form(Admin_Logic.ItemRow row) {
        VBox form = formShell();

        Label title      = formTitle("Edit Item  #" + row.getItemId());
        Label warn_empty = statusLabel("Name and price are required", Color.RED);
        Label warn_price = statusLabel("Price must be a number", Color.RED);
        Label ok_msg     = statusLabel("Saved!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg    = statusLabel("An error occurred", Color.RED);

        TextField nameField      = formField("Item Name");
        TextField priceField     = formField("Price");
        TextField wholesaleField = formField("Wholesale Price");
        ComboBox<String> typeField = itemTypeCombo(row.getItemType());

        nameField.setText(row.getName() != null ? row.getName() : "");
        priceField.setText(String.valueOf(row.getPrice()));

        // Image chooser
        String btnNormal = "-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        String btnHover  = "-fx-background-color: hsb(35, 16%, 38%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        Button chooserBtn = new Button("Change Image");
        chooserBtn.setStyle(btnNormal);
        chooserBtn.setCursor(Cursor.HAND);
        chooserBtn.setMaxWidth(Double.MAX_VALUE);
        chooserBtn.setOnMouseEntered(e -> chooserBtn.setStyle(btnHover));
        chooserBtn.setOnMouseExited(e -> chooserBtn.setStyle(btnNormal));

        Label imageLbl = new Label(row.getImagePath() != null ? new File(row.getImagePath()).getName() : "No image");
        imageLbl.setFont(Font.font("Nunito", 13));
        imageLbl.setTextFill(Color.hsb(30, 0.10, 0.55, 1));

        ImageView preview = new ImageView();
        preview.setFitWidth(72); preview.setFitHeight(72);
        preview.setPreserveRatio(true);
        if (row.getImagePath() != null) {
            try { preview.setImage(new Image(new File(row.getImagePath()).toURI().toString())); } catch (Exception ignored) {}
        }

        String[] imagePath = {row.getImagePath()};
        chooserBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Item Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
            File f = fc.showOpenDialog(chooserBtn.getScene().getWindow());
            if (f != null) {
                imagePath[0] = f.getAbsolutePath();
                imageLbl.setText(f.getName());
                preview.setImage(new Image(f.toURI().toString()));
            }
        });

        // Stock at this admin's branch
        Separator stockSep = new Separator();
        Label stockHead = new Label("Stock at your branch");
        stockHead.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        stockHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        int currentStock = adminBranchId >= 0
            ? Admin_Logic.getTotalStock(sys.getConn(), row.getItemId()) : 0;
        TextField stockField = formField("Quantity");
        stockField.setText(String.valueOf(currentStock));

        Button saveBtn = submitBtn("Save Changes");
        saveBtn.setOnAction(e -> {
            warn_empty.setVisible(false); warn_price.setVisible(false);
            ok_msg.setVisible(false); err_msg.setVisible(false);

            String res = Admin_Logic.updateItem(sys.getConn(), row.getItemId(),
                nameField.getText(), priceField.getText(), wholesaleField.getText(),
                typeField.getValue() != null ? typeField.getValue() : "", imagePath[0]);

            if (res.equals("empty"))       { warn_empty.setVisible(true); return; }
            if (res.equals("price_error")) { warn_price.setVisible(true); return; }
            if (!res.equals("ok"))         { err_msg.setVisible(true); return; }

            // update branch stock if admin has a branch
            if (adminBranchId >= 0 && !stockField.getText().isBlank()) {
                try {
                    int qty = Integer.parseInt(stockField.getText().trim());
                    Admin_Logic.setStock(sys.getConn(), adminBranchId, row.getItemId(), qty);
                } catch (NumberFormatException ignored) {}
            }

            ok_msg.setVisible(true);
            refresh_items();
        });

        form.getChildren().addAll(
            title, warn_empty, warn_price, ok_msg, err_msg,
            nameField, priceField, wholesaleField, typeField,
            chooserBtn, imageLbl, preview,
            stockSep, stockHead, stockField,
            saveBtn
        );
        return form;
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

    private HBox sectionTitleOnly(String titleText) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Labels(titleText,
            Font.font("Adwaita Mono", FontWeight.BOLD, 28),
            Color.hsb(48, 1, 0.92, 1)).getLabel();
        header.getChildren().add(lbl);
        return header;
    }

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

        TextField name       = formField("Item Name");
        TextField price      = formField("Price (e.g. 12.50)");
        ComboBox<String> item_type = itemTypeCombo(null);
        TextField stockField = formField("Quantity at your branch (optional)");

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

            int newId = Admin_Logic.addItem(
                sys.getConn(), name.getText(), price.getText(),
                item_type.getValue() != null ? item_type.getValue() : "", imagePath[0]);

            if (newId == -1)      warn_empty.setVisible(true);
            else if (newId == -2) warn_price.setVisible(true);
            else if (newId > 0) {
                String stockText = stockField.getText().trim();
                if (!stockText.isBlank() && adminBranchId > 0) {
                    try { Admin_Logic.setStock(sys.getConn(), adminBranchId, newId, Integer.parseInt(stockText)); }
                    catch (NumberFormatException ignored) {}
                }
                ok_msg.setVisible(true);
                name.clear(); price.clear(); item_type.setValue(null); stockField.clear();
                imagePath[0] = null;
                imageLbl.setText("No image selected");
                preview.setImage(null);
                preview.setVisible(false);
                preview.setManaged(false);
                refresh_items();
            } else err_msg.setVisible(true);
        });

        form.getChildren().addAll(title, warn_empty, warn_price, ok_msg, err_msg,
            name, price, item_type, stockField, chooserBtn, imageLbl, preview, submit);
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

    // ── REPORTS SECTION ──────────────────────────────────────────────────────

    private void build_reports_section() {
        reportsSection = new VBox();
        VBox.setVgrow(reportsSection, Priority.ALWAYS);
    }

    private void refresh_reports() {
        reportsSection.getChildren().setAll(ReportsUI.build(sys));
        VBox.setVgrow(reportsSection.getChildren().get(0), Priority.ALWAYS);
    }

    // ── PACKAGES SECTION ─────────────────────────────────────────────────────

    private void build_packages_section() {
        packagesSection = new VBox(12);
        packagesSection.setPadding(new Insets(14, 0, 14, 14));
        VBox.setVgrow(packagesSection, Priority.ALWAYS);

        HBox header = sectionHeader("Packages", e -> showFormOverlay(make_package_form()));

        package_cards_box = new VBox(10);
        package_cards_box.setPadding(new Insets(2, 0, 10, 0));

        packagesSection.getChildren().addAll(header, cardScroll(package_cards_box));
    }

    private void refresh_packages() {
        if (package_cards_box == null) return;
        package_cards_box.getChildren().clear();
        List<PackageDAO.PackageRow> rows = PackageDAO.getAll(sys.getConn());
        if (rows.isEmpty()) {
            package_cards_box.getChildren().add(emptyLabel("No packages yet"));
            return;
        }
        for (PackageDAO.PackageRow row : rows)
            package_cards_box.getChildren().add(makePackageCard(row));
    }

    private HBox makePackageCard(PackageDAO.PackageRow row) {
        HBox card = baseCard();
        Region accent = accentBar(Color.hsb(270, 0.55, 0.80, 1));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = cardTitle(row.name);
        String contentsStr = row.contents.isEmpty() ? "Empty package"
            : row.contents.stream().map(c -> c.itemName + " ×" + c.quantity)
                          .reduce((a, b) -> a + ", " + b).orElse("");
        Label contents = cardSub(contentsStr);
        if (row.description != null && !row.description.isBlank())
            contents = cardSub(row.description + "  |  " + contentsStr);

        info.getChildren().addAll(name, contents);

        Label priceBadge = badge(String.format("₪ %.2f", row.price), Color.hsb(270, 0.55, 0.80, 1));

        card.setOnMouseClicked(e -> showPackageDetail(row));
        card.getChildren().addAll(accent, info, priceBadge);
        return card;
    }

    private void showPackageDetail(PackageDAO.PackageRow row) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(14));

        Label title = formTitle("Package: " + row.name);
        if (row.description != null && !row.description.isBlank()) {
            Label desc = new Label(row.description);
            desc.setFont(Font.font("Nunito", 14));
            desc.setTextFill(Color.hsb(30, 0.12, 0.65, 1));
            desc.setWrapText(true);
            content.getChildren().addAll(title, desc);
        } else {
            content.getChildren().add(title);
        }

        Label priceLabel = new Label(String.format("Price:  ₪ %.2f", row.price));
        priceLabel.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        priceLabel.setTextFill(Color.hsb(48, 1, 0.92, 1));
        content.getChildren().add(priceLabel);

        content.getChildren().add(new Separator());

        Label itemsHead = new Label("Included Items");
        itemsHead.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 14));
        itemsHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        content.getChildren().add(itemsHead);

        if (row.contents.isEmpty()) {
            content.getChildren().add(emptyLabel("No items in this package yet"));
        } else {
            for (PackageDAO.PackageItemRow item : row.contents) {
                HBox row2 = new HBox(10);
                row2.setAlignment(Pos.CENTER_LEFT);
                row2.setPadding(new Insets(8, 12, 8, 12));
                row2.setBackground(new Background(new BackgroundFill(
                    Color.hsb(35, 0.10, 0.28, 1), new CornerRadii(8), null)));
                Label n = new Label(item.itemName);
                n.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
                n.setTextFill(Color.WHITE);
                HBox.setHgrow(n, Priority.ALWAYS);
                Label q = new Label("×" + item.quantity);
                q.setTextFill(Color.hsb(48, 1, 0.92, 1));
                q.setFont(Font.font("Nunito", 14));
                Label p = new Label(String.format("₪ %.2f ea", item.unitPrice));
                p.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
                p.setFont(Font.font("Nunito", 14));
                row2.getChildren().addAll(n, q, p);
                content.getChildren().add(row2);
            }
        }

        // Add item to package sub-form
        content.getChildren().add(new Separator());
        Label addHead = new Label("Add Item to Package");
        addHead.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        addHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        ComboBox<Admin_Logic.ItemRow> itemCombo = new ComboBox<>();
        itemCombo.setMaxWidth(Double.MAX_VALUE);
        itemCombo.setPromptText("Select Item");
        itemCombo.getItems().addAll(Admin_Logic.getItems(sys.getConn()));

        // Show "#ID – Name" in both the button cell and the drop-down cells
        Callback<ListView<Admin_Logic.ItemRow>, ListCell<Admin_Logic.ItemRow>> cellFactory = lv -> new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                if (empty || it == null) { setText(null); }
                else {
                    setText("#" + it.getItemId() + "  –  " + it.getName());
                    setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                    setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
                }
            }
        };
        itemCombo.setCellFactory(cellFactory);
        itemCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it == null ? "Select Item" : "#" + it.getItemId() + "  –  " + it.getName());
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
            }
        });

        TextField qtyField = formField("Quantity");
        Label addFeedback = statusLabel("Item added!", Color.hsb(120, 0.5, 0.85, 1));
        Label addErr = statusLabel("Select an item and enter a valid quantity", Color.RED);

        Button addBtn = submitBtn("Add Item");
        addBtn.setOnAction(e -> {
            addFeedback.setVisible(false); addErr.setVisible(false);
            Admin_Logic.ItemRow sel = itemCombo.getValue();
            if (sel == null || qtyField.getText().isBlank()) { addErr.setVisible(true); return; }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                boolean ok = PackageDAO.addItemToPackage(sys.getConn(), row.packageId, sel.getItemId(), qty);
                if (ok) { addFeedback.setVisible(true); qtyField.clear(); refresh_packages(); }
                else addErr.setVisible(true);
            } catch (NumberFormatException ex) { addErr.setVisible(true); }
        });

        content.getChildren().addAll(addHead, itemCombo, qtyField, addErr, addFeedback, addBtn);
        showFormOverlay(content);
    }

    private VBox make_package_form() {
        VBox form = formShell();

        Label title      = formTitle("Create Package");
        Label warn_empty = statusLabel("Name and price are required", Color.RED);
        Label warn_price = statusLabel("Price must be a number", Color.RED);
        Label ok_msg     = statusLabel("Package created!", Color.hsb(120, 0.5, 0.85, 1));
        Label err_msg    = statusLabel("An error occurred", Color.RED);

        TextField nameField  = formField("Package Name");
        TextField priceField = formField("Price (e.g. 45.00)");
        TextField descField  = formField("Description (optional)");

        Separator sep = new Separator();

        Label itemsHead = new Label("Items to Include");
        itemsHead.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        itemsHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        ComboBox<Admin_Logic.ItemRow> itemCombo = new ComboBox<>();
        itemCombo.setMaxWidth(Double.MAX_VALUE);
        itemCombo.setPromptText("Select Item");
        itemCombo.getItems().addAll(Admin_Logic.getItems(sys.getConn()));

        Callback<ListView<Admin_Logic.ItemRow>, ListCell<Admin_Logic.ItemRow>> cellFactory = lv -> new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                if (empty || it == null) { setText(null); }
                else {
                    setText("#" + it.getItemId() + "  –  " + it.getName());
                    setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                    setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
                }
            }
        };
        itemCombo.setCellFactory(cellFactory);
        itemCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Admin_Logic.ItemRow it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it == null ? "Select Item" : "#" + it.getItemId() + "  –  " + it.getName());
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 14px; -fx-background-color: transparent;");
            }
        });

        TextField qtyField = formField("Quantity");
        Label addErr = statusLabel("Select an item and enter a valid quantity", Color.RED);

        List<int[]> pendingItems = new ArrayList<>();
        VBox pendingBox = new VBox(4);

        String secBtnNormal = "-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        String secBtnHover  = "-fx-background-color: hsb(35, 16%, 38%); -fx-background-radius: 7; -fx-font-size: 13px; -fx-text-fill: #d4c0a0;";
        Button addItemBtn = new Button("+ Add Item to List");
        addItemBtn.setMaxWidth(Double.MAX_VALUE);
        addItemBtn.setStyle(secBtnNormal);
        addItemBtn.setCursor(Cursor.HAND);
        addItemBtn.setOnMouseEntered(ev -> addItemBtn.setStyle(secBtnHover));
        addItemBtn.setOnMouseExited(ev -> addItemBtn.setStyle(secBtnNormal));
        addItemBtn.setOnAction(e -> {
            addErr.setVisible(false);
            Admin_Logic.ItemRow sel = itemCombo.getValue();
            if (sel == null || qtyField.getText().isBlank()) { addErr.setVisible(true); return; }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) { addErr.setVisible(true); return; }
                pendingItems.add(new int[]{sel.getItemId(), qty});
                HBox row2 = new HBox(10);
                row2.setAlignment(Pos.CENTER_LEFT);
                row2.setPadding(new Insets(6, 10, 6, 10));
                row2.setBackground(new Background(new BackgroundFill(
                    Color.hsb(35, 0.10, 0.28, 1), new CornerRadii(8), null)));
                Label n = new Label("#" + sel.getItemId() + "  –  " + sel.getName());
                n.setFont(Font.font("Nunito", 13));
                n.setTextFill(Color.WHITE);
                HBox.setHgrow(n, Priority.ALWAYS);
                Label q = new Label("×" + qty);
                q.setTextFill(Color.hsb(48, 1, 0.92, 1));
                q.setFont(Font.font("Nunito", 13));
                row2.getChildren().addAll(n, q);
                pendingBox.getChildren().add(row2);
                itemCombo.setValue(null);
                qtyField.clear();
            } catch (NumberFormatException ex) { addErr.setVisible(true); }
        });

        Button submit = submitBtn("Create Package");
        submit.setOnAction(e -> {
            warn_empty.setVisible(false); warn_price.setVisible(false);
            ok_msg.setVisible(false); err_msg.setVisible(false);

            int pkgId = PackageDAO.createPackage(
                sys.getConn(), nameField.getText(), descField.getText(), priceField.getText());

            if (pkgId == -2) { warn_price.setVisible(true); return; }
            if (pkgId <= 0) {
                if (nameField.getText().isBlank() || priceField.getText().isBlank())
                    warn_empty.setVisible(true);
                else
                    err_msg.setVisible(true);
                return;
            }

            for (int[] pair : pendingItems)
                PackageDAO.addItemToPackage(sys.getConn(), pkgId, pair[0], pair[1]);

            ok_msg.setVisible(true);
            nameField.clear(); priceField.clear(); descField.clear();
            pendingItems.clear();
            pendingBox.getChildren().clear();
            itemCombo.setValue(null);
            refresh_packages();
        });

        form.getChildren().addAll(title, warn_empty, warn_price, ok_msg, err_msg,
            nameField, priceField, descField,
            sep, itemsHead, itemCombo, qtyField, addErr, addItemBtn, pendingBox,
            submit);
        return form;
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

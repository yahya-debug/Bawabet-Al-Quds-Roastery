package app.db_proj.UI;

import app.db_proj.Admin_Logic;
import app.db_proj.ItemDAO;
import app.db_proj.PackageDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Item;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Home_UI {
    private ScrollPane SP;
    private FlowPane grid;
    private HBox branchNav;
    private HBox searchBanner;
    private Label searchQueryLbl;
    private Label searchCountLbl;
    private SystemHandling sys;
    private Image placeholder;
    private int selectedBranchId = -1;
    private String currentQuery = "";
    private List<Button> navBtns = new ArrayList<>();

    public Home_UI(SystemHandling sys) {
        this.sys = sys;
        placeholder = new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));

        grid = new FlowPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setPadding(new Insets(20));

        branchNav = buildBranchNav();
        searchBanner = buildSearchBanner();

        VBox home = new VBox(0);
        home.getChildren().addAll(branchNav, searchBanner, grid);
        home.setPadding(new Insets(10));

        SP = new ScrollPane(home);
        SP.setFitToWidth(true);
        SP.setFitToHeight(false);
        SP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        SP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadItems();
    }

    public void refresh() {
        loadItems();
    }

    public void search(String query) {
        currentQuery = (query == null ? "" : query.trim());
        loadItems();
    }

    // ── Branch nav ────────────────────────────────────────────────────────────

    private HBox buildBranchNav() {
        HBox nav = new HBox(8);
        nav.setPadding(new Insets(12, 20, 12, 20));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.15, 0.25, 1), null, null)));

        List<Admin_Logic.BranchRow> branches = Admin_Logic.getBranches(sys.getConn());

        Button allBtn = navButton("All Branches", true);
        allBtn.setOnAction(e -> selectBranch(-1, allBtn));
        navBtns.add(allBtn);
        nav.getChildren().add(allBtn);

        for (Admin_Logic.BranchRow b : branches) {
            Button btn = navButton(b.name, false);
            int id = b.branchId;
            btn.setOnAction(e -> selectBranch(id, btn));
            navBtns.add(btn);
            nav.getChildren().add(btn);
        }

        return nav;
    }

    private Button navButton(String text, boolean selected) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        btn.setPadding(new Insets(7, 16, 7, 16));
        btn.setCursor(Cursor.HAND);
        setNavBtnStyle(btn, selected);
        return btn;
    }

    private void setNavBtnStyle(Button btn, boolean selected) {
        if (selected) {
            btn.setStyle(
                "-fx-background-color: hsb(48,100%,92%);" +
                "-fx-text-fill: hsb(35,80%,20%);" +
                "-fx-background-radius: 8;" +
                "-fx-border-width: 0;");
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: hsb(30,12%,72%);" +
                "-fx-border-width: 0;");
        }
    }

    private void selectBranch(int branchId, Button clicked) {
        selectedBranchId = branchId;
        sys.setSelectedBranchId(branchId);
        for (Button b : navBtns) setNavBtnStyle(b, b == clicked);
        loadItems();
    }

    // ── Search banner (visible only while a query is active) ──────────────────

    private HBox buildSearchBanner() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.18, 1), null, null)));
        bar.setVisible(false);
        bar.setManaged(false);

        Label icon = new Label("Search results for");
        icon.setFont(Font.font("Nunito", 14));
        icon.setTextFill(Color.hsb(30, 0.12, 0.65, 1));

        searchQueryLbl = new Label();
        searchQueryLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        searchQueryLbl.setTextFill(Color.WHITE);

        searchCountLbl = new Label();
        searchCountLbl.setFont(Font.font("Nunito", 13));
        searchCountLbl.setTextFill(Color.hsb(30, 0.12, 0.55, 1));
        searchCountLbl.setPadding(new Insets(0, 0, 0, 8));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearBtn = new Button("✕  Clear");
        clearBtn.setCursor(Cursor.HAND);
        clearBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: hsb(48,100%,88%);" +
            "-fx-border-color: hsb(48,80%,60%);" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-font-size: 13px; -fx-font-family: Nunito;");
        clearBtn.setPadding(new Insets(4, 12, 4, 12));
        clearBtn.setOnAction(e -> {
            currentQuery = "";
            setBannerVisible(false);
            loadItems();
        });

        bar.getChildren().addAll(icon, searchQueryLbl, searchCountLbl, spacer, clearBtn);
        return bar;
    }

    private void setBannerVisible(boolean visible) {
        searchBanner.setVisible(visible);
        searchBanner.setManaged(visible);
    }

    // ── Item loading ──────────────────────────────────────────────────────────

    private void loadItems() {
        grid.getChildren().clear();

        List<Item> items;
        if (selectedBranchId >= 0) {
            items = ItemDAO.getByBranch(sys.getConn(), selectedBranchId, currentQuery);
        } else {
            items = currentQuery.isBlank()
                ? ItemDAO.getAll(sys.getConn())
                : ItemDAO.search(sys.getConn(), currentQuery);
        }

        // update search banner
        if (!currentQuery.isBlank()) {
            searchQueryLbl.setText("\"" + currentQuery + "\"");
            searchCountLbl.setText("— " + items.size() + " result" + (items.size() == 1 ? "" : "s"));
            setBannerVisible(true);
        } else {
            setBannerVisible(false);
        }

        if (items.isEmpty()) {
            Label empty = new Label(currentQuery.isBlank() ? "No items found" : "No results for \"" + currentQuery + "\"");
            empty.setFont(Font.font("Nunito", 18));
            empty.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            empty.setPadding(new Insets(40, 20, 20, 20));
            grid.getChildren().add(empty);
            return;
        }

        for (Item item : items) {
            grid.getChildren().add(makeCard(item));
        }
    }

    private Node makeCard(Item item) {
        Image img = loadImage(item.imagePath);
        String typeLabel = PackageDAO.isPackage(sys.getConn(), item.itemId)
            ? "BUNDLE" : item.itemType;
        VBox card = Item_UI.makeProductCard(item.name, item.price, typeLabel, img);

        if (selectedBranchId >= 0 && (sys.isUserAdmin() || sys.isUserEmployee())) {
            Label qtyLbl = new Label(String.format("%.3f kg", item.branchQuantity));
            qtyLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 11));
            qtyLbl.setTextFill(Color.hsb(120, 0.7, 0.15, 1));
            qtyLbl.setPadding(new Insets(3, 8, 3, 8));
            qtyLbl.setBackground(new Background(new BackgroundFill(
                Color.hsb(120, 0.55, 0.78, 1), new CornerRadii(6), null)));
            StackPane.setAlignment(qtyLbl, Pos.TOP_RIGHT);
            StackPane.setMargin(qtyLbl, new Insets(8));
            StackPane wrapper = new StackPane(card, qtyLbl);
            wrapper.setCursor(Cursor.HAND);
            wrapper.setOnMouseClicked(e -> sys.openItemDetail(item));
            return wrapper;
        }
        card.setOnMouseClicked(e -> sys.openItemDetail(item));
        return card;
    }

    private Image loadImage(String path) {
        if (path != null && !path.isBlank()) {
            try { return new Image(new File(path).toURI().toString()); }
            catch (Exception ignored) {}
        }
        return placeholder;
    }

    public ScrollPane getSP() {
        return SP;
    }
}

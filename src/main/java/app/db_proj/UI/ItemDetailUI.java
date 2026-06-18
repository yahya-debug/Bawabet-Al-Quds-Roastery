package app.db_proj.UI;

import app.db_proj.Cart_Logic;
import app.db_proj.ItemDAO;
import app.db_proj.ItemDAO.ItemStats;
import app.db_proj.SystemHandling;
import app.db_proj.model.Item;
import app.db_proj.model.Review;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.List;

public class ItemDetailUI {

    private final StackPane overlay;

    public ItemDetailUI(SystemHandling sys, Item item) {
        overlay = new StackPane();
        overlay.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0, 0.70), null, null)));
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) sys.hideItemDetail();
        });

        VBox panel = buildPanel(sys, item);
        panel.setMaxWidth(860);
        panel.setMaxHeight(Double.MAX_VALUE);
        StackPane.setMargin(panel, new Insets(30));

        overlay.getChildren().add(panel);
    }

    private VBox buildPanel(SystemHandling sys, Item item) {
        VBox panel = new VBox(0);
        panel.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(18), null)));
        panel.setOnMouseClicked(javafx.event.Event::consume);

        panel.getChildren().addAll(
            buildHeader(sys, item.name),
            buildBody(sys, item)
        );
        return panel;
    }

    // ── Header bar ─────────────────────────────────────────────────────────────

    private HBox buildHeader(SystemHandling sys, String itemName) {
        HBox header = new HBox(12);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.22, 0.17, 1), new CornerRadii(18, 18, 0, 0, false), null)));

        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4c0a0; -fx-font-size: 14px;");
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4c0a0; -fx-font-size: 14px;"));
        backBtn.setOnAction(e -> sys.hideItemDetail());

        Label title = new Label(itemName);
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        title.setTextFill(Color.hsb(48, 1, 0.92, 1));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7a6a58; -fx-font-size: 16px;");
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7a6a58; -fx-font-size: 16px;"));
        closeBtn.setOnAction(e -> sys.hideItemDetail());

        header.getChildren().addAll(backBtn, title, spacer, closeBtn);
        return header;
    }

    // ── Body (image + info + reviews) ─────────────────────────────────────────

    private ScrollPane buildBody(SystemHandling sys, Item item) {
        VBox body = new VBox(0);

        // Top section: image (left) + info (right)
        HBox top = new HBox(0);
        top.getChildren().addAll(buildImagePane(item), buildInfoPane(sys, item));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: hsb(35, 0.18, 0.32);");
        VBox.setMargin(sep, new Insets(0, 20, 0, 20));

        body.getChildren().addAll(top, sep, buildReviewsPane(sys.getConn(), item.itemId));

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private VBox buildImagePane(Item item) {
        VBox pane = new VBox();
        pane.setPadding(new Insets(28));
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(280);
        pane.setMinWidth(280);
        pane.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.18, 0.18, 1), new CornerRadii(0, 0, 0, 18, false), null)));

        Image img = loadImage(item.imagePath);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(200);
        iv.setFitHeight(200);
        iv.setPreserveRatio(true);

        pane.getChildren().add(iv);
        return pane;
    }

    private VBox buildInfoPane(SystemHandling sys, Item item) {
        VBox pane = new VBox(14);
        pane.setPadding(new Insets(28, 28, 28, 24));
        HBox.setHgrow(pane, Priority.ALWAYS);

        // Type badge
        Label typeBadge = new Label(item.itemType != null ? item.itemType.toUpperCase() : "");
        typeBadge.setFont(Font.font("Nunito", FontWeight.BOLD, 12));
        typeBadge.setTextFill(Color.hsb(48, 0.9, 0.3, 1));
        typeBadge.setPadding(new Insets(3, 10, 3, 10));
        typeBadge.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(6), null)));

        // Name
        Label name = new Label(item.name);
        name.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 26));
        name.setTextFill(Color.WHITE);
        name.setWrapText(true);

        // Price
        Label price = new Label(String.format("₪ %.2f", item.price));
        price.setFont(Font.font("Nunito", FontWeight.BOLD, 28));
        price.setTextFill(Color.hsb(48, 1, 0.92, 1));

        // Supplier (if any)
        VBox meta = new VBox(4);
        if (item.supplierName != null) {
            Label sup = new Label("Supplier: " + item.supplierName);
            sup.setFont(Font.font("Nunito", 14));
            sup.setTextFill(Color.hsb(200, 0.5, 0.75, 1));
            meta.getChildren().add(sup);
        }

        // Stats row
        ItemStats stats = ItemDAO.getStats(sys.getConn(), item.itemId);
        HBox statsRow = buildStatsRow(stats);

        // Add to cart button (only for authenticated non-admin customers)
        pane.getChildren().addAll(typeBadge, name, price, meta, statsRow);
        if (sys.isAuthenticated() && !sys.isUserAdmin()) {
            pane.getChildren().add(buildAddToCartBtn(sys, item));
        }

        return pane;
    }

    private HBox buildStatsRow(ItemStats stats) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 6, 0));

        row.getChildren().addAll(
            statChip("⭐ " + (stats.reviewCount > 0
                ? String.format("%.1f", stats.avgRating) : "—"), Color.hsb(48, 0.9, 0.80, 1)),
            statChip(stats.reviewCount + " reviews", Color.hsb(30, 0.12, 0.65, 1)),
            statChip(stats.totalSold + " sold", Color.hsb(120, 0.4, 0.68, 1))
        );
        return row;
    }

    private Label statChip(String text, Color fg) {
        Label l = new Label(text);
        l.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        l.setTextFill(fg);
        l.setPadding(new Insets(4, 12, 4, 12));
        l.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(8), null)));
        return l;
    }

    private Button buildAddToCartBtn(SystemHandling sys, Item item) {
        String normal = "-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";
        String hover  = "-fx-background-color: hsb(48, 100%, 75%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";
        String added  = "-fx-background-color: hsb(120, 60%, 72%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";

        Button btn = new Button("+ Add to Cart");
        btn.setStyle(normal);
        btn.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);
        VBox.setMargin(btn, new Insets(8, 0, 0, 0));

        btn.setOnMouseEntered(e -> { if (!"Added!".equals(btn.getText())) btn.setStyle(hover); });
        btn.setOnMouseExited(e -> { if (!"Added!".equals(btn.getText())) btn.setStyle(normal); });
        btn.setOnAction(e -> {
            Cart_Logic.addToCart(sys.getConn(), sys.getCurrentUserId(), item.itemId);
            btn.setText("Added!");
            btn.setStyle(added);
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);
        });
        return btn;
    }

    // ── Reviews ────────────────────────────────────────────────────────────────

    private VBox buildReviewsPane(java.sql.Connection conn, int itemId) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20, 28, 28, 28));

        Label heading = new Label("Reviews");
        heading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        heading.setTextFill(Color.hsb(48, 1, 0.92, 1));
        pane.getChildren().add(heading);

        List<Review> reviews = ItemDAO.getReviews(conn, itemId);
        if (reviews.isEmpty()) {
            Label none = new Label("No reviews yet");
            none.setFont(Font.font("Nunito", 15));
            none.setTextFill(Color.hsb(30, 0.10, 0.55, 1));
            pane.getChildren().add(none);
        } else {
            for (Review r : reviews)
                pane.getChildren().add(makeReviewCard(r));
        }
        return pane;
    }

    private VBox makeReviewCard(Review r) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(10), null)));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label stars = new Label(starsFor(r.rating));
        stars.setFont(Font.font("Nunito", 15));

        Label reviewer = new Label(r.personName);
        reviewer.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        reviewer.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(r.reviewDate.toLocalDate().toString());
        date.setFont(Font.font("Nunito", 12));
        date.setTextFill(Color.hsb(30, 0.08, 0.55, 1));

        top.getChildren().addAll(stars, reviewer, spacer, date);

        if (r.comment != null && !r.comment.isBlank()) {
            Label comment = new Label(r.comment);
            comment.setFont(Font.font("Nunito", 14));
            comment.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
            comment.setWrapText(true);
            card.getChildren().addAll(top, comment);
        } else {
            card.getChildren().add(top);
        }
        return card;
    }

    private String starsFor(int rating) {
        return "★".repeat(Math.max(0, Math.min(rating, 5))) +
               "☆".repeat(Math.max(0, 5 - rating));
    }

    private Image loadImage(String path) {
        if (path != null && !path.isBlank()) {
            try { return new Image(new File(path).toURI().toString()); }
            catch (Exception ignored) {}
        }
        return new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));
    }

    public StackPane getOverlay() {
        return overlay;
    }
}

package app.db_proj.UI;

import app.db_proj.Admin_Logic;
import app.db_proj.Cart_Logic;
import app.db_proj.ItemDAO;
import app.db_proj.ItemDAO.ItemStats;
import app.db_proj.OrderDAO;
import app.db_proj.PackageDAO;
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
import javafx.scene.control.TextArea;
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

        boolean isPkg = PackageDAO.isPackage(sys.getConn(), item.itemId);
        javafx.scene.Node bottomSection = isPkg
            ? buildPackageContentsPane(sys.getConn(), item.itemId)
            : buildReviewsPane(sys, item.itemId);

        body.getChildren().addAll(top, sep, buildStockPane(sys.getConn(), item.itemId), bottomSection);

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

        // Add to cart controls (only for authenticated non-admin customers)
        pane.getChildren().addAll(typeBadge, name, price, meta, statsRow);
        if (sys.isAuthenticated() && !sys.isUserAdmin()) {
            pane.getChildren().add(buildCartControls(sys, item));
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

    private VBox buildCartControls(SystemHandling sys, Item item) {
        VBox box = new VBox(8);
        VBox.setMargin(box, new Insets(8, 0, 0, 0));

        int currentQty = Cart_Logic.getCartQuantity(sys.getConn(), sys.getCurrentUserId(), item.itemId);
        int[] qty = { currentQty > 0 ? currentQty : 1 };

        String stepStyle = "-fx-background-color: hsb(35, 14%, 30%); -fx-background-radius: 7;"
                         + "-fx-font-size: 18px; -fx-text-fill: #d4c0a0;"
                         + "-fx-min-width: 36; -fx-min-height: 36;";
        String stepHover = "-fx-background-color: hsb(35, 16%, 40%); -fx-background-radius: 7;"
                         + "-fx-font-size: 18px; -fx-text-fill: #d4c0a0;"
                         + "-fx-min-width: 36; -fx-min-height: 36;";

        Button minusBtn = new Button("−");
        minusBtn.setStyle(stepStyle); minusBtn.setCursor(Cursor.HAND);
        minusBtn.setOnMouseEntered(e -> minusBtn.setStyle(stepHover));
        minusBtn.setOnMouseExited(e -> minusBtn.setStyle(stepStyle));

        Label qtyLbl = new Label(String.valueOf(qty[0]));
        qtyLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        qtyLbl.setTextFill(Color.WHITE);
        qtyLbl.setMinWidth(40);
        qtyLbl.setAlignment(Pos.CENTER);

        Button plusBtn = new Button("+");
        plusBtn.setStyle(stepStyle); plusBtn.setCursor(Cursor.HAND);
        plusBtn.setOnMouseEntered(e -> plusBtn.setStyle(stepHover));
        plusBtn.setOnMouseExited(e -> plusBtn.setStyle(stepStyle));

        HBox stepper = new HBox(8, minusBtn, qtyLbl, plusBtn);
        stepper.setAlignment(Pos.CENTER_LEFT);

        minusBtn.setOnAction(e -> {
            if (qty[0] > 1) { qty[0]--; qtyLbl.setText(String.valueOf(qty[0])); }
        });
        plusBtn.setOnAction(e -> {
            qty[0]++; qtyLbl.setText(String.valueOf(qty[0]));
        });

        boolean inCart = currentQty > 0;
        String addStyle    = "-fx-background-color: hsb(48, 100%, 92%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";
        String addHover    = "-fx-background-color: hsb(48, 100%, 75%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";
        String updateStyle = "-fx-background-color: hsb(200, 60%, 72%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";
        String updateHover = "-fx-background-color: hsb(200, 60%, 55%); -fx-background-radius: 12; -fx-font-size: 16px; -fx-text-fill: black;";

        String[] btnNormal = { inCart ? updateStyle : addStyle };
        String[] btnHover  = { inCart ? updateHover : addHover };

        if (inCart) {
            Label note = new Label("Currently in cart: " + currentQty);
            note.setFont(Font.font("Nunito", 13));
            note.setTextFill(Color.hsb(120, 0.4, 0.68, 1));
            box.getChildren().add(note);
        }

        Button actionBtn = new Button(inCart
            ? "Update Cart  (" + currentQty + " in cart)"
            : "+ Add to Cart");
        actionBtn.setStyle(btnNormal[0]);
        actionBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setCursor(Cursor.HAND);
        actionBtn.setOnMouseEntered(e -> actionBtn.setStyle(btnHover[0]));
        actionBtn.setOnMouseExited(e -> actionBtn.setStyle(btnNormal[0]));
        actionBtn.setOnAction(e -> {
            Cart_Logic.setCartQuantity(sys.getConn(), sys.getCurrentUserId(), item.itemId, qty[0], sys.getSelectedBranchId());
            actionBtn.setText("✓  In Cart: " + qty[0]);
            actionBtn.setStyle(updateStyle);
            btnNormal[0] = updateStyle;
            btnHover[0]  = updateHover;
        });

        box.getChildren().addAll(stepper, actionBtn);
        return box;
    }

    // ── Reviews ────────────────────────────────────────────────────────────────

    private VBox buildReviewsPane(SystemHandling sys, int itemId) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20, 28, 28, 28));

        Label heading = new Label("Reviews");
        heading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        heading.setTextFill(Color.hsb(48, 1, 0.92, 1));
        pane.getChildren().add(heading);

        List<Review> reviews = ItemDAO.getReviews(sys.getConn(), itemId);
        if (reviews.isEmpty()) {
            Label none = new Label("No reviews yet");
            none.setFont(Font.font("Nunito", 15));
            none.setTextFill(Color.hsb(30, 0.10, 0.55, 1));
            pane.getChildren().add(none);
        } else {
            for (Review r : reviews)
                pane.getChildren().add(makeReviewCard(r));
        }

        // show review form if logged-in customer has purchased this item but not reviewed it yet
        Integer personId = sys.getCurrentUserId();
        if (personId != null && sys.isAuthenticated() && !sys.isUserAdmin()
                && OrderDAO.hasPurchased(sys.getConn(), personId, itemId)
                && !OrderDAO.hasReviewed(sys.getConn(), personId, itemId)) {
            pane.getChildren().add(buildReviewForm(sys.getConn(), personId, itemId, pane));
        }

        return pane;
    }

    private VBox buildReviewForm(java.sql.Connection conn, int personId, int itemId, VBox pane) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(14, 0, 0, 0));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: hsb(35, 0.18, 0.32);");

        Label heading = new Label("Write a Review");
        heading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 16));
        heading.setTextFill(Color.hsb(48, 1, 0.92, 1));

        // star rating
        int[] rating = {0};
        Label[] starLbls = new Label[5];
        HBox stars = new HBox(6);
        stars.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            final int star = i + 1;
            Label sl = new Label("☆");
            sl.setFont(Font.font("Nunito", 30));
            sl.setTextFill(Color.hsb(30, 0.10, 0.45, 1));
            sl.setCursor(Cursor.HAND);
            sl.setOnMouseClicked(e -> {
                rating[0] = star;
                for (int j = 0; j < 5; j++) {
                    starLbls[j].setText(j < star ? "★" : "☆");
                    starLbls[j].setTextFill(j < star
                        ? Color.hsb(48, 1, 0.92, 1)
                        : Color.hsb(30, 0.10, 0.45, 1));
                }
            });
            starLbls[i] = sl;
            stars.getChildren().add(sl);
        }

        TextArea commentTA = new TextArea();
        commentTA.setPromptText("Share your thoughts (optional)");
        commentTA.setFont(Font.font("Nunito", 14));
        commentTA.setPrefRowCount(3);
        commentTA.setWrapText(true);
        commentTA.setStyle(
            "-fx-control-inner-background: hsb(35, 14%, 25%);" +
            "-fx-text-fill: #d4c0a0;" +
            "-fx-prompt-text-fill: #5a4a38;" +
            "-fx-background-radius: 8;");

        Label statusLbl = new Label();
        statusLbl.setFont(Font.font("Nunito", 13));

        Button submitBtn = new Button("Submit Review");
        submitBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        submitBtn.setCursor(Cursor.HAND);
        submitBtn.setPadding(new Insets(7, 20, 7, 20));
        submitBtn.setStyle(
            "-fx-background-color: hsb(48,100%,92%);" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 8;");
        submitBtn.setOnAction(e -> {
            if (rating[0] == 0) {
                statusLbl.setTextFill(Color.hsb(0, 0.7, 0.75, 1));
                statusLbl.setText("Please select a star rating.");
                return;
            }
            boolean ok = OrderDAO.addReview(conn, personId, itemId, rating[0],
                                            commentTA.getText().trim());
            if (ok) {
                form.setVisible(false);
                form.setManaged(false);
                // add the new review card to the pane
                Review fresh = new Review(0, personId, "You", itemId, "",
                    rating[0], commentTA.getText().trim(),
                    java.time.LocalDateTime.now());
                pane.getChildren().add(pane.getChildren().size() - 1, makeReviewCard(fresh));
            } else {
                statusLbl.setTextFill(Color.hsb(0, 0.7, 0.75, 1));
                statusLbl.setText("Could not submit — you may have already reviewed this item.");
            }
        });

        form.getChildren().addAll(sep, heading, stars, commentTA,
            new HBox(12) {{ setAlignment(Pos.CENTER_LEFT); getChildren().addAll(submitBtn, statusLbl); }});
        return form;
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

    // ── Branch stock levels ────────────────────────────────────────────────────

    private HBox buildStockPane(java.sql.Connection conn, int itemId) {
        HBox pane = new HBox(10);
        pane.setPadding(new Insets(0, 28, 14, 28));
        pane.setAlignment(Pos.CENTER_LEFT);

        int total = Admin_Logic.getTotalStock(conn, itemId);
        Label stockLabel = new Label("Stock:  " + total + " units across all branches");
        stockLabel.setFont(Font.font("Nunito", 14));
        stockLabel.setTextFill(total > 0
            ? Color.hsb(120, 0.5, 0.72, 1)
            : Color.hsb(0, 0.7, 0.75, 1));
        pane.getChildren().add(stockLabel);
        return pane;
    }

    // ── Package contents (instead of reviews for packages) ─────────────────────

    private VBox buildPackageContentsPane(java.sql.Connection conn, int packageId) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20, 28, 28, 28));

        Label heading = new Label("Package Contents");
        heading.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        heading.setTextFill(Color.hsb(48, 1, 0.92, 1));
        pane.getChildren().add(heading);

        List<PackageDAO.PackageItemRow> contents = PackageDAO.getContents(conn, packageId);
        if (contents.isEmpty()) {
            Label none = new Label("No items in this package yet");
            none.setFont(Font.font("Nunito", 15));
            none.setTextFill(Color.hsb(30, 0.10, 0.55, 1));
            pane.getChildren().add(none);
        } else {
            for (PackageDAO.PackageItemRow c : contents) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 14, 10, 14));
                row.setBackground(new Background(new BackgroundFill(
                    Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(10), null)));

                Label n = new Label(c.itemName);
                n.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
                n.setTextFill(Color.WHITE);
                HBox.setHgrow(n, Priority.ALWAYS);

                Label qty = new Label("×" + c.quantity);
                qty.setFont(Font.font("Nunito", 14));
                qty.setTextFill(Color.hsb(48, 1, 0.92, 1));

                Label price = new Label(String.format("₪ %.2f ea", c.unitPrice));
                price.setFont(Font.font("Nunito", 14));
                price.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

                row.getChildren().addAll(n, qty, price);
                pane.getChildren().add(row);
            }
        }
        return pane;
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

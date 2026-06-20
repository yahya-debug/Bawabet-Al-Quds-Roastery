package app.db_proj.UI;

import app.db_proj.Cart_Logic;
import app.db_proj.Labels;
import app.db_proj.OrderDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Item;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class CartUI {
    private HBox screen;
    private VBox cart_side, cart_scroll, payment_side;
    private VBox orders_vb;
    private BorderPane payment_box, orders_box;
    private ScrollPane SP;
    private SystemHandling sys;
    private Region spacer;
    private Label total_price_lbl;
    private TextField search_tf;
    private ComboBox<String> sortBox;
    private ComboBox<String> filterBox;
    private String currentSort   = "Default";
    private String currentFilter = "All";

    public CartUI(SystemHandling sys) {
        this.sys = sys;
        screen = new HBox(10);
        screen.setPrefWidth(Double.MAX_VALUE);

        cart_scroll = new VBox(10);

        SP = new ScrollPane(cart_scroll);

        make_cart_side();
        make_payment_side();

        screen.getChildren().addAll(cart_side, payment_side);
    }

    public void refresh() {
        currentSort   = "Default";
        currentFilter = "All";
        if (sortBox   != null) sortBox.setValue("Default");
        if (filterBox != null) filterBox.setValue("All");
        loadCartItems("");
        refreshOrders();
        if (search_tf != null) search_tf.clear();
    }

    public void refreshOrders() {
        if (orders_vb == null) return;
        orders_vb.getChildren().setAll(CartOrdersUI.build(sys).getChildren());
    }

    public void loadCartItems(String search) {
        cart_scroll.getChildren().clear();

        Integer uid = sys.getCurrentUserId();
        if (uid == null) {
            cart_scroll.getChildren().add(new Labels("Please log in to view your cart",
                Font.font("Nunito", 17), Color.hsb(30, 0.12, 0.78, 1)).getLabel());
            updateTotal(0.0);
            return;
        }

        List<Cart_Logic.CartItemRow> items =
            Cart_Logic.getCartItems(sys.getConn(), uid, search, currentSort, currentFilter);

        if (items.isEmpty()) {
            boolean filtered = !search.isBlank() || !"All".equals(currentFilter);
            String emptyMsg  = filtered ? "No items match your search/filter" : "Your cart is empty";
            cart_scroll.getChildren().add(new Labels(emptyMsg,
                Font.font("Nunito", 17), Color.hsb(30, 0.12, 0.78, 1)).getLabel());
        } else {
            for (Cart_Logic.CartItemRow item : items)
                cart_scroll.getChildren().add(makeDbItemCard(item));
        }

        updateTotal(Cart_Logic.getCartTotal(sys.getConn(), uid));
    }

    private HBox makeDbItemCard(Cart_Logic.CartItemRow row) {
        HBox card = new HBox(0);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(14), null)));
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.16, 0.38, 1), new CornerRadii(14), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(14), null))));

        // image panel
        Image img = loadItemImage(row.getImagePath());
        ImageView iv = new ImageView(img);
        iv.setFitWidth(90);
        iv.setFitHeight(90);
        iv.setPreserveRatio(true);
        VBox imgBox = new VBox(iv);
        imgBox.setMinWidth(110);
        imgBox.setPrefWidth(110);
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPadding(new Insets(10));
        imgBox.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.18, 0.20, 1), new CornerRadii(14, 0, 0, 14, false), null)));

        // info panel
        VBox info = new VBox(6);
        info.setPadding(new Insets(14, 12, 14, 14));
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(row.getName());
        name.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 16));
        name.setTextFill(Color.WHITE);

        HBox priceQty = new HBox(16);
        priceQty.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(String.format("₪ %.2f", row.getPrice()));
        price.setFont(Font.font("Nunito", FontWeight.BOLD, 15));
        price.setTextFill(Color.hsb(48, 1, 0.92, 1));

        boolean isPackage = "Package".equalsIgnoreCase(row.getItemType());
        String qtyText = isPackage
            ? "× " + (int) row.getQuantity()
            : String.format("× %.3f kg", row.getQuantity());
        Label qty = new Label(qtyText);
        qty.setFont(Font.font("Nunito", 14));
        qty.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

        Label subtotal = new Label(String.format("= ₪ %.2f", row.getPrice() * row.getQuantity()));
        subtotal.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        subtotal.setTextFill(Color.hsb(120, 0.45, 0.72, 1));

        priceQty.getChildren().addAll(price, qty, subtotal);

        String typeTxt = row.getItemType() != null ? row.getItemType().toUpperCase() : "";
        Label typeBadge = new Label(typeTxt);
        typeBadge.setFont(Font.font("Nunito", FontWeight.BOLD, 11));
        typeBadge.setTextFill(Color.hsb(48, 0.9, 0.30, 1));
        typeBadge.setPadding(new Insets(2, 8, 2, 8));
        typeBadge.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(5), null)));

        info.getChildren().addAll(name, priceQty, typeBadge);

        // clicking image or info panel opens item detail
        Item detailItem = new Item(row.getItemId(), row.getName(), row.getPrice(),
            0, row.getItemType(), row.getImagePath(), null, null);
        imgBox.setOnMouseClicked(e -> sys.openItemDetail(detailItem));
        info.setOnMouseClicked(e -> sys.openItemDetail(detailItem));

        // remove button
        ImageView trashIcon = new ImageView(
            new Image(getClass().getResourceAsStream("/app/db_proj/icons8-trash-96.png")));
        Button removeBtn = new Buttons(null, null, trashIcon, 24).getBtn();
        removeBtn.setPadding(new Insets(12, 14, 12, 8));
        removeBtn.setOnAction(e -> {
            if (sys.getCurrentUserId() != null)
                Cart_Logic.removeFromCart(sys.getConn(), sys.getCurrentUserId(), row.getItemId());
            loadCartItems(search_tf != null ? search_tf.getText().trim() : "");
        });

        card.getChildren().addAll(imgBox, info, removeBtn);
        return card;
    }

    private Image loadItemImage(String path) {
        if (path != null && !path.isBlank()) {
            try { return new Image(new File(path).toURI().toString()); }
            catch (Exception ignored) {}
        }
        return new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));
    }

    private void updateTotal(double total) {
        if (total_price_lbl != null)
            total_price_lbl.setText(String.format("₪ %.2f", total));
    }

    private void make_cart_side() {
        cart_side = new VBox(10);
        cart_side.setPadding(new Insets(15, 15, 15, 15));
        cart_side.prefWidthProperty().bind(screen.widthProperty().multiply(0.60));

        search_tf = new TextField();
        search_tf.setPromptText("Search in Cart");
        search_tf.setFont(Font.font("Nunito", 20));
        search_tf.setStyle("-fx-control-inner-background: transparent; -fx-background-color: transparent; -fx-text-fill: #d4c0a0; -fx-prompt-text-fill: #5a4a38;");

        HBox searchBox = new HBox(0);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.30, 1), new CornerRadii(8), null)));
        HBox.setHgrow(search_tf, Priority.ALWAYS);

        ImageView iv = new ImageView(
            new Image(getClass().getResourceAsStream("/app/db_proj/search.png")));
        Button search_exec = new Buttons(null,
            new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(8), null), iv, 23).getBtn();
        search_exec.setMaxHeight(Double.MAX_VALUE);
        searchBox.setMaxHeight(45);
        searchBox.getChildren().addAll(search_tf, search_exec);

        search_exec.setOnAction(e -> loadCartItems(search_tf.getText().trim()));
        search_tf.setOnAction(e -> loadCartItems(search_tf.getText().trim()));

        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Coffee", "Roasts", "Spice", "Package");
        filterBox.setValue("All");
        filterBox.setStyle(
            "-fx-background-color: hsb(35, 14%, 30%); -fx-font-family: 'Nunito'; " +
            "-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8;");
        filterBox.buttonCellProperty().set(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Filter" : item);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setFont(Font.font("Nunito", 15));
                setBackground(Background.EMPTY);
            }
        });
        filterBox.setPrefHeight(45);
        filterBox.setPrefWidth(120);
        filterBox.setCursor(Cursor.HAND);
        filterBox.setOnAction(e -> {
            currentFilter = filterBox.getValue() != null ? filterBox.getValue() : "All";
            loadCartItems(search_tf.getText().trim());
        });

        sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Default", "Price: Low to High", "Price: High to Low", "Name: A-Z", "Name: Z-A");
        sortBox.setValue("Default");
        sortBox.setStyle(
            "-fx-background-color: hsb(35, 14%, 30%); -fx-font-family: 'Nunito'; " +
            "-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8;");
        sortBox.buttonCellProperty().set(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sort" : item);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setFont(Font.font("Nunito", 15));
                setBackground(Background.EMPTY);
            }
        });
        sortBox.setPrefHeight(45);
        sortBox.setPrefWidth(160);
        sortBox.setCursor(Cursor.HAND);
        sortBox.setOnAction(e -> {
            currentSort = sortBox.getValue() != null ? sortBox.getValue() : "Default";
            loadCartItems(search_tf.getText().trim());
        });

        HBox topBar = new HBox(8);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER);
        topBar.getChildren().addAll(searchBox, filterBox, sortBox);

        SP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        SP.setFitToWidth(true);
        cart_scroll.setStyle("-fx-background-color: transparent;");

        cart_side.getChildren().addAll(topBar, SP);
        VBox.setVgrow(SP, Priority.ALWAYS);
    }

    private void make_payment_side() {
        payment_side = new VBox(10);
        payment_side.setPadding(new Insets(15, 15, 15, 15));
        payment_side.prefWidthProperty().bind(screen.widthProperty().multiply(.40));

        make_payment_box();
        make_order_box();

        payment_side.getChildren().addAll(payment_box, orders_box);
    }

    private void make_payment_box() {
        payment_box = new BorderPane();
        payment_box.setPadding(new Insets(7));
        payment_box.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(12), null)));

        Label title_in_box = new Labels("Purchase",
            Font.font("Adwaita Mono", FontWeight.BOLD, 30), Color.hsb(48, 1, 0.92, 1)).getLabel();
        title_in_box.setPadding(new Insets(5));
        payment_box.setTop(title_in_box);

        VBox form_in_box = new VBox(7);
        form_in_box.setPadding(new Insets(7, 0, 7, 0));
        Label type_of_payment = new Labels("Type of payment",
            Font.font("Adwaita Mono", 19), Color.hsb(30, 0.12, 0.78, 1)).getLabel();

        HBox toggle = new HBox(0);
        toggle.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0.25, 1), new CornerRadii(10), null)));
        toggle.setMaxWidth(Region.USE_PREF_SIZE);

        Button inStoreBtn = new Button("In-Store");
        Button onlineBtn  = new Button("Online");
        for (Button b : new Button[]{inStoreBtn, onlineBtn}) {
            b.setFont(Font.font("Nunito", 15));
            b.setPadding(new Insets(5, 14, 5, 14));
            b.setCursor(Cursor.HAND);
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        }
        inStoreBtn.setBackground(new Background(new BackgroundFill(
            Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
        inStoreBtn.setTextFill(Color.BLACK);

        VBox paymentForm = makePaymentForm();
        paymentForm.setVisible(false);
        paymentForm.setManaged(false);

        onlineBtn.setOnAction(e -> {
            onlineBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            onlineBtn.setTextFill(Color.BLACK);
            inStoreBtn.setBackground(Background.EMPTY);
            inStoreBtn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            paymentForm.setVisible(true);
            paymentForm.setManaged(true);
        });
        inStoreBtn.setOnAction(e -> {
            inStoreBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.92, 1), new CornerRadii(10), null)));
            inStoreBtn.setTextFill(Color.BLACK);
            onlineBtn.setBackground(Background.EMPTY);
            onlineBtn.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            paymentForm.setVisible(false);
            paymentForm.setManaged(false);
        });

        toggle.getChildren().addAll(inStoreBtn, onlineBtn);

        HBox radios = new HBox(10);
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        radios.getChildren().addAll(type_of_payment, spacer, toggle);

        form_in_box.getChildren().addAll(radios, paymentForm);
        form_in_box.setBorder(new Border(new BorderStroke(
            Color.hsb(48, 1, 0.92, 1), BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 2, 0))));
        payment_box.setCenter(form_in_box);

        VBox bottom = new VBox(7);
        HBox total = new HBox();
        VBox.setMargin(total, new Insets(7, 0, 0, 0));
        Label total_text = new Labels("Total: ",
            Font.font("Adwaita Mono", FontWeight.BOLD, 23), Color.hsb(48, 1, 0.92, 1)).getLabel();
        total_price_lbl = new Labels("₪ 0.00",
            Font.font("Adwaita Mono", 23), Color.hsb(48, 1, 0.92, 1)).getLabel();
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        total.getChildren().addAll(total_text, spacer, total_price_lbl);

        Button order_btn = new Buttons("Purchase",
            new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)).getBtn();
        order_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        order_btn.setMaxWidth(Double.MAX_VALUE);
        order_btn.setOnAction(e -> {
            Integer uid = sys.getCurrentUserId();
            if (uid == null) return;
            int orderId = OrderDAO.placeOrder(sys.getConn(), uid);
            if (orderId > 0) {
                loadCartItems("");
                refreshOrders();
                updateTotal(0.0);
                if (search_tf != null) search_tf.clear();
            }
        });

        bottom.getChildren().addAll(total, order_btn);
        payment_box.setBottom(bottom);
    }

    private void make_order_box() {
        orders_box = new BorderPane();
        orders_box.setPadding(new Insets(7));
        orders_box.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(12), null)));

        HBox top = new HBox(0);

        Label title_in_box = new Labels("My Orders",
            Font.font("Adwaita Mono", FontWeight.BOLD, 30), Color.hsb(48, 1, 0.92, 1)).getLabel();
        title_in_box.setPadding(new Insets(5));

        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(0, 0, 7, 0));
        top.getChildren().add(title_in_box);

        orders_box.setTop(top);

        orders_vb = new VBox(7);
        orders_vb.getChildren().addAll(CartOrdersUI.build(sys).getChildren());
        ScrollPane scroll_orders = new ScrollPane(orders_vb);
        scroll_orders.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll_orders.setFitToWidth(true);

        orders_box.setCenter(scroll_orders);
        VBox.setVgrow(orders_box, Priority.ALWAYS);
    }

    private VBox makePaymentForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(10, 0, 0, 0));

        form.getChildren().addAll(
            payField("Card Number (1234 5678 9012 3456)"),
            new HBox(10) {{
                TextField e = payField("Expiry (MM/YY)");
                TextField c = payField("CVV");
                HBox.setHgrow(e, Priority.ALWAYS);
                HBox.setHgrow(c, Priority.ALWAYS);
                getChildren().addAll(e, c);
            }},
            payField("Cardholder Name"),
            payField("Delivery Address")
        );
        return form;
    }

    private TextField payField(String prompt) {
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
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    public HBox getScreen() {
        return screen;
    }
}

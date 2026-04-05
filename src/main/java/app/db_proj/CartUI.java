package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.DayOfWeek;

public class CartUI {
    private HBox screen;
    private StackPane root;
    private VBox cart_side, cart_scroll, payment_side;
    private BorderPane payment_box;
    private ScrollPane SP;
    private SystemHandling sys;

    public CartUI(SystemHandling sys) {
        this.sys = sys;
        screen = new HBox(10);
        screen.setPrefWidth(Double.MAX_VALUE);



        cart_scroll = new VBox(10);
        Image img = new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));
        cart_scroll.getChildren().add(makeItemCard("Ethiopian Blend", 12.00, img));

        SP = new ScrollPane(cart_scroll);

        make_cart_side();
        make_payment_side();

        screen.getChildren().addAll(cart_side, payment_side);
    }

    private void make_cart_side() {
        cart_side = new VBox(10);
        cart_side.setPadding(new Insets(0, 15, 15, 15));
        cart_side.prefWidthProperty().bind(screen.widthProperty().multiply(0.60));

        HBox searchBox = new HBox(0);
        searchBox.setMaxWidth(Double.MAX_VALUE);

        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(0));
        searchBox.setMaxHeight(52);

        TextField search_tf = new TextField();
        search_tf.setPromptText("Search in Cart");
        search_tf.setFont(Font.font("Roboto", 20));
        search_tf.setBackground(null);
        search_tf.setStyle("-fx-text-fill: white;");



        ImageView iv = new ImageView(new Image("file:/home/yahya/IdeaProjects/DB_Proj/target/classes/app/db_proj/search.png"));
        Button search_exec = new Buttons(null, new BackgroundFill(Color.hsb(48, 1, .85, 1), new CornerRadii(8), null), iv, 23).getBtn();

        searchBox.getChildren().addAll(search_tf, search_exec);
        searchBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(8), null)));
        HBox.setHgrow(search_tf, Priority.ALWAYS);
        search_exec.setMaxHeight(Double.MAX_VALUE);
        searchBox.setMaxHeight(45);


        SP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        SP.setFitToWidth(true);
        cart_scroll.setStyle("-fx-background-color: transparent;");

        cart_side.getChildren().addAll(searchBox, SP);
    }

    private void make_payment_side() {
        Region spacer;
        payment_side = new VBox(0);
        payment_side.setPadding(new Insets(0, 15, 15, 15));
        payment_side.prefWidthProperty().bind(screen.widthProperty().multiply(.40));

        payment_box = new BorderPane();
        payment_box.setPadding(new Insets(7));
        payment_box.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(12), null)));
        payment_box.setMaxHeight(Double.MAX_VALUE);

        Label title_in_box = new Labels("Purchase", Font.font("Adwaita Mono", FontWeight.BOLD, 30), Color.hsb(48, 1, .85, 1)).getLabel();
        title_in_box.setPadding(new Insets(5));
        payment_box.setTop(title_in_box);

        VBox form_in_box = new VBox(7);
        form_in_box.setPadding(new Insets(7, 0, 7, 0));
        Label type_of_payment = new Labels("Type of payment", Font.font("Adwaita Mono", 19), Color.hsb(0, 0, 0.75, 1)).getLabel();
        HBox radios = new HBox(10);
        // Create the toggle container
        HBox toggle = new HBox(0);
        toggle.setBackground(new Background(new BackgroundFill(
                Color.hsb(215, 0.6, 0.14, 1), new CornerRadii(10), null)));
        toggle.setMaxWidth(Region.USE_PREF_SIZE);

        Button inStoreBtn = new Button("In-Store");
        Button onlineBtn  = new Button("Online");
        // Shared styling
        for (Button b : new Button[]{inStoreBtn, onlineBtn}) {
            b.setFont(Font.font("Roboto", 15));
            b.setPadding(new Insets(5, 14, 5, 14));
            b.setCursor(Cursor.HAND);
            b.setBackground(Background.EMPTY);
            b.setTextFill(Color.hsb(0, 0, 0.75, 1));
        }
        // Default selected = In-Store
        inStoreBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.85, 1), new CornerRadii(10), null)));
        inStoreBtn.setTextFill(Color.BLACK);
        // Payment form (hidden by default)
        VBox paymentForm = makePaymentForm();
        paymentForm.setVisible(false);
        paymentForm.setManaged(false); // won't take space when hidden

        onlineBtn.setOnAction(e -> {
            onlineBtn.setBackground(new Background(new BackgroundFill(
                    Color.hsb(48, 1, 0.85, 1), new CornerRadii(10), null)));
            onlineBtn.setTextFill(Color.BLACK);
            inStoreBtn.setBackground(Background.EMPTY);
            inStoreBtn.setTextFill(Color.hsb(0, 0, 0.75, 1));

            paymentForm.setVisible(true);
            paymentForm.setManaged(true);
        });

        inStoreBtn.setOnAction(e -> {
            inStoreBtn.setBackground(new Background(new BackgroundFill(
                    Color.hsb(48, 1, 0.85, 1), new CornerRadii(10), null)));
            inStoreBtn.setTextFill(Color.BLACK);
            onlineBtn.setBackground(Background.EMPTY);
            onlineBtn.setTextFill(Color.hsb(0, 0, 0.75, 1));

            paymentForm.setVisible(false);
            paymentForm.setManaged(false);
        });


        toggle.getChildren().addAll(inStoreBtn, onlineBtn);

        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        radios.getChildren().addAll(type_of_payment, spacer, toggle);
        form_in_box.getChildren().addAll(radios, paymentForm);
        form_in_box.setBorder(new Border(new BorderStroke(Color.hsb(48, 1, .85, 1), BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 2, 0))));
        payment_box.setCenter(form_in_box);

        VBox bottom = new VBox(7);
        HBox total = new HBox();
        VBox.setMargin(total, new Insets(7, 0, 0,0));
        Label total_text = new Labels("Total: ", Font.font("Adwaita Mono", FontWeight.BOLD, 23), Color.hsb(48, 1, .85, 1)).getLabel();
        Label total_price = new Labels("$100", Font.font("Adwaita Mono", 23), Color.hsb(48, 1, .85, 1)).getLabel();;
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        total.getChildren().addAll(total_text, spacer, total_price);

        Button order_btn = new Buttons("Purchase", new BackgroundFill(Color.hsb(48, 1, .85, 1), new CornerRadii(12), null)).getBtn();
        order_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        order_btn.setMaxWidth(Double.MAX_VALUE);

        bottom.getChildren().addAll(total, order_btn);
        payment_box.setBottom(bottom);

        payment_side.getChildren().add(payment_box);
    }


    private VBox makePaymentForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(10, 0, 0, 0));

        TextField cardNumber = formField("Card Number (1234 5678 9012 3456)");

        HBox row = new HBox(10);
        TextField expiry = formField("Expiry (MM/YY)");
        TextField cvv    = formField("CVV");
        HBox.setHgrow(expiry, Priority.ALWAYS);
        HBox.setHgrow(cvv, Priority.ALWAYS);
        row.getChildren().addAll(expiry, cvv);

        TextField cardHolder = formField("Cardholder Name");

        form.getChildren().addAll(cardNumber, row, cardHolder);
        return form;
    }

    private TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Roboto", 15));
        tf.setBackground(new Background(new BackgroundFill(
                Color.hsb(215, 0.6, 0.14, 1), new CornerRadii(7), null)));
        tf.setStyle("-fx-text-fill: white;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private HBox makeItemCard(String itemName, double price, Image itemImage) {
        HBox card = new HBox(15);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(12), null)));

        // Item image
        ImageView img = new ImageView(itemImage);
        img.setFitWidth(70);
        img.setFitHeight(70);
        img.setPreserveRatio(true);

        // Name + price
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(itemName);
        name.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 17));
        name.setTextFill(Color.WHITE);

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setFont(Font.font("Roboto", 16));
        priceLabel.setTextFill(Color.hsb(48, 1, 0.85, 1)); // gold

        info.getChildren().addAll(name, priceLabel);

        // Remove button
        ImageView trashIcon = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-trash-96.png")));


        Button removeBtn = new Buttons(null, null, trashIcon, 28).getBtn();
        removeBtn.setPadding(new Insets(8));
        removeBtn.setOnAction(e -> {
            VBox parent = (VBox) card.getParent();
            parent.getChildren().remove(card);
        });

        card.getChildren().addAll(img, info, removeBtn);
        return card;
    }



    public HBox getScreen() {
        return screen;
    }
}

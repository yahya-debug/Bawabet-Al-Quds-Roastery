package app.db_proj;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class Navigation_Bar {
    private BorderPane Top;
    private HBox titleBox, nav, right;
    private SystemHandling sys;
    private Button Login, SignUp;

    public Navigation_Bar(SystemHandling sys) {
        this.sys = sys;

        // initialize the nodes;
        Top = new BorderPane();
        titleBox = new HBox();
        nav = CenterBlock();
        right = RightBlock(sys.isAuthenticated());



        Top.setPadding(new Insets(20, 15, 15, 15));
        titleBox.setPadding(new Insets(12));
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(12), null)));
        titleBox.setCursor(Cursor.HAND);
        titleBox.setOnMouseClicked(e -> {
            sys.changePage(Page.HOME);
            right = RightBlock(sys.isAuthenticated());
            nav = CenterBlock();

            Top.setCenter(nav);
            Top.setRight(right);
            BorderPane.setAlignment(titleBox, Pos.CENTER);
            BorderPane.setAlignment(nav, Pos.CENTER);
            BorderPane.setAlignment(right, Pos.CENTER);
        });

        Label title = new Label("Bawabet Al-Quds");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 35));
        title.setTextFill(Color.hsb(48, 1, .85, 1));


        // append nodes
        titleBox.getChildren().add(title);
        Top.setLeft(titleBox);
        Top.setCenter(nav);
        Top.setRight(right);
        BorderPane.setAlignment(titleBox, Pos.CENTER);
        BorderPane.setAlignment(nav, Pos.CENTER);
        BorderPane.setAlignment(right, Pos.CENTER);
    }

    public HBox RightBlock(boolean authenticated) {
        HBox retBox = new HBox(10);
        retBox.setAlignment(Pos.CENTER);
        retBox.setPadding(new Insets(0));
        retBox.setMaxHeight(52);

        if (authenticated) {
            ImageView cart_icon = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-shopping-cart-96.png")));
            ImageView profile_icon = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-profile-96.png")));

            Button open_cart_btn = new Buttons(null, null, cart_icon, 48).getBtn();
            Button open_profile_btn = new Buttons(null, null, profile_icon, 53).getBtn();

            open_cart_btn.setOnAction(e -> sys.showOverlay(buildCartOverlay()));
            open_profile_btn.setOnAction(e -> sys.showOverlay(buildProfileOverlay(), Pos.TOP_RIGHT, new Insets(85, 15, 0, 0)));

            retBox.getChildren().addAll(open_cart_btn, open_profile_btn);
            return retBox;
        }

        Button Auth = new Button("Register");
        Auth.setPadding(new Insets(10, 20, 10, 20));
        Auth.setBackground(Background.EMPTY);
        Auth.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 23));
        Auth.setTextFill(Color.hsb(0, 0, 0.75, 1));
        Auth.setTextFill(Color.hsb(215, 0.6, 0.14, 1));
        Auth.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.85, 1), new CornerRadii(13), null)));
        Auth.setCursor(Cursor.HAND);

        Auth.setOnAction(new GoToAuth(Page.Login, sys));
        retBox.getChildren().add(Auth);
        return retBox;
    }

    public HBox CenterBlock() {
        HBox retBox = new HBox(0);
        retBox.setPrefWidth(400);
        retBox.setMaxWidth(400);
        retBox.setAlignment(Pos.CENTER);
        retBox.setPadding(new Insets(0));
        retBox.setMaxHeight(52);

        TextField search_tf = new TextField();
        search_tf.setPromptText("Search");
        search_tf.setFont(Font.font("Roboto", 23));
        search_tf.setBackground(null);
        search_tf.setStyle("-fx-text-fill: white;");



        ImageView iv = new ImageView(new Image("file:/home/yahya/IdeaProjects/DB_Proj/target/classes/app/db_proj/search.png"));
        iv.setFitHeight(25);
        iv.setFitWidth(25);
        Button search_exec = new Button(null, iv);
        search_exec.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, .85, 1), new CornerRadii(12), null)));
        search_exec.setPadding(new Insets(10));
        search_exec.setCursor(Cursor.HAND);

        retBox.getChildren().addAll(search_tf, search_exec);
        retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(12), null)));
        HBox.setHgrow(search_tf, Priority.ALWAYS);
        search_exec.setMaxHeight(Double.MAX_VALUE);
        retBox.setMaxHeight(45);

        return retBox;
    }

    public BorderPane getTop() {
        return Top;
    }

    public HBox getRight() {
        return right;
    }

    public Button getLogin() {
        return Login;
    }

    public Button getSignUp() {
        return SignUp;
    }


    public HBox Switch_auth() {
        HBox retBox = new HBox();
        retBox.setAlignment(Pos.CENTER);
        retBox.setPadding(new Insets(0));
        retBox.setMaxHeight(52);
        retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(17), null)));

        Login = new Button("Login");
        SignUp = new Button("Sign up");
        Login.setPadding(new Insets(5, 15, 5, 15));
        SignUp.setPadding(new Insets(5, 15, 5, 15));
        Login.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 23));
        SignUp.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 23));
        Login.setTextFill(Color.BLACK);
        SignUp.setTextFill(Color.BLACK);

        SignUp.setBackground(Background.EMPTY);
        HBox.setHgrow(Login, Priority.ALWAYS);
        Login.setMaxHeight(Double.MAX_VALUE);
        SignUp.setMaxHeight(Double.MAX_VALUE);
        Login.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.7, 1), new CornerRadii(12), null)));

        Login.setCursor(Cursor.HAND);
        SignUp.setCursor(Cursor.HAND);

        retBox.getChildren().addAll(Login, SignUp);

        Login.setOnAction(new GoToAuth(Page.Login, sys));
        SignUp.setOnAction(new GoToAuth(Page.SignUp, sys));
        return retBox;
    }
    /** Builds the two-column cart panel: left = search + item list, right = payment. */
    private VBox buildCartOverlay() {
        // ── Outer container ──────────────────────────────────────────────────
        VBox outer = new VBox(16);
        outer.setPadding(new Insets(24));
        outer.setPrefWidth(900);
        outer.setPrefHeight(580);
        outer.setMaxHeight(580);
        outer.setBackground(new Background(new BackgroundFill(
                Color.hsb(215, 0.55, 0.18, 1), new CornerRadii(16), null)));
        outer.setOnMouseClicked(javafx.event.Event::consume);

        // ── Header row (title + close) ────────────────────────────────────────
        Label title = new Label("Your Basket");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 26));
        title.setTextFill(Color.hsb(48, 1, 0.85, 1));

        Button close = new Button("✕");
        close.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        close.setBackground(Background.EMPTY);
        close.setTextFill(Color.hsb(0, 0, 0.65, 1));
        close.setCursor(Cursor.HAND);
        close.setOnAction(e -> sys.hideOverlay());

        HBox header = new HBox(title);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().add(close);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── LEFT column: search bar + scrollable item list ────────────────────
        // Search bar
        TextField cartSearch = new TextField();
        cartSearch.setPromptText("Search in cart…");
        cartSearch.setFont(Font.font("Adwaita Mono", 16));
        cartSearch.setBackground(null);
        cartSearch.setStyle("-fx-text-fill: white;");

        Button cartSearchBtn = new Button("Search");
        cartSearchBtn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 14));
        cartSearchBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.75, 1), new CornerRadii(10), null)));
        cartSearchBtn.setPadding(new Insets(7, 14, 7, 14));
        cartSearchBtn.setCursor(Cursor.HAND);

        HBox searchBar = new HBox(cartSearch, cartSearchBtn);
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setBackground(new Background(new BackgroundFill(
                Color.hsb(0, 0, 0.24, 1), new CornerRadii(10), null)));
        searchBar.setPadding(new Insets(4, 6, 4, 10));
        HBox.setHgrow(cartSearch, Priority.ALWAYS);
        cartSearchBtn.setMaxHeight(Double.MAX_VALUE);

        // Item list inside a ScrollPane
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(8));

        Label emptyNote = new Label("Your cart is empty.");
        emptyNote.setFont(Font.font("Adwaita Mono", 15));
        emptyNote.setTextFill(Color.hsb(0, 0, 0.55, 1));
        itemList.getChildren().add(emptyNote);
        // TODO: replace emptyNote with actual cart item rows

        ScrollPane itemScroll = new ScrollPane(itemList);
        itemScroll.setFitToWidth(true);
        itemScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemScroll.setBackground(new Background(new BackgroundFill(
                Color.hsb(215, 0.5, 0.14, 1), new CornerRadii(10), null)));
        itemScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(itemScroll, Priority.ALWAYS);

        VBox leftCol = new VBox(10, searchBar, itemScroll);
        leftCol.setPadding(new Insets(0, 12, 0, 0));
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // ── RIGHT column: payment method ──────────────────────────────────────
        Label payTitle = new Label("Payment Method");
        payTitle.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        payTitle.setTextFill(Color.hsb(48, 1, 0.85, 1));

        ToggleGroup payGroup = new ToggleGroup();

        RadioButton cashRb = new RadioButton("Cash on Delivery");
        RadioButton cardRb = new RadioButton("Credit / Debit Card");
        cashRb.setToggleGroup(payGroup);
        cardRb.setToggleGroup(payGroup);
        cashRb.setSelected(true);
        for (RadioButton rb : new RadioButton[]{cashRb, cardRb}) {
            rb.setFont(Font.font("Adwaita Mono", 15));
            rb.setTextFill(Color.hsb(0, 0, 0.85, 1));
            rb.setCursor(Cursor.HAND);
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");

        Label totalLabel = new Label("Total:  $0.00");
        totalLabel.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 17));
        totalLabel.setTextFill(Color.WHITE);

        Button orderBtn = new Button("Place Order");
        orderBtn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 16));
        orderBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.80, 1), new CornerRadii(12), null)));
        orderBtn.setPadding(new Insets(12, 0, 12, 0));
        orderBtn.setMaxWidth(Double.MAX_VALUE);
        orderBtn.setCursor(Cursor.HAND);

        VBox rightCol = new VBox(14, payTitle, cashRb, cardRb, sep, totalLabel, orderBtn);
        rightCol.setPrefWidth(260);
        rightCol.setMinWidth(260);
        rightCol.setPadding(new Insets(0, 0, 0, 12));
        rightCol.setAlignment(Pos.TOP_LEFT);

        // Left border on right column as a visual divider
        rightCol.setStyle("-fx-border-color: rgba(255,255,255,0.12); -fx-border-width: 0 0 0 1;");

        // ── Body row ──────────────────────────────────────────────────────────
        HBox body = new HBox(leftCol, rightCol);
        VBox.setVgrow(body, Priority.ALWAYS);

        outer.getChildren().addAll(header, body);
        return outer;
    }

    /** Builds the floating profile panel shown when the profile button is clicked. */
    private VBox buildProfileOverlay() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setMinWidth(260);
        box.setBackground(new Background(new BackgroundFill(
                Color.hsb(215, 0.55, 0.18, 1), new CornerRadii(14), null)));

        Label title = new Label("My Account");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 22));
        title.setTextFill(Color.hsb(48, 1, 0.85, 1));

        Button logoutBtn = new Button("Log out");
        logoutBtn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 15));
        logoutBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(0, 0.65, 0.65, 1), new CornerRadii(10), null)));
        logoutBtn.setPadding(new Insets(8, 18, 8, 18));
        logoutBtn.setCursor(Cursor.HAND);
        logoutBtn.setTextFill(Color.WHITE);
        logoutBtn.setOnAction(e -> sys.hideOverlay());

        Button close = new Button("Close");
        close.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 15));
        close.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.75, 1), new CornerRadii(10), null)));
        close.setPadding(new Insets(8, 18, 8, 18));
        close.setCursor(Cursor.HAND);
        close.setOnAction(e -> sys.hideOverlay());

        box.getChildren().addAll(title, logoutBtn, close);
        box.setOnMouseClicked(javafx.event.Event::consume);
        return box;
    }

    class GoToAuth implements EventHandler<ActionEvent> {
        private Page page;
        public GoToAuth(Page page, SystemHandling sys) {
            this.page = page;
        }
        @Override
        public void handle(ActionEvent event) {
            Top.setRight(Switch_auth());
            Top.setCenter(null);
            sys.changePage(page);
        }
    }

}

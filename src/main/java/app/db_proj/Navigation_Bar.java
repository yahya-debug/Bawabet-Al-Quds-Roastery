package app.db_proj;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Navigation_Bar {
    private BorderPane Top;
    private HBox left, titleBox, nav, right;
    private SystemHandling sys;
    private Button Login, SignUp;

    public Navigation_Bar(SystemHandling sys) {
        this.sys = sys;

        // initialize the nodes;
        Top = new BorderPane();
        left = new HBox(7);
        titleBox = new HBox();
        nav = CenterBlock();
        right = RightBlock(sys.isAuthenticated());



        Top.setPadding(new Insets(20, 15, 20, 15));
        titleBox.setPadding(new Insets(12));
        left.setAlignment(Pos.CENTER);
        titleBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(12), null)));
        titleBox.setCursor(Cursor.HAND);
        Label title = new Label("Bawabet Al-Quds");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 35));
        title.setTextFill(Color.hsb(48, 1, .85, 1));
        titleBox.setOnMouseClicked(e -> {
            sys.changePage(Page.HOME);
            right = RightBlock(sys.isAuthenticated());
            nav = CenterBlock();
            left.getChildren().clear();


            left.getChildren().addAll(titleBox);


            Top.setLeft(left);
            Top.setCenter(nav);
            Top.setRight(right);
            BorderPane.setAlignment(left, Pos.CENTER);
            BorderPane.setAlignment(nav, Pos.CENTER);
            BorderPane.setAlignment(right, Pos.CENTER);

        });



        // append nodes
        titleBox.getChildren().add(title);
        left.getChildren().add(titleBox);
        Top.setLeft(left);
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

            open_cart_btn.setOnAction(new GoToCart());
            open_profile_btn.setOnAction(new GoToProf());

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
        search_tf.setFont(Font.font("Roboto Rounded-MT", 23));
        search_tf.setBackground(null);
        search_tf.setStyle("-fx-text-fill: white;");



        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/search.png")));
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
        Login.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.85, 1), new CornerRadii(12), null)));

        Login.setCursor(Cursor.HAND);
        SignUp.setCursor(Cursor.HAND);

        retBox.getChildren().addAll(Login, SignUp);

        Login.setOnAction(new GoToAuth(Page.Login, sys));
        SignUp.setOnAction(new GoToAuth(Page.SignUp, sys));
        return retBox;
    }



    // Actions
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
    class GoToCart implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            switch (sys.getCurPage()) {
                case CART -> {
                    return;
                }
            }
            Label text = new Label("Cart");
            text.setTextFill(Color.hsb(48, 1, .85, 1));
            text.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 30));
            left.getChildren().add(text);
            Top.setCenter(null);
            sys.changePage(Page.CART);
        }
    }
    class GoToProf implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            sys.changePage(Page.PROFILE);
        }
    }

}

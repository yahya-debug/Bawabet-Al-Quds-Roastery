package app.db_proj.UI;

import app.db_proj.Page;
import app.db_proj.SystemHandling;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class Navigation_Bar {
    private BorderPane Top;
    private HBox left, titleBox, nav, right;
    private SystemHandling sys;
    private Button Login, SignUp;
    private TextField search_tf;
    private Button search_exec_stored;
    private Consumer<String> searchAction;

    public Navigation_Bar(SystemHandling sys) {
        this.sys = sys;

        // initialize the nodes;
        Top = new BorderPane();
        left = new HBox(7);
        titleBox = new HBox();
        nav = CenterBlock();
        right = RightBlock(sys.isAuthenticated());



        Top.setPadding(new Insets(14, 20, 14, 20));
        Top.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.22, 0.15, 1), new CornerRadii(0), null)));
        Top.setBorder(new Border(new BorderStroke(Color.hsb(48, 0.55, 0.32, 1), BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 2, 0))));
        titleBox.setPadding(new Insets(10, 14, 10, 14));
        left.setAlignment(Pos.CENTER);
        titleBox.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.18, 0.26, 1), new CornerRadii(12), null)));
        titleBox.setCursor(Cursor.HAND);
        Label title = new Label("Bawabet Al-Quds");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 35));
        title.setTextFill(Color.hsb(48, 1, 0.92, 1));
        titleBox.setOnMouseClicked(e -> {
            right = RightBlock(sys.isAuthenticated());
            left.getChildren().clear();
            left.getChildren().add(titleBox);
            Top.setLeft(left);
            Top.setRight(right);
            BorderPane.setAlignment(left, Pos.CENTER);
            BorderPane.setAlignment(right, Pos.CENTER);
            if (sys.canAccessPanel()) {
                sys.changePage(Page.ADMIN);
                Top.setCenter(null);
            } else {
                sys.changePage(Page.HOME);
                nav = CenterBlock();
                Top.setCenter(nav);
                BorderPane.setAlignment(nav, Pos.CENTER);
            }
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
            if (sys.canAccessPanel()) {
                String label = sys.isUserAdmin() ? "Admin Panel" : "My Portal";
                Button adminBtn = new Button(label);
                adminBtn.setPadding(new Insets(10, 22, 10, 22));
                adminBtn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(13), null)));
                adminBtn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
                adminBtn.setTextFill(Color.BLACK);
                adminBtn.setCursor(Cursor.HAND);
                adminBtn.setOnMouseEntered(e -> adminBtn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.75, 1), new CornerRadii(13), null))));
                adminBtn.setOnMouseExited(e -> adminBtn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(13), null))));
                adminBtn.setOnAction(e -> sys.changePage(Page.ADMIN));
                retBox.getChildren().add(adminBtn);
                return retBox;
            }

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
        Auth.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        Auth.setTextFill(Color.hsb(30, 0.50, 0.25, 1));
        Auth.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(13), null)));
        Auth.setCursor(Cursor.HAND);
        Auth.setOnMouseEntered(e -> Auth.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.75, 1), new CornerRadii(13), null))));
        Auth.setOnMouseExited(e -> Auth.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(13), null))));

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

        search_tf = new TextField();
        search_tf.setPromptText("Search");
        search_tf.setFont(Font.font("Nunito", 19));
        search_tf.setBackground(null);
        search_tf.setStyle("-fx-text-fill: white;");

        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/search.png")));
        iv.setFitHeight(20);
        iv.setFitWidth(20);
        Button search_exec = new Button(null, iv);
        search_exec_stored = search_exec;
        search_exec.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12, 12, 12, 12, false), null)));
        search_exec.setPadding(new Insets(10));
        search_exec.setCursor(Cursor.HAND);
        search_exec.setOnMouseEntered(e -> search_exec.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.75, 1), new CornerRadii(12, 12, 12, 12, false), null))));
        search_exec.setOnMouseExited(e -> search_exec.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12, 12, 12, 12, false), null))));

        if (searchAction != null) {
            search_exec.setOnAction(e -> searchAction.accept(search_tf.getText().trim()));
            search_tf.setOnAction(e -> searchAction.accept(search_tf.getText().trim()));
        }

        retBox.getChildren().addAll(search_tf, search_exec);
        retBox.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));
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
        retBox.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(17), null)));

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
        Login.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)));

        Login.setCursor(Cursor.HAND);
        SignUp.setCursor(Cursor.HAND);

        retBox.getChildren().addAll(Login, SignUp);

        Login.setOnAction(new GoToAuth(Page.Login, sys));
        SignUp.setOnAction(new GoToAuth(Page.SignUp, sys));
        return retBox;
    }


    public HBox getLeft() {
        return left;
    }

    // rebuild the right and center blocks based on the current auth state and role
    // called by SystemHandling after login or signup
    // admins get logo only, no search bar, no cart, no profile icons
    public void refreshAuth() {
        right = RightBlock(sys.isAuthenticated());
        Top.setRight(right);
        BorderPane.setAlignment(right, Pos.CENTER);
        if (sys.isAuthenticated() && sys.canAccessPanel()) {
            Top.setCenter(null);
        } else {
            nav = CenterBlock();
            Top.setCenter(nav);
            BorderPane.setAlignment(nav, Pos.CENTER);
        }
        Top.setLeft(left);
        BorderPane.setAlignment(left, Pos.CENTER);
    }

    public void setSearchAction(Consumer<String> action) {
        this.searchAction = action;
        if (search_exec_stored != null && search_tf != null) {
            search_exec_stored.setOnAction(e -> action.accept(search_tf.getText().trim()));
            search_tf.setOnAction(e -> action.accept(search_tf.getText().trim()));
        }
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

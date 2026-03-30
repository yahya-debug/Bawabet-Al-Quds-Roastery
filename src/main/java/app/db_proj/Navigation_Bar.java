package app.db_proj;

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
    private HBox titleBox, nav;
    private SystemHandling sys;

    public Navigation_Bar(SystemHandling sys) {
        this.sys = sys;

        // initialize the nodes;
        Top = new BorderPane();
        titleBox = new HBox();
        nav = new HBox(10);
        HBox right = RightBlock(sys.isAuthenticated());


        Top.setPadding(new Insets(20, 15, 15, 15));
        titleBox.setPadding(new Insets(12));
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(12), null)));

        Label title = new Label("Bawabet Al-Quds");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 35));
        title.setTextFill(Color.hsb(48, 1, .7, 1));
        // append nodes
        titleBox.getChildren().add(title);

        Top.setLeft(titleBox);
        Top.setCenter(nav);
        Top.setRight(right);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(right, Pos.CENTER);
    }

    public HBox RightBlock(boolean authenticated) {
        HBox retBox = new HBox(0);
        retBox.setAlignment(Pos.CENTER);
        retBox.setPadding(new Insets(0));
        retBox.setMaxHeight(52);
        if (authenticated) {
            TextField search_tf = new TextField();
            search_tf.setPromptText("Search");
            search_tf.setFont(Font.font("Adwaita Mono", 23));
            search_tf.setBackground(null);


            ImageView iv = new ImageView(new Image("file:/home/yahya/IdeaProjects/DB_Proj/target/classes/app/db_proj/search.png"));
            iv.setFitHeight(25);
            iv.setFitWidth(25);
            Button search_exec = new Button(null, iv);
            search_exec.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, .7, 1), new CornerRadii(12), null)));
            search_exec.setPadding(new Insets(10));
            search_exec.setCursor(Cursor.HAND);

            retBox.getChildren().addAll(search_tf, search_exec);
            retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(12), null)));
            HBox.setHgrow(search_tf, Priority.ALWAYS);
            search_exec.setMaxHeight(Double.MAX_VALUE);
            retBox.setMaxHeight(45);

            return retBox;
        }

        retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.28, 1), new CornerRadii(17), null)));
        Button Login = new Button("Login");
        Button SignUp = new Button("Sign up");
        Login.setPadding(new Insets(5, 15, 5, 15));
        SignUp.setPadding(new Insets(5, 15, 5, 15));
        Login.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 23));
        SignUp.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 23));
        Login.setTextFill(Color.BLACK);
        SignUp.setTextFill(Color.BLACK);

        SignUp.setBackground(Background.EMPTY);
        HBox.setHgrow(Login, Priority.ALWAYS);
        Login.setMaxHeight(Double.MAX_VALUE);
        Login.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.7, 1), new CornerRadii(17), null)));

        Login.setCursor(Cursor.HAND);
        SignUp.setCursor(Cursor.HAND);

        retBox.getChildren().addAll(Login, SignUp);
        return retBox;
    }

    public BorderPane getTop() {
        return Top;
    }
}

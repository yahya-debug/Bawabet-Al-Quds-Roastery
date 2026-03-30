package app.db_proj;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            SystemHandling sys = new SystemHandling();

            // init base blocks
            BorderPane root = new BorderPane();
            Navigation_Bar Top = new Navigation_Bar(sys);
            Auth_UI auth_page = new Auth_UI(false);

            // work on root

            root.setTop(Top.getTop());
            root.setCenter(auth_page.getUI());
            root.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), null, null)));

            Scene scene = new Scene(root);


            primaryStage.setScene(scene);
            primaryStage.setTitle("Bawabet Al-Quds");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


package app.db_proj;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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


            Scene scene = new Scene(sys.getRoot());


            primaryStage.setScene(scene);
            primaryStage.setTitle("Bawabet Al-Quds");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/app/db_proj/Logo.png")));
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}


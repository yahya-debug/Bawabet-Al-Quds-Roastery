package app.db_proj;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestItemTable extends Application {

    @Override
    public void start(Stage stage) {
        ItemTable_UI itemTable = new ItemTable_UI();

        Scene scene = new Scene(itemTable.getPage(), 1200, 650);

        stage.setTitle("Items Table");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
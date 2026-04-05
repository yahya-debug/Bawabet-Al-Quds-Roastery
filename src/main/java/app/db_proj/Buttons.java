package app.db_proj;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;

public class Buttons {
    private Button btn;

    public Buttons(String text, BackgroundFill bg) {
        btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        if (bg == null)
            btn.setStyle("-fx-background-color: transparent;");
        else btn.setBackground(new Background(bg));
    }

    public Buttons(String text, BackgroundFill bg, ImageView img, double width_height) {
        this(text, bg);
        img.setFitWidth(width_height);
        img.setFitHeight(width_height);
        btn.setGraphic(img);
    }

    public Buttons(String text, BackgroundFill bg, ImageView img, double width, double height) {
        this(text, bg);
        img.setFitHeight(height);
        img.setFitWidth(width);
        btn.setGraphic(img);
    }

    public Button getBtn() {
        return btn;
    }
}

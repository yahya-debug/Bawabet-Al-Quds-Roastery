package app.db_proj;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class Buttons {
    private Button btn;

    public Buttons(String text, BackgroundFill bg) {
        btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        btn.setFont(Font.font("Nunito"));
        if (bg == null) {
            btn.setStyle("-fx-background-color: transparent;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));
        } else {
            btn.setBackground(new Background(bg));
            Paint paint = bg.getFill();
            if (paint instanceof Color c) {
                Color hover = c.deriveColor(0, 1, 0.82, 1);
                btn.setOnMouseEntered(e -> btn.setBackground(new Background(new BackgroundFill(hover, bg.getRadii(), bg.getInsets()))));
                btn.setOnMouseExited(e -> btn.setBackground(new Background(bg)));
            }
        }
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

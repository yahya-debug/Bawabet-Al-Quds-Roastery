package app.db_proj;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Labels {
    private Label label;

    public Labels(String text, Font font, Color fill) {
        label = new Label(text);
        label.setFont(font);
        label.setTextFill(fill);
    }

    public Label getLabel() {
        return label;
    }
}

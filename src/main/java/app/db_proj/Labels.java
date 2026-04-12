package app.db_proj;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    static HBox info_fields(String title, String data) {
        HBox retBox = new HBox(0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label label1 = new Label(title);
        Label label2 = new Label(data);
        label1.setTextFill(Color.hsb(0, 0, 0.75, 1));
        label2.setTextFill(Color.hsb(0, 0, 0.75, 1));
        label1.setFont(Font.font("Nunito", FontWeight.BOLD, 20));
        label2.setFont(Font.font("Nunito", 20));

        retBox.getChildren().addAll(label1, spacer, label2);
        retBox.setMaxWidth(Double.MAX_VALUE);
        return retBox;
    }

    public Label getLabel() {
        return label;
    }
}

package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Home_UI {
    private ScrollPane SP;
    private FlowPane grid;
    private SystemHandling sys;
    private HBox packageBox;
    private VBox home;

    public Home_UI(SystemHandling sys) {
        this.sys = sys;
        home = new VBox(10);
        grid = new FlowPane();
        packageBox = new HBox();

        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(15));

        home.getChildren().add(grid);
        home.setPadding(new Insets(10));
        SP = new ScrollPane(home);

        SP.setFitToWidth(true);
        SP.setFitToHeight(false);
        SP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        SP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    public ScrollPane getSP() {
        return SP;
    }
}

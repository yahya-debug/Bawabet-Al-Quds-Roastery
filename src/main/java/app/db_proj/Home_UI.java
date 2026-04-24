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
        


        showPackage();
//        for (Item item : items) {
//            grid.getChildren().add(makeCard(item));
//        }

    }


    private void showPackage() {
        packageBox.setSpacing(10);
        packageBox.setPadding(new Insets(20));
        packageBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Image logo = new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));

        ImageView iv = new ImageView(logo);
        iv.setFitWidth(170);
        iv.setFitHeight(170);

        VBox info = new VBox(7);
        Label l = new Labels("Items: ", Font.font("Nunito", FontWeight.BOLD, 17), Color.BLACK).getLabel();
        Label c = new Labels("Coffee, tea, nuts", Font.font("Nunito", 17), Color.BLACK).getLabel();
        HBox row = new HBox(5);
        row.getChildren().addAll(l, c);
        info.getChildren().addAll(row);

        Button goTo = new Buttons(null, null, new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-right-96.png"))), 96).getBtn();
        packageBox.getChildren().addAll(iv, info, spacer, goTo);

        Region bgRegion = new Region();
        bgRegion.setBackground(new Background(new BackgroundImage(
            logo,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(1, 1, true, true, false, true)
        )));
        bgRegion.setOpacity(0.15);
        bgRegion.setManaged(false);

        StackPane wrapper = new StackPane();
        wrapper.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(15), null)));
        wrapper.setCursor(Cursor.HAND);
        bgRegion.prefWidthProperty().bind(wrapper.widthProperty());
        bgRegion.prefHeightProperty().bind(wrapper.heightProperty());

        wrapper.getChildren().addAll(bgRegion, packageBox);
        home.getChildren().add(0, wrapper);
    }

    public ScrollPane getSP() {
        return SP;
    }
}

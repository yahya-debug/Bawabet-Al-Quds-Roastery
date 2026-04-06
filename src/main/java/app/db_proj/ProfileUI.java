package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProfileUI {
    private StackPane SP;
    private VBox main_block;

    public ProfileUI(SystemHandling sys) {
        SP = new StackPane();
        SP.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0, .55), null, null)));
        SP.setOnMouseClicked(e -> sys.hideProf());

        main_block = new VBox(10);
        main_block.setPadding(new Insets(12));
        main_block.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(15), null)));
        main_block.prefWidthProperty().bind(SP.widthProperty().multiply(.3));
        main_block.prefHeightProperty().bind(SP.heightProperty().multiply(.5));
        main_block.maxWidthProperty().bind(SP.widthProperty().multiply(.3));
        main_block.maxHeightProperty().bind(SP.heightProperty().multiply(.5));
        main_block.setMinHeight(400);
        main_block.setMinWidth(355);

        // not exiting profile when clicking the main_box
        main_block.setOnMouseClicked(e -> e.consume());
        SP.getChildren().add(main_block);
        Base();
    }

    private void Base() {
        main_block.getChildren().clear();

        VBox head = new VBox(7);
        ImageView prof_ph = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-profile-96.png")));
        prof_ph.setFitHeight(96);
        prof_ph.setFitWidth(96);
        Label name = new Labels("Name", Font.font("Roboto Rounded MT", FontWeight.BOLD, 27), Color.WHITE).getLabel();

        HBox editProfBtn = profileBtn("Edit Profile");
        editProfBtn.setOnMouseClicked(e -> editProf());

        Button LogoutBtn = new Buttons("Log out", null).getBtn();
        LogoutBtn.setBorder(new Border(new BorderStroke(Color.hsb(5, 1, .4), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));
        LogoutBtn.setPadding(new Insets(5, 7, 5, 7));
        LogoutBtn.setFont(Font.font("Roboto Rounded MT", 23));
        LogoutBtn.setMaxWidth(Double.MAX_VALUE);
        LogoutBtn.setOnMouseEntered(e -> {
            LogoutBtn.setStyle("-fx-background-color: hsb(5, 100%, 40%); -fx-background-radius: 10;");
        });

        LogoutBtn.setOnMouseExited(e -> {
            LogoutBtn.setStyle("-fx-background-color: transparent;"); // clears inline style, restores stylesheet
        });
        LogoutBtn.setFont(Font.font("Roboto Rounded-MT", FontWeight.BOLD, 23));
        LogoutBtn.setTextFill(Color.hsb(0, 0, .75));


        head.setAlignment(Pos.TOP_CENTER);
        head.getChildren().addAll(prof_ph, name, editProfBtn, LogoutBtn);


        main_block.getChildren().addAll(head);
    }

    private void editProf() {
        main_block.getChildren().clear();

        HBox head = new HBox();
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-left-96.png")));
        Button go_back_btn = new Buttons(null, null, iv, 35).getBtn();
        Label title = new Labels("Edit Profile", Font.font("Roboto Rounded MT", FontWeight.BOLD, 27), Color.hsb(48, 1, .85, 1)).getLabel();

        go_back_btn.setOnAction(e -> Base());

        head.getChildren().addAll(go_back_btn, title);
        head.setAlignment(Pos.CENTER_LEFT);

        HBox email = field("Email", "example@email.com");
        HBox password1 = field("Current Password", null);
        HBox password2 = field("New Password", null);
        HBox password3 = field("Confirm Password", null);

        main_block.getChildren().addAll(head, email, password1, password2, password3);
    }

    public static HBox profileBtn(String txt) {
        HBox retBox = new HBox();
        Label label = new Labels(txt, Font.font("Roboto Rounded MT", FontWeight.BOLD, 23), Color.hsb(0, 0, 0.75, 1)).getLabel();
        retBox.setPadding(new Insets(5, 7, 5, 7));
        retBox.setCursor(Cursor.HAND);

        retBox.setOnMouseEntered(e -> retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .40, 1), new CornerRadii(10), null))));
        retBox.setOnMouseExited(e -> retBox.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, .28, 1), new CornerRadii(10), null))));

        retBox.getChildren().addAll(label);
        return retBox;
    }

    public static HBox field(String txt, String data) {
        HBox retBox = new HBox(10);

        Label label = new Labels(txt, Font.font("Roboto Rounded MT", FontWeight.BOLD, 18), Color.hsb(0, 0, .75)).getLabel();

        TextField tf = new TextField(data);
        tf.setStyle("-fx-text-fill: #fff; -fx-prompt-text-fill: hsb(0, 0, 75%);");
        tf.setPromptText(txt);
        tf.setFont(Font.font("Roboto Rounded-MT", 17));
        tf.setBackground(new Background(new BackgroundFill(Color.hsb(215, .6, .14, 1), new CornerRadii(10), null)));
        tf.setBorder(new Border(new BorderStroke(Color.hsb(215, .85, .83, 1), BorderStrokeStyle.SOLID, new CornerRadii(10), null)));
        HBox.setHgrow(tf, Priority.ALWAYS);

        retBox.getChildren().addAll(label, tf);
        retBox.setAlignment(Pos.CENTER_LEFT);


        return retBox;
    }


    public StackPane getSP() {
        return SP;
    }
}

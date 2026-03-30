package app.db_proj;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class Auth_UI {
    private VBox UI, VB, loginBox, signBox;

    public Auth_UI(boolean login) {
        UI = new VBox();
        VB = new VBox(10);
        loginBox = make_loginBox();
        signBox = make_signupBox();




        UI.setAlignment(Pos.CENTER);
        UI.setFillWidth(false);

        if (login)
            UI.getChildren().add(loginBox);
        else
            UI.getChildren().add(signBox);
    }

    public VBox make_loginBox() {
        VBox vb = Box("Login", 10, Color.hsb(0, 0, .28, 1), 15);

        TextField name = FormField("Name");
        TextField password = FormField("Password");

        Button btn = FormBtn("Login");

        VBox.setMargin(btn, new Insets(7, 0, 0, 0));

        vb.getChildren().addAll(name, password, btn);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    public VBox make_signupBox() {
        VBox vb = Box("Sign Up", 10, Color.hsb(0, 0, .28, 1), 15);
        HBox type_picker = new HBox(0);

        type_picker.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), new CornerRadii(12), null)));
        Button personal_btn = new Button("Personal");
        Button business_btn = new Button("Business");
        personal_btn.setPadding(new Insets(5, 15, 5, 15));
        business_btn.setPadding(new Insets(5, 15, 5, 15));
        personal_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        business_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        personal_btn.setTextFill(Color.BLACK);
        business_btn.setTextFill(Color.WHITE);

        business_btn.setBackground(Background.EMPTY);
//        HBox.setHgrow(personal_btn, Priority.ALWAYS);
        type_picker.setMaxWidth(Region.USE_PREF_SIZE);
        personal_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.7, 1), new CornerRadii(12), null)));


        personal_btn.setCursor(Cursor.HAND);
        business_btn.setCursor(Cursor.HAND);

        type_picker.setAlignment(Pos.CENTER);
        type_picker.getChildren().addAll(personal_btn, business_btn);

        TextField name = FormField("Name");
        TextField email = FormField("Email");
        TextField phone = FormField("Phone, ...");
        TextField password = FormField("Password");
        TextField location = FormField("Street Num-Name, City, zip");
        ArrayList<String> list = new ArrayList<>(List.of("Wholesalers", "Retailers"));
        ObservableList<String> cb_1 = FXCollections.observableList(list);
        ComboBox<String> kind_of_business = new ComboBox<>(cb_1);
        kind_of_business.setPromptText("Kind of business");
        kind_of_business.setMaxWidth(Double.MAX_VALUE);
        kind_of_business.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), new CornerRadii(7), null)));
        kind_of_business.setPrefHeight(35);
        kind_of_business.setStyle("-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: white; " +
                        "-fx-font-size: 16px;");
        Button btn = FormBtn("Sign Up");

        VBox.setMargin(btn, new Insets(7, 0, 0, 0));

        vb.getChildren().addAll(type_picker, name, email, phone, password, location, kind_of_business, btn);
        vb.setAlignment(Pos.CENTER);
        return vb;
    }

    public VBox Box(String title, double gap, Color bc, double rad) {
        VBox vb = new VBox(gap);

        Label title_ = new Label(title);
        title_.setFont(Font.font("Adwaita Mono", FontWeight.EXTRA_BOLD, 25));
        title_.setTextFill(Color.hsb(215, 0.6, 0.14, 1));

        VBox.setMargin(title_, new Insets(0, 0, 7, 0));

        vb.setBackground(new Background(new BackgroundFill(bc, new CornerRadii(rad), null)));
        vb.setPadding(new Insets(7, 15, 15, 15));
        vb.getChildren().add(title_);


        vb.setPrefWidth(350);
        return vb;
    }

    public TextField FormField(String text) {
        TextField tf = new TextField();
        tf.setPromptText(text);
        tf.setFont(Font.font("Roboto", 17));
        tf.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), new CornerRadii(7), null)));
        return tf;
    }
    public Button FormBtn(String text) {
        Button btn = new Button(text);
        btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, .7, 1), new CornerRadii(12), null)));
        btn.setPadding(new Insets(7, 20, 7, 20));
        btn.setFont(Font.font("Roboto", 18));
        btn.setTextFill(Color.BLACK);
        btn.setCursor(Cursor.HAND);
        return btn;
    }

    public VBox getUI() {
        return UI;
    }
}

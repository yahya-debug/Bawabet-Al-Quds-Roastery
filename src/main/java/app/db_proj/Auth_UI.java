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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Auth_UI {
    private VBox UI, vb, loginBox, signBox;
    private SystemHandling sys;

    public Auth_UI(SystemHandling sys) {
        this.sys = sys;
        UI = new VBox();
        loginBox = make_loginBox();
        signBox = make_signupBox();

        UI.setAlignment(Pos.CENTER);
        UI.setFillWidth(false);

    }

    public Auth_UI changePage(boolean login) {
        UI.getChildren().clear();
        if (login) {
            UI.getChildren().add(loginBox);
            sys.getTop().getLogin().setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(17), null)));
            sys.getTop().getSignUp().setBackground(Background.EMPTY);
            sys.getTop().getLogin().setBorder(Border.EMPTY);
        }  else {
            UI.getChildren().add(signBox);
            sys.getTop().getSignUp().setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(17), null)));
            sys.getTop().getLogin().setBackground(Background.EMPTY);
            sys.getTop().getLogin().setBorder(Border.EMPTY);
        }

        return this;
    }

    public VBox make_loginBox() {
        VBox vb = Box("Login", 10, Color.hsb(35, 0.08, 0.46, 1), 15);

        TextField name = FormField("Name");
        TextField email = FormField("Email");
        TextField password = FormField("Password");

        Button btn = FormBtn("Login");

        VBox.setMargin(btn, new Insets(7, 0, 0, 0));

        vb.setAlignment(Pos.CENTER);

        HBox little_switch = new HBox(4);
        Label l1 = new Label("Don't have an account?");
        Label l2 = new Label("Sign Up.");

        l1.setFont(Font.font("Nunito", 16));
        l2.setFont(Font.font("Nunito", 16));
        l1.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        l2.setTextFill(Color.hsb(48, 1, 0.92, 1));
        l2.setCursor(Cursor.HAND);

        little_switch.getChildren().addAll(l1, l2);
        little_switch.setAlignment(Pos.CENTER);

        l2.setOnMouseClicked(e -> changePage(false));

        vb.getChildren().addAll(name, email, password, btn, little_switch);

        loginBox = vb;

        btn.setOnAction(e -> {
            Connection conn = sys.getConn();
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Person WHERE email = '" + email.getText() + "' AND name = '" + name.getText() + "' AND password = '" +  password.getText() + "';");
                System.out.println(rs);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return vb;
    }

    public VBox make_signupBox() {
        vb = Box("Sign Up", 10, Color.hsb(35, 0.08, 0.46, 1), 15);
        HBox type_picker = new HBox(0);
        VBox form = new VBox(10);

        type_picker.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(12), null)));
        Button personal_btn = new Button("Personal");
        Button business_btn = new Button("Business");
        personal_btn.setPadding(new Insets(5, 15, 5, 15));
        business_btn.setPadding(new Insets(5, 15, 5, 15));
        personal_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        business_btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        personal_btn.setTextFill(Color.BLACK);
        business_btn.setTextFill(Color.WHITE);

        type_picker.setMaxWidth(Region.USE_PREF_SIZE);
        personal_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)));
        business_btn.setBackground(Background.EMPTY);


        personal_btn.setCursor(Cursor.HAND);
        business_btn.setCursor(Cursor.HAND);

        type_picker.setAlignment(Pos.CENTER);
        type_picker.getChildren().addAll(personal_btn, business_btn);


        // Form Fields
        TextField name = FormField("Name");
        TextField email = FormField("Email");
        TextField phone = FormField("Phone, ...");
        TextField password = FormField("Password");
        TextField location = FormField("Street Num-Name, City, zip");
        TextField tax_id = FormField("Tax ID");
        TextField reg_number = FormField("Registration Number");


        // ComboBox styling
        ArrayList<String> list = new ArrayList<>(List.of("Wholesalers", "Retailers"));
        ObservableList<String> cb_1 = FXCollections.observableList(list);
        ComboBox<String> kind_of_business = new ComboBox<>(cb_1);
        kind_of_business.setPromptText("Kind of business");
        kind_of_business.setMaxWidth(Double.MAX_VALUE)  ;
        kind_of_business.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(7), null)));
        kind_of_business.setPrefHeight(35);
        kind_of_business.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? kind_of_business.getPromptText() : item);
                setTextFill(Color.hsb(30, 0.12, 0.78, 1));
                setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");
            }
        });

        Button btn = FormBtn("Sign Up");

        // Track which mode is active so the button action knows what to insert
        boolean[] isPersonal = {true};

        // switch account from the registration box
        HBox little_switch = new HBox(4);
        Label l1 = new Label("Already have an account?");
        Label l2 = new Label("Login.");

        l1.setFont(Font.font("Nunito", 16));
        l2.setFont(Font.font("Nunito", 16));
        l1.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
        l2.setTextFill(Color.hsb(48, 1, 0.92, 1));
        l2.setCursor(Cursor.HAND);

        little_switch.getChildren().addAll(l1, l2);
        little_switch.setAlignment(Pos.CENTER);

        l2.setOnMouseClicked(e -> changePage(true));

        // Toggle between business mode or personal
        personal_btn.setOnAction(e -> {
            isPersonal[0] = true;
            UI.getChildren().clear();
            vb = Box("Sign Up", 10, Color.hsb(35, 0.08, 0.46, 1), 15);;
            vb.getChildren().addAll(type_picker, name, email, phone, password, btn, little_switch);
            personal_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)));
            business_btn.setBackground(Background.EMPTY);
            UI.getChildren().add(vb);
            personal_btn.setTextFill(Color.BLACK);
            business_btn.setTextFill(Color.WHITE);
            signBox = vb;
        });

        business_btn.setOnAction(e -> {
            isPersonal[0] = false;
            UI.getChildren().clear();
            vb = Box("Sign Up", 10, Color.hsb(35, 0.08, 0.46, 1), 15);;
            vb.getChildren().addAll(type_picker, name, email, phone, password, location, kind_of_business, tax_id, reg_number, btn, little_switch);
            business_btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)));
            personal_btn.setBackground(Background.EMPTY);
            UI.getChildren().add(vb);
            personal_btn.setTextFill(Color.WHITE);
            business_btn.setTextFill(Color.BLACK);
            signBox = vb;
        });

        VBox.setMargin(btn, new Insets(7, 0, 0, 0));
        vb.getChildren().addAll(type_picker, name, email, phone, password, btn, little_switch);
        vb.setAlignment(Pos.CENTER);
        signBox = vb;

        btn.setOnAction(e -> {
            try {
                Connection conn = sys.getConn();
                Statement stmt = conn.createStatement();
                if (isPersonal[0]) {
                    stmt.addBatch("INSERT INTO Person (person_id, name, email, password) VALUES (" + 1 + ",'" + name.getText() + "','" + email.getText() + "','" + password.getText() + "')");
//                    stmt.addBatch();
                } else
                stmt.executeBatch();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return vb;
    }

    public VBox Box(String title, double gap, Color bc, double rad) {
        vb = new VBox(gap);

        Label title_ = new Label(title);
        title_.setFont(Font.font("Adwaita Mono", FontWeight.EXTRA_BOLD, 25));
        title_.setTextFill(Color.hsb(48, 1, 0.92, 1));

        VBox.setMargin(title_, new Insets(0, 0, 7, 0));

        vb.setBackground(new Background(new BackgroundFill(bc, new CornerRadii(rad), null)));
        vb.setPadding(new Insets(7, 15, 15, 15));
        vb.getChildren().add(title_);


        vb.setAlignment(Pos.CENTER);

        vb.setPrefWidth(400);
        return vb;
    }

    public TextField FormField(String text) {
        TextField tf = new TextField();
        tf.setPromptText(text);
        tf.setFont(Font.font("Nunito", 17));
        tf.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0.25, 1), new CornerRadii(7), null)));
        tf.setStyle("-fx-text-fill: white;");
        return tf;
    }
    public Button FormBtn(String text) {
        Button btn = new Button(text);
        btn.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)));
        btn.setPadding(new Insets(7, 20, 7, 20));
        btn.setFont(Font.font("Nunito", 18));
        btn.setTextFill(Color.BLACK);
        btn.setCursor(Cursor.HAND);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }



    public VBox getUI() {
        return UI;
    }
}

package app.db_proj.UI;

import app.db_proj.Labels;
import app.db_proj.Page;
import app.db_proj.Profile_Logic;
import app.db_proj.SystemHandling;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
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
    private SystemHandling sys;
    private Profile_Logic.CustomerInfo info;

    public ProfileUI(SystemHandling sys) {
        this.sys = sys;

        // pull the current customer info from the database when the profile opens
        // so the screen shows the real name email and account type
        if (sys.getCurrentUserId() != null) {
            info = Profile_Logic.getCustomer(sys.getConn(), sys.getCurrentUserId());
        }

        SP = new StackPane();
        SP.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0, .55), null, null)));
        SP.setOnMouseClicked(e -> sys.hideProf());

        main_block = new VBox(10);
        main_block.setPadding(new Insets(12));
        main_block.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(15), null)));
        main_block.prefWidthProperty().bind(SP.widthProperty().multiply(.3));
        main_block.maxWidthProperty().bind(SP.widthProperty().multiply(.3));
        main_block.setMinWidth(355);
        main_block.setMaxHeight(Region.USE_PREF_SIZE);

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

        // fall back to placeholders when the database has nothing for this user
        String displayName = info != null && info.name != null ? info.name : "Name";
        String displayEmail = info != null && info.email != null ? info.email : "";
        String displayType = info != null && info.type != null ? info.type : "";

        Label name = new Labels(displayName, Font.font("Nunito", FontWeight.BOLD, 27), Color.WHITE).getLabel();
        Label emailLabel = new Labels(displayEmail, Font.font("Nunito", 16), Color.hsb(30, 0.12, 0.78, 1)).getLabel();
        Label typeLabel = new Labels(displayType, Font.font("Nunito", 14), Color.hsb(48, 1, 0.92, 1)).getLabel();

        HBox editProfBtn = profileBtn("Edit Profile");
        editProfBtn.setOnMouseClicked(e -> editProf());
        editProfBtn.setOnMouseEntered(e -> editProfBtn.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(12), null))));
        editProfBtn.setOnMouseExited(e -> editProfBtn.setBackground(Background.EMPTY));


        HBox ordersBtn = profileBtn("My Orders");
        ordersBtn.setOnMouseClicked(e -> { sys.hideProf(); sys.changePage(Page.CART); });
        ordersBtn.setOnMouseEntered(e -> ordersBtn.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(12), null))));
        ordersBtn.setOnMouseExited(e -> ordersBtn.setBackground(Background.EMPTY));


        HBox cartBtn = profileBtn("Cart");
        cartBtn.setOnMouseClicked(e -> { sys.hideProf(); sys.changePage(Page.CART); });
        cartBtn.setOnMouseEntered(e -> cartBtn.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(12), null))));
        cartBtn.setOnMouseExited(e -> cartBtn.setBackground(Background.EMPTY));

        // only build the Admin Panel entry when the logged in person is in the Admin table
        // we keep the variable nullable so the head can decide whether to add it
        HBox adminBtn = null;
        boolean userIsAdmin = sys.getCurrentUserId() != null
                && Profile_Logic.isAdmin(sys.getConn(), sys.getCurrentUserId());
        if (userIsAdmin) {
            adminBtn = profileBtn("Admin Panel");
            HBox finalAdminBtn = adminBtn;
            adminBtn.setOnMouseClicked(e -> { sys.hideProf(); sys.changePage(Page.ADMIN); });
            adminBtn.setOnMouseEntered(e -> finalAdminBtn.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(12), null))));
            adminBtn.setOnMouseExited(e -> finalAdminBtn.setBackground(Background.EMPTY));
        }


        Button LogoutBtn = new Buttons("Log out", null).getBtn();
        LogoutBtn.setOnAction(e -> sys.logout());
        LogoutBtn.setBorder(new Border(new BorderStroke(Color.hsb(5, 1, .4), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));
        LogoutBtn.setPadding(new Insets(5, 7, 5, 7));
        LogoutBtn.setFont(Font.font("Nunito", 23));
        LogoutBtn.setMaxWidth(Double.MAX_VALUE);
        LogoutBtn.setOnMouseEntered(e -> {
            LogoutBtn.setStyle("-fx-background-color: hsb(5, 100%, 40%); -fx-background-radius: 10;");
        });

        LogoutBtn.setOnMouseExited(e -> {
            LogoutBtn.setStyle("-fx-background-color: transparent;"); // clears inline style, restores stylesheet
        });
        LogoutBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 23));
        LogoutBtn.setTextFill(Color.hsb(30, 0.12, 0.78));


        head.setAlignment(Pos.TOP_CENTER);
        head.getChildren().addAll(prof_ph, name, emailLabel, typeLabel, editProfBtn, ordersBtn, cartBtn);
        if (adminBtn != null) head.getChildren().add(adminBtn);
        head.getChildren().add(LogoutBtn);


        main_block.getChildren().addAll(head);
    }

    private void editProf() {
        main_block.getChildren().clear();

        HBox head = new HBox();
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-left-96.png")));
        Button go_back_btn = new Buttons(null, null, iv, 35).getBtn();
        Label title = new Labels("Edit Profile", Font.font("Nunito", FontWeight.BOLD, 27), Color.hsb(48, 1, 0.92, 1)).getLabel();
        go_back_btn.setOnAction(e -> Base());
        go_back_btn.setPadding(new Insets(0));
        head.getChildren().addAll(go_back_btn, title);
        head.setAlignment(Pos.CENTER_LEFT);

        boolean isBusiness = info != null && "business".equalsIgnoreCase(info.type);
        String nameLabel = isBusiness ? "Business Name" : "Name";

        HBox nameBox    = field(nameLabel, info != null && info.name    != null ? info.name    : "");
        HBox emailBox   = field("Email",   info != null && info.email   != null ? info.email   : "");
        HBox phoneBox   = field("Phone",   info != null && info.phone   != null ? info.phone   : "");
        HBox addressBox = field("Address", info != null && info.address != null ? info.address : "");

        TextField nameField    = (TextField) nameBox.getChildren().get(1);
        TextField emailField   = (TextField) emailBox.getChildren().get(1);
        TextField phoneField   = (TextField) phoneBox.getChildren().get(1);
        TextField addressField = (TextField) addressBox.getChildren().get(1);

        Label pwHead = new Labels("Change Password",
            Font.font("Nunito", FontWeight.BOLD, 16), Color.hsb(30, 0.12, 0.60, 1)).getLabel();
        pwHead.setPadding(new Insets(8, 0, 0, 0));

        HBox curPwBox  = field("Current Password", "");
        HBox newPwBox  = field("New Password",     "");
        HBox confPwBox = field("Confirm Password", "");

        TextField curPwField  = (TextField) curPwBox.getChildren().get(1);
        TextField newPwField  = (TextField) newPwBox.getChildren().get(1);
        TextField confPwField = (TextField) confPwBox.getChildren().get(1);

        Label errLabel = new Label();
        errLabel.setFont(Font.font("Nunito", 14));
        errLabel.setTextFill(Color.hsb(5, 0.85, 0.75));
        errLabel.setVisible(false);
        errLabel.setManaged(false);

        Label okLabel = new Label("Saved!");
        okLabel.setFont(Font.font("Nunito", 14));
        okLabel.setTextFill(Color.hsb(120, 0.5, 0.75));
        okLabel.setVisible(false);
        okLabel.setManaged(false);

        Button saveBtn = new Buttons("Save Changes",
            new BackgroundFill(Color.hsb(48, 1, 0.92, 1), new CornerRadii(12), null)).getBtn();
        saveBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 18));
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            errLabel.setVisible(false); errLabel.setManaged(false);
            okLabel.setVisible(false);  okLabel.setManaged(false);

            String res = Profile_Logic.updateProfile(sys.getConn(), sys.getCurrentUserId(),
                nameField.getText(), emailField.getText(), phoneField.getText());
            if (res.equals("empty")) {
                errLabel.setText("Name and email cannot be empty");
                errLabel.setVisible(true); errLabel.setManaged(true);
                return;
            } else if (res.equals("duplicate")) {
                errLabel.setText("Email already in use by another account");
                errLabel.setVisible(true); errLabel.setManaged(true);
                return;
            } else if (!res.equals("ok")) {
                errLabel.setText("Failed to save changes");
                errLabel.setVisible(true); errLabel.setManaged(true);
                return;
            }

            Profile_Logic.updateAddress(sys.getConn(), sys.getCurrentUserId(), addressField.getText());

            if (!curPwField.getText().isBlank()) {
                String pwRes = Profile_Logic.updatePassword(sys.getConn(), sys.getCurrentUserId(),
                    curPwField.getText(), newPwField.getText(), confPwField.getText());
                if (pwRes.equals("empty")) {
                    errLabel.setText("Fill in all three password fields");
                    errLabel.setVisible(true); errLabel.setManaged(true);
                    return;
                } else if (pwRes.equals("wrong_password")) {
                    errLabel.setText("Current password is incorrect");
                    errLabel.setVisible(true); errLabel.setManaged(true);
                    return;
                } else if (pwRes.equals("mismatch")) {
                    errLabel.setText("New passwords do not match");
                    errLabel.setVisible(true); errLabel.setManaged(true);
                    return;
                }
            }

            info = Profile_Logic.getCustomer(sys.getConn(), sys.getCurrentUserId());
            sys.setCurrentUser(sys.getCurrentUserId(),
                info != null ? info.name  : sys.getCurrentUserName(),
                info != null ? info.email : sys.getCurrentUserEmail());
            okLabel.setVisible(true); okLabel.setManaged(true);
        });

        main_block.getChildren().addAll(
            head, nameBox, emailBox, phoneBox, addressBox,
            pwHead, curPwBox, newPwBox, confPwBox,
            errLabel, okLabel, saveBtn
        );
    }

    public static HBox profileBtn(String txt) {
        HBox retBox = new HBox();
        Label label = new Labels(txt, Font.font("Nunito", FontWeight.BOLD, 23), Color.hsb(30, 0.12, 0.78, 1)).getLabel();
        retBox.setPadding(new Insets(5, 7, 5, 7));
        retBox.setCursor(Cursor.HAND);

        retBox.setOnMouseEntered(e -> retBox.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.15, 0.34, 1), new CornerRadii(10), null))));
        retBox.setOnMouseExited(e -> retBox.setBackground(Background.EMPTY));

        retBox.getChildren().addAll(label);
        return retBox;
    }

    public static HBox field(String txt, String data) {
        HBox retBox = new HBox(10);

        Label label = new Labels(txt, Font.font("Nunito", FontWeight.BOLD, 18), Color.hsb(30, 0.12, 0.78)).getLabel();

        TextField tf = new TextField(data);
        tf.setStyle("-fx-control-inner-background: #1a1a1a; -fx-text-fill: #d4c0a0; -fx-prompt-text-fill: #5a4a38; -fx-background-radius: 10; -fx-font-size: 17px;");
        tf.setPromptText(txt);
        tf.setFont(Font.font("Nunito", 17));
        HBox.setHgrow(tf, Priority.ALWAYS);

        retBox.getChildren().addAll(label, tf);
        retBox.setAlignment(Pos.CENTER_LEFT);


        return retBox;
    }


    public StackPane getSP() {
        return SP;
    }
}

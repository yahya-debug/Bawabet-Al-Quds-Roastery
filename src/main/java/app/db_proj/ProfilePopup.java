package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProfilePopup {
    private StackPane overlay;

    public ProfilePopup(SystemHandling sys) {
        // Semi-transparent dark overlay — clicking it dismisses the popup
        overlay = new StackPane();
        overlay.setBackground(new Background(new BackgroundFill(Color.hsb(0, 0, 0, 0.55), null, null)));
        overlay.setOnMouseClicked(e -> sys.hideProfilePopup());

        // Profile card
        VBox card = new VBox(14);
        card.setMaxWidth(320);
        card.setPadding(new Insets(24, 28, 28, 28));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.20, 1), new CornerRadii(18), null)));
        card.setStyle("-fx-border-color: rgba(255,185,0,0.25); -fx-border-width: 1.5; -fx-border-radius: 18;");

        // Clicking inside the card does NOT dismiss the popup
        card.setOnMouseClicked(e -> e.consume());

        // Close button row
        Button closeBtn = new Button("×");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        closeBtn.setTextFill(Color.hsb(0, 0, 0.55, 1));
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setPadding(new Insets(0, 2, 0, 2));
        closeBtn.setOnAction(e -> sys.hideProfilePopup());

        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.TOP_RIGHT);

        // Avatar
        ImageView avatarImg = new ImageView(new Image(getClass().getResourceAsStream("/app/db_proj/icons8-profile-96.png")));
        avatarImg.setFitWidth(68);
        avatarImg.setFitHeight(68);
        StackPane avatar = new StackPane(avatarImg);
        avatar.setMaxWidth(76);
        avatar.setMaxHeight(76);
        avatar.setPadding(new Insets(4));
        avatar.setBackground(new Background(new BackgroundFill(Color.hsb(48, 1, 0.85, 0.12), new CornerRadii(50), null)));

        // Name label
        Label nameLbl = new Label("Guest User");
        nameLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        nameLbl.setTextFill(Color.hsb(48, 1, 0.85, 1));

        // Email label
        Label emailLbl = new Label("user@example.com");
        emailLbl.setFont(Font.font("Adwaita Mono", 13));
        emailLbl.setTextFill(Color.hsb(0, 0, 0.55, 1));

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,185,0,0.2);");
        VBox.setMargin(sep, new Insets(4, 0, 4, 0));

        // Menu items
        Button ordersBtn  = menuButton("My Orders");
        Button editBtn    = menuButton("Edit Profile");
        Button logoutBtn  = menuButton("Logout");
        logoutBtn.setTextFill(Color.hsb(5, 0.65, 0.85, 1));

        card.getChildren().addAll(closeRow, avatar, nameLbl, emailLbl, sep, ordersBtn, editBtn, logoutBtn);

        overlay.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    private Button menuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(11, 18, 11, 18));
        btn.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 15));
        btn.setTextFill(Color.hsb(0, 0, 0.85, 1));
        btn.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.28, 1), new CornerRadii(10), null)));
        btn.setCursor(Cursor.HAND);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    public StackPane getOverlay() {
        return overlay;
    }
}

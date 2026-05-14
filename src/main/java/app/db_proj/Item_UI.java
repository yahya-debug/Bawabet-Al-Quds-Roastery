package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Item_UI {

    /** Vertical product card used in the Home catalogue grid. */
    public static VBox makeProductCard(String itemName, double price, String type, Image itemImage) {
        VBox card = new VBox(8);
        card.setPrefWidth(210);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.38, 1), new CornerRadii(12), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null))));

        ImageView img = new ImageView(itemImage);
        img.setFitWidth(90);
        img.setFitHeight(90);
        img.setPreserveRatio(true);

        Label typeLabel = new Label(type.toUpperCase());
        typeLabel.setFont(Font.font("Nunito", 12));
        typeLabel.setTextFill(Color.hsb(30, 0.12, 0.78, 1));

        Label nameLabel = new Label(itemName);
        nameLabel.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(182);
        nameLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(String.format("₪ %.2f", price));
        priceLabel.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.hsb(48, 1, 0.92, 1));

        card.getChildren().addAll(img, typeLabel, nameLabel, priceLabel);
        return card;
    }

    public static HBox makeItemCard(String itemName, double price, Image itemImage) {
        HBox card = new HBox(15);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));

        // Item image
        ImageView img = new ImageView(itemImage);
        img.setFitWidth(70);
        img.setFitHeight(70);
        img.setPreserveRatio(true);

        // Name + price
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(itemName);
        name.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 17));
        name.setTextFill(Color.WHITE);

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setFont(Font.font("Nunito", 16));
        priceLabel.setTextFill(Color.hsb(48, 1, 0.92, 1)); // gold

        info.getChildren().addAll(name, priceLabel);

        // Remove button
        ImageView trashIcon = new ImageView(new Image(Item_UI.class.getResourceAsStream("/app/db_proj/icons8-trash-96.png")));


        Button removeBtn = new Buttons(null, null, trashIcon, 28).getBtn();
        removeBtn.setPadding(new Insets(8));
        removeBtn.setOnAction(e -> {
            VBox parent = (VBox) card.getParent();
            parent.getChildren().remove(card);
        });

        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.38, 1), new CornerRadii(12), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null))));

        card.getChildren().addAll(img, info, removeBtn);
        return card;
    }

    public static HBox makeOrderCard(String itemName, double price, Image itemImage) {
        HBox card = new HBox(15);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(5));
        card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null)));
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.38, 1), new CornerRadii(12), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.08, 0.46, 1), new CornerRadii(12), null))));

        // Item image
        ImageView img = new ImageView(itemImage);
        img.setFitWidth(70);
        img.setFitHeight(70);
        img.setPreserveRatio(true);

        // Name + price
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(itemName);
        name.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 17));
        name.setTextFill(Color.WHITE);

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setFont(Font.font("Nunito", 16));
        priceLabel.setTextFill(Color.hsb(48, 1, 0.92, 1)); // gold

        info.getChildren().addAll(name, priceLabel);

        card.getChildren().addAll(img, info, priceLabel);
        return card;
    }
}

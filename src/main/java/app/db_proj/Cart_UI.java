package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Cart_UI {
    private BorderPane root;
    private SystemHandling sys;

    public Cart_UI(SystemHandling sys) {
        this.sys = sys;
        build();
    }

    private void build() {
        root = new BorderPane();
        root.setBackground(Background.EMPTY);

        // ── Left: scrollable cart item list ──────────────────────────────────
        VBox itemList = new VBox(12);
        itemList.setPadding(new Insets(15, 15, 15, 15));

        // placeholder cart items (replace with real data later)
        for (int i = 0; i < 6; i++) {
            itemList.getChildren().add(makeCartItemCard());
        }

        ScrollPane scrollPane = new ScrollPane(itemList);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        // ── Right: fixed order-summary sidebar ───────────────────────────────
        VBox sidebar = makeSidebar();
        sidebar.setPrefWidth(300);
        sidebar.setMinWidth(300);
        sidebar.setMaxWidth(300);

        // ── Outer content row ────────────────────────────────────────────────
        HBox contentRow = new HBox(15, scrollPane, sidebar);
        contentRow.setPadding(new Insets(15));
        // Make the sidebar fill the same height as the HBox
        sidebar.setMaxHeight(Double.MAX_VALUE);

        root.setCenter(contentRow);
    }

    private HBox makeCartItemCard() {
        HBox card = new HBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(
                Color.hsb(0, 0, 0.28, 1), new CornerRadii(12), null)));
        card.setMinHeight(90);
        return card;
    }

    private VBox makeSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.hsb(0, 0, 0.28, 1), new CornerRadii(12), null)));
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Order Summary");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 20));
        title.setTextFill(Color.hsb(48, 1, 0.85, 1));

        Label totalLabel = new Label("Total: $0.00");
        totalLabel.setFont(Font.font("Roboto", 18));
        totalLabel.setTextFill(Color.hsb(0, 0, 0.75, 1));

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setBackground(new Background(new BackgroundFill(
                Color.hsb(48, 1, 0.85, 1), new CornerRadii(12), null)));
        checkoutBtn.setPadding(new Insets(10, 20, 10, 20));
        checkoutBtn.setFont(Font.font("Roboto", 18));
        checkoutBtn.setTextFill(Color.BLACK);
        checkoutBtn.setCursor(Cursor.HAND);

        sidebar.getChildren().addAll(title, totalLabel, checkoutBtn);
        return sidebar;
    }

    public BorderPane getRoot() {
        return root;
    }
}

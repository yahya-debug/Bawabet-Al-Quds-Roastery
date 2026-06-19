package app.db_proj.UI;

import app.db_proj.OrderDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Order;
import app.db_proj.model.OrderItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class CartOrdersUI {

    public static VBox build(SystemHandling sys) {
        VBox box = new VBox(8);

        Integer uid = sys.getCurrentUserId();
        if (uid == null) return box;

        List<Order> orders = OrderDAO.getOrdersForPerson(sys.getConn(), uid);
        if (orders.isEmpty()) {
            Label none = new Label("No orders yet");
            none.setFont(Font.font("Nunito", 15));
            none.setTextFill(Color.hsb(30, 0.12, 0.65, 1));
            box.getChildren().add(none);
            return box;
        }

        for (Order o : orders)
            box.getChildren().add(makeOrderCard(o, sys));

        return box;
    }

    private static HBox makeOrderCard(Order o, SystemHandling sys) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.12, 0.30, 1), new CornerRadii(10), null)));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseEntered(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.14, 0.38, 1), new CornerRadii(10), null))));
        card.setOnMouseExited(e -> card.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.12, 0.30, 1), new CornerRadii(10), null))));

        Region accent = new Region();
        accent.setPrefWidth(4);
        accent.setMinHeight(42);
        Color accentColor = statusColor(o.status);
        accent.setBackground(new Background(new BackgroundFill(accentColor, new CornerRadii(2), null)));

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label idLbl = new Label("Order #" + o.orderId);
        idLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 14));
        idLbl.setTextFill(Color.WHITE);

        String dateStr = o.orderDate.toLocalDate().toString();
        Label dateLbl = new Label(dateStr + "  ·  " + o.items.size() + " item(s)");
        dateLbl.setFont(Font.font("Nunito", 13));
        dateLbl.setTextFill(Color.hsb(30, 0.12, 0.65, 1));

        info.getChildren().addAll(idLbl, dateLbl);

        Label totalLbl = new Label(String.format("₪ %.2f", o.total));
        totalLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        totalLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));

        card.getChildren().addAll(accent, info, totalLbl);
        card.setOnMouseClicked(e -> showOrderDetail(o, sys));
        return card;
    }

    private static void showOrderDetail(Order o, SystemHandling sys) {
        StackPane parent = sys.getScreen();

        StackPane overlay = new StackPane();
        overlay.setBackground(new Background(new BackgroundFill(
            Color.hsb(0, 0, 0, 0.70), null, null)));
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) parent.getChildren().remove(overlay);
        });

        VBox modal = new VBox(0);
        modal.setMaxWidth(480);
        modal.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.20, 0.22, 1), new CornerRadii(16), null)));
        modal.setEffect(new DropShadow(28, 0, 8, Color.hsb(0, 0, 0, 0.60)));
        StackPane.setMargin(modal, new Insets(40));

        // header
        HBox header = new HBox();
        header.setPadding(new Insets(14, 18, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
            Color.hsb(35, 0.22, 0.17, 1), new CornerRadii(16, 16, 0, 0, false), null)));

        Label titleLbl = new Label("Order #" + o.orderId);
        titleLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font("Nunito", FontWeight.BOLD, 16));
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> parent.getChildren().remove(overlay));
        closeBtn.setOnMouseEntered(e -> closeBtn.setTextFill(Color.WHITE));
        closeBtn.setOnMouseExited(e -> closeBtn.setTextFill(Color.hsb(30, 0.12, 0.72, 1)));
        header.getChildren().addAll(titleLbl, spacer, closeBtn);

        // body
        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 18, 18, 18));

        String dateStr = o.orderDate.toLocalDate().toString();
        Label dateLbl = new Label("Placed on " + dateStr);
        dateLbl.setFont(Font.font("Nunito", 13));
        dateLbl.setTextFill(Color.hsb(30, 0.12, 0.65, 1));

        Label statusLbl = new Label(o.status);
        statusLbl.setFont(Font.font("Nunito", FontWeight.BOLD, 13));
        statusLbl.setTextFill(statusColor(o.status));
        statusLbl.setPadding(new Insets(2, 10, 2, 10));
        statusLbl.setBackground(new Background(new BackgroundFill(
            statusColor(o.status).deriveColor(0, 1, 1, 0.20), new CornerRadii(6), null)));

        HBox meta = new HBox(10, dateLbl, statusLbl);
        meta.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().add(meta);

        body.getChildren().add(new Separator());

        Label itemsHead = new Label("Items");
        itemsHead.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
        itemsHead.setTextFill(Color.hsb(30, 0.12, 0.72, 1));
        body.getChildren().add(itemsHead);

        for (OrderItem item : o.items) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setBackground(new Background(new BackgroundFill(
                Color.hsb(35, 0.10, 0.28, 1), new CornerRadii(8), null)));

            Label n = new Label(item.itemName);
            n.setFont(Font.font("Nunito", FontWeight.BOLD, 14));
            n.setTextFill(Color.WHITE);
            HBox.setHgrow(n, Priority.ALWAYS);

            Label qty = new Label("×" + item.quantity);
            qty.setFont(Font.font("Nunito", 14));
            qty.setTextFill(Color.hsb(48, 1, 0.92, 1));

            Label price = new Label(String.format("₪ %.2f", item.subtotal()));
            price.setFont(Font.font("Nunito", 14));
            price.setTextFill(Color.hsb(30, 0.12, 0.72, 1));

            row.getChildren().addAll(n, qty, price);
            body.getChildren().add(row);
        }

        body.getChildren().add(new Separator());

        Label totalLbl = new Label(String.format("Total:  ₪ %.2f", o.total));
        totalLbl.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 16));
        totalLbl.setTextFill(Color.hsb(48, 1, 0.92, 1));
        body.getChildren().add(totalLbl);

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxHeight(520);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        modal.getChildren().addAll(header, scroll);
        overlay.getChildren().add(modal);
        parent.getChildren().add(overlay);
    }

    private static Color statusColor(String status) {
        if (status == null) return Color.hsb(30, 0.12, 0.65, 1);
        return switch (status.toLowerCase()) {
            case "pending"    -> Color.hsb(48, 0.9, 0.88, 1);
            case "processing" -> Color.hsb(200, 0.7, 0.80, 1);
            case "shipped"    -> Color.hsb(220, 0.6, 0.82, 1);
            case "delivered"  -> Color.hsb(120, 0.6, 0.72, 1);
            case "cancelled"  -> Color.hsb(0, 0.7, 0.78, 1);
            default           -> Color.hsb(30, 0.12, 0.65, 1);
        };
    }
}

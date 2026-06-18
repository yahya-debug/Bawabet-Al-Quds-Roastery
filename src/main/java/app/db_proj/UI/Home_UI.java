package app.db_proj.UI;

import app.db_proj.ItemDAO;
import app.db_proj.SystemHandling;
import app.db_proj.model.Item;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.util.List;

public class Home_UI {
    private ScrollPane SP;
    private FlowPane grid;
    private SystemHandling sys;
    private Image placeholder;

    public Home_UI(SystemHandling sys) {
        this.sys = sys;
        placeholder = new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));

        grid = new FlowPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setPadding(new Insets(20));

        VBox home = new VBox(10);
        home.getChildren().add(grid);
        home.setPadding(new Insets(10));

        SP = new ScrollPane(home);
        SP.setFitToWidth(true);
        SP.setFitToHeight(false);
        SP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        SP.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadItems("");
    }

    public void search(String query) {
        loadItems(query == null ? "" : query);
    }

    private void loadItems(String query) {
        grid.getChildren().clear();

        List<Item> items = query.isBlank()
            ? ItemDAO.getAll(sys.getConn())
            : ItemDAO.search(sys.getConn(), query);

        if (items.isEmpty()) {
            Label empty = new Label("No items found");
            empty.setFont(Font.font("Nunito", 18));
            empty.setTextFill(Color.hsb(30, 0.12, 0.78, 1));
            grid.getChildren().add(empty);
            return;
        }

        for (Item item : items) {
            Image img = loadImage(item.imagePath);
            VBox card = Item_UI.makeProductCard(item.name, item.price, item.itemType, img);
            card.setOnMouseClicked(e -> sys.openItemDetail(item));
            grid.getChildren().add(card);
        }
    }

    private Image loadImage(String path) {
        if (path != null && !path.isBlank()) {
            try { return new Image(new File(path).toURI().toString()); }
            catch (Exception ignored) {}
        }
        return placeholder;
    }

    public ScrollPane getSP() {
        return SP;
    }
}

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Home_UI {
    private ScrollPane SP;
    private FlowPane grid;
    private SystemHandling sys;
    private HBox packageBox;
    private VBox home;
    private Image placeholder;

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

        // Pre-load all items from the database on startup
        loadItems("", "All");
    }

    // ── Database retrieval (Functionality 2) ──────────────────────────────────

    public void loadItems(String keyword, String category) {
        grid.getChildren().clear();

        if (placeholder == null) {
            placeholder = new Image(getClass().getResourceAsStream("/app/db_proj/Logo.jpg"));
        }

        try {
            Connection conn = DBConnection.getConnection();

            StringBuilder sql = new StringBuilder(
                "SELECT item_id, name, price, item_type FROM Item WHERE 1=1");
            if (!keyword.isEmpty())         sql.append(" AND name LIKE ?");
            if (!category.equals("All"))    sql.append(" AND item_type = ?");
            sql.append(" ORDER BY item_type, name");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (!keyword.isEmpty())       { ps.setString(idx, "%" + keyword + "%"); idx++; }
            if (!category.equals("All"))    ps.setString(idx, category.toLowerCase());

            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                String name  = rs.getString("name");
                double price = rs.getDouble("price");
                String type  = rs.getString("item_type");
                grid.getChildren().add(Item_UI.makeProductCard(name, price, type, placeholder));
            }

            if (!any) {
                Label noResults = new Labels(
                    "No products found.", Font.font("Nunito", 20),
                    Color.hsb(30, 0.12, 0.78, 1)).getLabel();
                grid.getChildren().add(noResults);
            }

        } catch (SQLException e) {
            Label err = new Labels(
                "Could not connect to database. Run schema.sql and test_data.sql first.",
                Font.font("Nunito", 15), Color.web("#e57373")).getLabel();
            err.setWrapText(true);
            grid.getChildren().add(err);
            e.printStackTrace();
        }
    }

    public ScrollPane getSP() {
        return SP;
    }
}

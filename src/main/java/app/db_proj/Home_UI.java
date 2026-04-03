package app.db_proj;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

public class Home_UI {
    private ScrollPane SP;
    private FlowPane grid;
    private SystemHandling sys;

    public Home_UI(SystemHandling sys) {
        this.sys = sys;
        FlowPane grid = new FlowPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(15));
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

//        for (Item item : items) {
//            grid.getChildren().add(makeCard(item));
//        }

    }

    public ScrollPane getSP() {
        return SP;








        
        
        
        
        


    }
}

package app.db_proj;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class SystemHandling {
    private boolean authenticated;
    private HashMap<String, String> Session; // here we will store our session (authentication, user, text in text fields data)
    private BorderPane root;
    private StackPane stackRoot;
    private Navigation_Bar Top;
    private Auth_UI auth_page;
    private Home_UI home_page;
    private Page curPage;

    public Page getCurPage() {
        return curPage;
    }

    public SystemHandling() {
        this.authenticated = true;
        curPage = Page.HOME;

        root = new BorderPane();
        Top = new Navigation_Bar(this);
        auth_page = new Auth_UI(this);
        home_page = new Home_UI(this);

        // work on root
        root.setTop(Top.getTop());
        root.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), null, null)));

        stackRoot = new StackPane(root);
    }

    public void changePage(Page page) {
        switch (page) {
            case Login -> root.setCenter(auth_page.changePage(true).getUI());
            case SignUp -> root.setCenter(auth_page.changePage(false).getUI());
            case HOME -> root.setCenter(home_page.getSP());
        }
        curPage = page;
    }

    /** Show a floating overlay panel on top of all content. */
    public void showOverlay(Node content) {
        hideOverlay(); // remove any existing overlay first

        // Semi-transparent full-screen backdrop — clicking it closes the overlay
        Region backdrop = new Region();
        backdrop.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.45), null, null)));
        backdrop.setOnMouseClicked(e -> hideOverlay());

        // Position the content panel at the top-right, just below the nav bar
        StackPane overlay = new StackPane(backdrop, content);
        StackPane.setAlignment(content, Pos.TOP_RIGHT);
        StackPane.setMargin(content, new Insets(85, 15, 0, 0));
        overlay.setId("overlay");

        stackRoot.getChildren().add(overlay);
    }

    /** Remove the overlay if one is currently shown. */
    public void hideOverlay() {
        stackRoot.getChildren().removeIf(n -> "overlay".equals(n.getId()));
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public StackPane getRoot() {
        return stackRoot;
    }

    public HashMap<String, String> getSession() {
        return Session;
    }

    public Navigation_Bar getTop() {
        return Top;
    }

    public Auth_UI getAuth_page() {
        return auth_page;
    }
}

// Class defines values that will be used in switching pages
enum Page {
    INTRO("Welcome"),
    HOME("Dashboard"),
    Login("Login"),
    SignUp("SignUp"),
    CART("Your Basket"),
    ADMIN("Admin Panel"),
    ITEM_PAGE("Product Details");

    private final String displayName;

    // Constructor (automatically private)
    Page(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
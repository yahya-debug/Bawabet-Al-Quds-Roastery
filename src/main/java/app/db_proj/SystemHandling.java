package app.db_proj;

import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;

public class SystemHandling {
    private boolean authenticated;
    private HashMap<String, String> Session; // here we will store our session (authentication, user, text in text fields data)
    private BorderPane root;
    private Navigation_Bar Top;
    private Auth_UI auth_page;
    private Home_UI home_page;
    private CartUI cart_page;
    private ProfileUI profile_page;
    private Page curPage;
    private StackPane screen;

    public Page getCurPage() {
        return curPage;
    }

    public SystemHandling() {
        this.authenticated = true;
        curPage = Page.HOME;

        root = new BorderPane();
        screen = new StackPane(root);
        Top = new Navigation_Bar(this);
        auth_page = new Auth_UI(this);
        home_page = new Home_UI(this);
        cart_page = new CartUI(this);

        // work on root
        root.setTop(Top.getTop());
        root.setBackground(new Background(new BackgroundFill(Color.web("#F0E8CC"), null, null)));

    }

    public void changePage(Page page) {
        switch (page) {
            case Login -> root.setCenter(auth_page.changePage(true).getUI());
            case SignUp -> root.setCenter(auth_page.changePage(false).getUI());
            case HOME -> root.setCenter(home_page.getSP());
            case CART -> {
                root.setCenter(cart_page.getScreen());
                if (!curPage.equals(Page.CART)) {
                    Label text = new Label("Cart");
                    text.setTextFill(Color.hsb(48, 1, 0.92, 1));
                    text.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 30));
                    Top.getLeft().getChildren().add(text);
                    Top.getTop().setCenter(null);
                }
            }
            case PROFILE -> openProf();
        }

        // since the profile is a pop-up like screen; we dont need to consider it as a separated page
        if (!page.equals(Page.PROFILE)) curPage = page;

    }

    public void openProf() {
        profile_page = new ProfileUI(this);
        screen.getChildren().add(profile_page.getSP());
    }
    public void hideProf() {
        screen.getChildren().remove(screen.getChildren().size() - 1);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public BorderPane getRoot() {
        return root;
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

    public StackPane getScreen() {
        return screen;
    }
}


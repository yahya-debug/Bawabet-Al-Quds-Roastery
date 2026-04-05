package app.db_proj;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class SystemHandling {
    private boolean authenticated;
    private HashMap<String, String> Session; // here we will store our session (authentication, user, text in text fields data)
    private BorderPane root;
    private Navigation_Bar Top;
    private Auth_UI auth_page;
    private Home_UI home_page;
    private CartUI cart_page;
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
        cart_page = new CartUI(this);

        // work on root
        root.setTop(Top.getTop());
        root.setBackground(new Background(new BackgroundFill(Color.hsb(215, 0.6, 0.14, 1), null, null)));

    }

    public void changePage(Page page) {
        switch (page) {
            case Login -> root.setCenter(auth_page.changePage(true).getUI());
            case SignUp -> root.setCenter(auth_page.changePage(false).getUI());
            case HOME -> root.setCenter(home_page.getSP());
            case CART -> root.setCenter(cart_page.getScreen());
        }
        curPage = page;

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

}


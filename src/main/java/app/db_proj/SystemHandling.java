package app.db_proj;

import app.db_proj.UI.*;
import app.db_proj.model.Item;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
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
    private Admin_UI admin_page;
    private Page curPage;
    private StackPane screen;
    private Connection conn;

    // the person id name and email of the currently signed in user
    // filled in by Auth_Logic.login after a successful match
    private Integer currentUserId;
    private String currentUserName;
    private String currentUserEmail;
    private boolean userIsAdmin;
    private boolean userIsEmployee;
    private int selectedBranchId = -1; // -1 = All branches



    public SystemHandling() {
        this.conn = DBConnection.getConnection();
        this.authenticated = false;
        curPage = Page.HOME;

        root = new BorderPane();
        screen = new StackPane(root);
        Top = new Navigation_Bar(this);
        auth_page = new Auth_UI(this);
        home_page = new Home_UI(this);
        cart_page = new CartUI(this);

        Cart_Logic.ensureCartTable(conn);
        Admin_Logic.ensureItemImageColumn(conn);
        Admin_Logic.ensureSupplierTable(conn); // also calls ensureDefaultSupplier internally
        WarehouseDAO.ensureTables(conn);

        // wire the nav-bar search to the home page item filter
        Top.setSearchAction(q -> home_page.search(q));

        // work on root
        root.setTop(Top.getTop());
        root.setBackground(new Background(new BackgroundFill(Color.web("#EDE0B8"), null, null)));

        changePage(curPage);
    }

    public void changePage(Page page) {
        switch (page) {
            case Login -> root.setCenter(auth_page.changePage(true).getUI());
            case SignUp -> root.setCenter(auth_page.changePage(false).getUI());
            case HOME -> { home_page.refresh(); root.setCenter(home_page.getSP()); }
            case CART -> {
                cart_page.refresh();
                root.setCenter(cart_page.getScreen());
                Top.getLeft().getChildren().removeIf(n -> n instanceof Label);
                Label text = new Label("Cart");
                text.setTextFill(Color.hsb(48, 1, 0.92, 1));
                text.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 30));
                Top.getLeft().getChildren().add(text);
                Top.getTop().setCenter(null);
            }
            case PROFILE -> openProf();
            case ADMIN -> {
                System.out.println("admin");
                if (admin_page == null) admin_page = new Admin_UI(this);
                root.setCenter(admin_page.getScreen());
            }
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

    public void openItemDetail(Item item) {
        screen.getChildren().add(new ItemDetailUI(this, item).getOverlay());
    }

    public void hideItemDetail() {
        if (screen.getChildren().size() > 1)
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
    public Page getCurPage() {
        return curPage;
    }

    public Connection getConn() {
        return conn;
    }

    // store the signed in user and check if they are an admin
    // also asks the navigation bar to rebuild itself for the correct role
    public void setCurrentUser(Integer id, String name, String email) {
        System.out.println("H");
        this.currentUserId = id;
        this.currentUserName = name;
        this.currentUserEmail = email;
        this.authenticated = true;
        System.out.println(Profile_Logic.isAdmin(conn, id));
        this.userIsAdmin = Profile_Logic.isAdmin(conn, id);
        this.userIsEmployee = Admin_Logic.getEmployeeBranchId(conn, id) != -1;
        if (Top != null) Top.refreshAuth();
    }

    public boolean isUserAdmin() {
        return userIsAdmin;
    }

    public boolean isUserEmployee() {
        return userIsEmployee;
    }

    public boolean canAccessPanel() {
        return userIsAdmin || userIsEmployee;
    }

    public void logout() {
        authenticated = false;
        currentUserId = null;
        currentUserName = null;
        currentUserEmail = null;
        userIsAdmin = false;
        userIsEmployee = false;
        admin_page = null;
        // only close the profile overlay if it's actually open
        if (screen.getChildren().size() > 1) hideProf();
        Top.getLeft().getChildren().removeIf(n -> n instanceof Label);
        Top.refreshAuth();
        changePage(Page.HOME);
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public int getSelectedBranchId() {
        return selectedBranchId;
    }

    public void setSelectedBranchId(int id) {
        this.selectedBranchId = id;
    }
}


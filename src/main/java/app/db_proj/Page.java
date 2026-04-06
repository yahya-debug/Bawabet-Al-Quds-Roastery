package app.db_proj;

public enum Page {
    INTRO("Welcome"),
    HOME("Dashboard"),
    Login("Login"),
    SignUp("SignUp"),
    CART("Cart"),
    PROFILE("Profile"),
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

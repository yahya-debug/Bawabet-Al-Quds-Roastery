package app.db_proj.model;

public class Supplier {
    public final int supplierId;
    public final String name;
    public final String email;
    public final String phone;

    public Supplier(int supplierId, String name, String email, String phone) {
        this.supplierId = supplierId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}

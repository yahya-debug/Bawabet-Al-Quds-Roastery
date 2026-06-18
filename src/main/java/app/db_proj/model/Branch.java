package app.db_proj.model;

public class Branch {
    public final int branchId;
    public final String branchName;
    public final String street;
    public final String city;
    public final String zip;

    public Branch(int branchId, String branchName, String street, String city, String zip) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.street = street;
        this.city = city;
        this.zip = zip;
    }
}

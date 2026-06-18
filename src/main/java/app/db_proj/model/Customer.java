package app.db_proj.model;

public class Customer extends Person {
    public final String type;          // "individual" or "business"
    public final String street;
    public final String city;
    public final String zip;

    // Business-only fields (null for individual)
    public final String registrationNumber;
    public final String taxId;
    public final String businessType;

    public Customer(int personId, String name, String email,
                    String type, String street, String city, String zip,
                    String registrationNumber, String taxId, String businessType) {
        super(personId, name, email);
        this.type = type;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.registrationNumber = registrationNumber;
        this.taxId = taxId;
        this.businessType = businessType;
    }

    public boolean isBusiness() {
        return "business".equalsIgnoreCase(type);
    }
}

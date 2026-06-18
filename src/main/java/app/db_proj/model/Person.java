package app.db_proj.model;

public class Person {
    public final int personId;
    public final String name;
    public final String email;

    public Person(int personId, String name, String email) {
        this.personId = personId;
        this.name = name;
        this.email = email;
    }
}

package app.db_proj.model;

import java.time.LocalDate;

public class Employee extends Person {
    public final String role;
    public final double salary;
    public final LocalDate hireDate;
    public final int branchId;
    public final String branchName;

    public Employee(int personId, String name, String email,
                    String role, double salary, LocalDate hireDate,
                    int branchId, String branchName) {
        super(personId, name, email);
        this.role = role;
        this.salary = salary;
        this.hireDate = hireDate;
        this.branchId = branchId;
        this.branchName = branchName;
    }
}

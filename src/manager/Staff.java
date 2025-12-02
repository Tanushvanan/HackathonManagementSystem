package manager;

public class Staff {
    protected int staffID;
    protected Name name;
    protected String role;

    public Staff(int staffID, Name name, String role) {
        this.staffID = staffID;
        this.name = name;
        this.role = role;
    }

    public void login() {}
    public void logout() {}

    public int getStaffID() { return staffID; }
    public Name getName() { return name; }
    public String getRole() { return role; }
}

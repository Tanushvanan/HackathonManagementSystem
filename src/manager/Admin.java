package manager;

    public class Admin extends Staff {
    public Admin(int staffID, Name name) {
        super(staffID, name, "Admin");
    }

    public void manageCategories() {}
    public void assignJudges() {}
    public void generateReports() {}  // stub, since you're ignoring Report class
}

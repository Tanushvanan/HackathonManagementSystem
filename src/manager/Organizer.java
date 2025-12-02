package manager;

public class Organizer extends Staff {

    public Organizer(int staffID, Name name) {
        super(staffID, name, "Organizer"); // role is fixed
    }

    public void requestResults() {}
    public void manageCategories() {}
    public void assignJudges() {}
    public void generateReports() {}
}

package manager;

public class RegistrationClerk extends Staff {

    private int counterNumber;
    private String assignedShift;

    public RegistrationClerk(int staffID, Name name) {
        super(staffID, name, "Registration Clerk"); // role is fixed
        this.counterNumber = 0; // default value
        this.assignedShift = "Morning"; // default value
    }

    public void registerTeam() {}
    public void registerLateTeam() {}
    public void removeTeam() {}
    public void updateDetails() {}
}

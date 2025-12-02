package manager;

public class Competitor extends Staff {

    private String leaderEmail;
    private String leaderStudentID;
    private String leaderDOB;
    private String[] members;

    public Competitor(int staffID, Name name, String username, String leaderEmail, String leaderStudentID, String leaderDOB) {
        super(staffID, name, "Competitor"); // Match Staff constructor
        this.leaderEmail = leaderEmail;
        this.leaderStudentID = leaderStudentID;
        this.leaderDOB = leaderDOB;
        this.members = new String[0];
    }

    // ----- Getters -----
    public String getLeaderEmail() { return leaderEmail; }
    public String getLeaderStudentID() { return leaderStudentID; }
    public String getLeaderDOB() { return leaderDOB; }
    public String[] getMembers() { return members; }

    // ----- Setters -----
    public void setLeaderEmail(String email) { this.leaderEmail = email; }
    public void setLeaderStudentID(String id) { this.leaderStudentID = id; }
    public void setLeaderDOB(String dob) { this.leaderDOB = dob; }
    public void setMembers(String[] members) { this.members = members; }

    @Override
    public String toString() {
        return getName() + " (Leader) - Email: " + leaderEmail;
    }
}

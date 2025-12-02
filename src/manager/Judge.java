package manager;

import java.util.ArrayList;

public class Judge extends Staff {

    private ArrayList<Team> assignedTeams = new ArrayList<>();

    public Judge(int staffID, Name name) {
        super(staffID, name, "Judge"); // role is fixed
    }

    public void updateScores() {}
    public void enterScore() {}
    public void searchTeam() {}
    public void viewLeaderboard() {}

    public void assignTeam(Team t) {
        assignedTeams.add(t);
    }
}

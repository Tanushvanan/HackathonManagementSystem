package manager;

import java.util.Arrays;

public abstract class Team {

    private int teamID;
    private String teamName;
    private String university;
    private String category;
    
    //  Declare scores array in the abstract superclass
    protected int[] scores; 

    public Team(int teamID, String teamName, String university, String category, int[] scores) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.university = university;
        this.category = category;
        if (scores == null || scores.length != 4) {
            this.scores = new int[]{0, 0, 0, 0};
        } else {
            this.scores = scores;
        }
    }

    // ---------------- Getters ----------------
    public int getTeamID() { return teamID; }
    public String getTeamName() { return teamName; }
    public String getUniversity() { return university; }
    public String getCategory() { return category; }
    
    // Non-abstract method moved from subclasses
    public int[] getScoreArray() { return scores; }

    // ---------------- Setters ----------------
    public void setTeamName(String name) { this.teamName = name; }
    public void setUniversity(String uni) { this.university = uni; }
    public void setCategory(String cat) { this.category = cat; }
    
    // Non-abstract method moved from subclasses
    public void setScores(int[] scores) { 
        if (scores.length == 4) {
            this.scores = scores; 
        }
    }

    // ---------------- Abstract method ----------------
    public abstract double getOverallScore();

    // ---------------- Full details  ----------------
    public String getFullDetails() {
        return "Team ID " + teamID + ", name " + teamName + " (" + university + ")\n" +
                teamName + " is competing in the **" + category + "** category, and received scores " + Arrays.toString(scores) +
                ", resulting in an overall score of " + String.format("%.2f", getOverallScore());
    }

    // ---------------- Short details ----------------
    public String getShortDetails() {
        String initials = "";
        for (String part : teamName.split(" ")) {
            if (!part.isEmpty()) initials += part.charAt(0);
        }
        return "TID " + teamID + " (" + initials + ") has an overall score of " + String.format("%.2f", getOverallScore());
    }
}

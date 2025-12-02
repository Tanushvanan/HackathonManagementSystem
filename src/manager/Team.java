package manager;

import java.util.Arrays;
import java.util.Objects; // Used for Objects.requireNonNull()

public abstract class Team {

    private int teamID;
    private String teamName;
    private String university;
    // Custom Attribute: HAS-A Relationship
    private Category categoryObject; 

    // Required Attribute: Array of Scores (4 scores)
    protected int[] scores; 

  // Constructor requires Category object
    public Team(int teamID, String teamName, String university, Category categoryObj, int[] scores) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.university = university;
        
        // Ensures the Category object is not null
        this.categoryObject = Objects.requireNonNull(categoryObj); 

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
    // 🌟 MODIFIED: Accesses the name from the Category object
    public String getCategory() { return categoryObject.getCategoryName(); }
    public int[] getScoreArray() { return scores; }

    // ---------------- Setters ----------------
    public void setTeamName(String name) { this.teamName = name; }
    public void setUniversity(String uni) { this.university = uni; }
    // 🌟 MODIFIED: Updates the name within the Category object
    public void setCategory(String catName) { 
        this.categoryObject.setCategoryName(catName);
    }
    
    public void setScores(int[] scores) { 
        if (scores.length == 4) {
            this.scores = scores; 
        }
    }

    // ---------------- Abstract method ----------------
    public abstract double getOverallScore();

    // ---------------- Full details ----------------
    public String getFullDetails() {
        return "Team ID " + teamID + ", name " + teamName + " (" + university + ")\n" +
                teamName + " is competing in the **" + getCategory() + "** category, and received scores " +
                Arrays.toString(scores) + ", resulting in an overall score of " +
                String.format("%.2f", getOverallScore());
    }

    // ---------------- Short details ----------------
    public String getShortDetails() {
        String initials = "";
        for (String part : teamName.split(" ")) {
            if (!part.isEmpty()) initials += part.charAt(0);
        }
        return "TID " + teamID + " (" + initials + ") has an overall score of " +
                String.format("%.2f", getOverallScore());
    }
}
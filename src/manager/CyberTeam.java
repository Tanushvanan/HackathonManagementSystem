package manager;

public class CyberTeam extends Team {
    
    private static final String CATEGORY_NAME = "Cybersecurity";
    
    // 🌟 MODIFIED: Passes a new Category object to the superclass constructor
    public CyberTeam(int teamID, String teamName, String university, int[] scores) {
        super(teamID, teamName, university, new Category(1, CATEGORY_NAME, "Specialized Cyber Security competition"), scores);
    }
    
    /**
     * Calculates the overall score using a weighted average.
     * Technical score (index 1) is given a double weight (x2).
     */
    @Override
    public double getOverallScore() {
        if (scores == null || scores.length != 4) return 0.0;

        // Scores: [Creativity(0), Technical(1), Teamwork(2), Presentation(3)]
        double weightedSum = scores[0] 
                           + (scores[1] * 2) // Technical score is double-weighted
                           + scores[2] 
                           + scores[3];

        int totalWeight = 1 + 2 + 1 + 1; // Total weight is 5

        return weightedSum / totalWeight;
    }
}

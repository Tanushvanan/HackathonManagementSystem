package manager;

public class CyberTeam extends Team {

    public CyberTeam(int teamID, String teamName, String university, String category, int[] scores) {
        super(teamID, teamName, university, category, scores);
    }

    // Weighted average: technical score (scores[1]) counts double
    @Override
    public double getOverallScore() {
        // scores[0] + scores[1]*2 + scores[2] + scores[3]
        return (scores[0] + scores[1] * 2.0 + scores[2] + scores[3]) / 5.0; 
    }
    
    
}

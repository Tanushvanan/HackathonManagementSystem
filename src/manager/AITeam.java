package manager;

import java.util.Arrays;
import java.util.stream.IntStream;

public class AITeam extends Team {

    public AITeam(int teamID, String teamName, String university, String category, int[] scores) {
        super(teamID, teamName, university, category, scores);
    }

    // Average top 3 scores
    @Override
    public double getOverallScore() {
        if (scores.length < 4) return 0;
        int sum = IntStream.of(scores).sum();
        // find the minimum score to disregard it
        int min = Arrays.stream(scores).min().getAsInt(); 
        return (sum - min) / 3.0; // sum of top 3 divided by 3
    }

   
}
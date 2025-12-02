package manager;

import java.util.Arrays;

public class AITeam extends Team {

    private static final String CATEGORY_NAME = "Artificial Intelligence";

    // Constructor initializes the Category object
    public AITeam(int teamID, String teamName, String university, int[] scores) {
        super(teamID, teamName, university, new Category(2, CATEGORY_NAME, "Specialized AI/ML competition"), scores);
    }

    /**
     * Calculates the overall score using a "trimmed mean".
     * The highest and lowest score are removed, and the remaining two scores are averaged.
     */
    @Override
    public double getOverallScore() {
        if (scores == null || scores.length < 3) {
            // Revert to simple average if trimming is not possible
            return Arrays.stream(getScoreArray()).average().orElse(0.0);
        }

        // 1. Sort a copy of the scores array
        int[] sortedScores = Arrays.copyOf(scores, scores.length);
        Arrays.sort(sortedScores);

        // 2. Sum the middle two scores (indices 1 and 2)
        double sum = sortedScores[1] + sortedScores[2];

        // 3. Calculate average of the remaining two scores
        return sum / 2.0;
    }
}
package manager;

import java.io.*;
import java.util.*;

/**
 * TeamList - stores teams, CSV load/save, stats, leaderboard.
 */
public class TeamList {

    private ArrayList<Team> teams;
    private int nextTeamID;

    public TeamList() {
        teams = new ArrayList<>();
        nextTeamID = 1;
    }

    // ----- Basic management -----
    public void addTeam(Team team) {
        teams.add(team);
        nextTeamID = Math.max(nextTeamID, team.getTeamID() + 1);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
    }

    // Extracting the search method
    public Team getTeamByID(int id) {
    // Required: Loop through the ArrayList to find the ID
    for (Team t : teams) {
        if (t.getTeamID() == id) {
            return t;
    }
    }
    return null; // Return null if not found (invalid ID)
}

    public ArrayList<Team> getAllTeams() {
        return new ArrayList<>(teams);
    }

    public int generateNextTeamID() {
        return nextTeamID;
    }

    // ----- Registration -----
    /**
     * Register a team. Duplicate check: teamID clash OR same teamName+category.
     * Returns true if added, false if duplicate.
     */
    public boolean registerTeam(Team newTeam) {
        // check ID collision
        if (getTeamByID(newTeam.getTeamID()) != null) return false;

        // check duplicate teamName + category
        for (Team t : teams) {
            if (t.getTeamName().equalsIgnoreCase(newTeam.getTeamName())
                    && t.getCategory().equalsIgnoreCase(newTeam.getCategory())) {
                return false;
            }
        }

        addTeam(newTeam);
        return true;
    }

    // ----- CSV Save/Load -----
    public ArrayList<String> loadFromCSV(String filename) {
        ArrayList<String> errors = new ArrayList<>();
        teams.clear(); // keep same behaviour: replace list

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                // skip blank lines
                if (line.trim().isEmpty()) { lineNum++; continue; }

                // optional header detection
                if (lineNum == 1 && line.toLowerCase().contains("team id")) { lineNum++; continue; }

                String[] parts = line.split(",");

                if (parts.length < 8) {
                    errors.add("Line " + lineNum + ": Missing fields (expected 8, found " + parts.length + ")");
                    lineNum++;
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String uni = parts[2].trim();
                    String cat = parts[3].trim();

                    int[] scores = new int[4];
                    for (int i = 0; i < 4; i++) scores[i] = Integer.parseInt(parts[4 + i].trim());

                    // 🌟 MODIFICATION: Use subclasses based on category
Team t;
switch (cat.toLowerCase()) {
    case "cybersecurity":
                            t = new CyberTeam(id, name, uni, scores);
        break;
    case "artificial intelligence":
                            t = new AITeam(id, name, uni, scores);
        break;
    default:
                            // Fallback (simple average) for all other categories
                            // ⚠️ NOTE: This uses an anonymous class, as requested by the user to preserve original logic.
                            Category fallbackCat = new Category(0, cat, "General Category");
                            t = new Team(id, name, uni, fallbackCat, scores) {
            @Override
            public double getOverallScore() {
                return Arrays.stream(getScoreArray()).average().orElse(0.0);
            }
        };
        break;
}

                    addTeam(t);
                } catch (NumberFormatException nfe) {
                    errors.add("Line " + lineNum + ": Number format error.");
                } catch (Exception ex) {
                    errors.add("Line " + lineNum + ": Unexpected error: " + ex.getMessage());
                }

                lineNum++;
            }

            // after loading, ensure nextTeamID is > max
            int max = 0;
            for (Team t : teams) max = Math.max(max, t.getTeamID());
            nextTeamID = Math.max(nextTeamID, max + 1);

        } catch (FileNotFoundException fnfe) {
            errors.add("FILE READ ERROR: '" + filename + "' not found. Using empty list.");
        } catch (IOException ioe) {
            errors.add("FILE READ ERROR: " + ioe.getMessage());
        }

        return errors;
    }

    public void saveToCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // header
            pw.println("teamID,teamName,university,category,score1,score2,score3,score4");
            for (Team t : teams) {
                int[] s = t.getScoreArray();
                pw.printf("%d,%s,%s,%s,%d,%d,%d,%d%n",
                        t.getTeamID(),
                        escapeCsv(t.getTeamName()),
                        escapeCsv(t.getUniversity()),
                        escapeCsv(t.getCategory()), // Uses the string name from Team.getCategory()
                        s[0], s[1], s[2], s[3]);
            }
        } catch (IOException e) {
            System.out.println("Error saving CSV: " + e.getMessage());
        }
    }

    // helper: minimal CSV escaping of commas
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ----- Reports & Stats -----
    // Extracting the final report method
    public void saveReport(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("=========================================");
        pw.println("      Hackathon Final Report           ");
            pw.println("=========================================\n");

        // Required 1: Table of teams with full details
            pw.println("=== 1. Full Teams Detail Table ===\n");
            for (Team t : teams) pw.println(t.getFullDetails() + "\n");

        // Required 2: Details of the team with the highest overall score
            Team top = getHighestScoreTeam();
            pw.println("\n=== 2. Team with Highest Score ===");
            if (top != null) pw.println(top.getFullDetails() + "\n");
            else pw.println("No teams available.");

        // Required 3: Four other summary statistics (avg, min, max, total teams)
            pw.println("\n=== 3. Summary Stats ===");
            pw.println("Total Teams: " + teams.size());
            pw.println("Average Overall Score: " + String.format("%.2f", getAverageScore()));
            pw.println("Minimum Overall Score: " + String.format("%.2f", getMinOverallScore()));
            pw.println("Maximum Overall Score: " + String.format("%.2f", getMaxOverallScore()));

        // Required 4: Frequency report
            pw.println("\n=== 4. Individual Score Frequency ===");
            int[] freq = getScoreFrequency();
            for (int i = 0; i < freq.length; i++) pw.println("Score " + i + ": " + freq[i] + " times awarded");

            pw.println("\nReport Generation Complete.");
        } catch (IOException e) {
            System.out.println("ERROR SAVING REPORT: " + e.getMessage());
        }
    }

   // Extracting the Score Frequency logic
    public int[] getScoreFrequency() {
    // Array to hold counts for scores 0 through 5
        int[] freq = new int[6];
        for (Team t : teams) {
        // Iterates through all scores of all teams
            for (int s : t.getScoreArray()) {
            // Check boundaries (0-5)
            if (s >= 0 && s <= 5) {
                freq[s]++;
            }
        }
    }
        return freq;
    }

    public double getAverageScore() {
        if (teams.isEmpty()) return 0;
        double sum = 0;
        for (Team t : teams) sum += t.getOverallScore();
        return sum / teams.size();
    }

    public double getMinOverallScore() {
        if (teams.isEmpty()) return 0;
        double min = Double.MAX_VALUE;
        for (Team t : teams) min = Math.min(min, t.getOverallScore());
        return min;
    }

    public double getMaxOverallScore() {
        if (teams.isEmpty()) return 0;
        double max = Double.MIN_VALUE;
        for (Team t : teams) max = Math.max(max, t.getOverallScore());
        return max;
    }

    public Team getHighestScoreTeam() {
        if (teams.isEmpty()) return null;
        Team best = teams.get(0);
        for (Team t : teams) if (t.getOverallScore() > best.getOverallScore()) best = t;
        return best;
    }

    public ArrayList<Team> getLeaderboardByCategory(String category) {
        ArrayList<Team> list = new ArrayList<>();
        for (Team t : teams) if (t.getCategory().equalsIgnoreCase(category)) list.add(t);
        list.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return list;
    }
}
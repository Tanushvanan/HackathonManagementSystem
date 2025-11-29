package manager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TeamList {

    private ArrayList<Team> teams = new ArrayList<>();

    public void addTeam(Team t) { teams.add(t); }
    public ArrayList<Team> getAllTeams() { return teams; }

    public Team getTeamByID(int id) {
        for (Team t : teams) if (t.getTeamID() == id) return t;
        return null;
    }

    public void removeTeam(Team t) { teams.remove(t); }

    // -------------------------- CSV LOADING (Stage 5) --------------------------
    public ArrayList<String> loadFromCSV(String filename) {
        teams.clear();
        ArrayList<String> errors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                // Ignore header line if any 
                if (lineNum == 1 && line.toLowerCase().contains("team id")) {
                    lineNum++;
                    continue;
                }
                
                String[] parts = line.split(",");

                // Data Validation 1: Check field count
                if (parts.length < 8) {
                    errors.add("Line " + lineNum + ": Missing fields (Expected 8, found " + parts.length + ")");
                    lineNum++;
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    // Data Validation 2: Check for duplicate ID
                    if (getTeamByID(id) != null) {
                        errors.add("Line " + lineNum + ": Duplicate ID " + id);
                        lineNum++;
                        continue;
                    }

                    String name = parts[1].trim();
                    String uni = parts[2].trim();
                    String cat = parts[3].trim();

                    int[] scores = new int[]{
                        Integer.parseInt(parts[4].trim()),
                        Integer.parseInt(parts[5].trim()),
                        Integer.parseInt(parts[6].trim()),
                        Integer.parseInt(parts[7].trim())
                    };

                    // Data Validation 3: Ensure scores 0-5
                    boolean validScores = true;
                    for (int s : scores) {
                        if (s < 0 || s > 5) {
                            errors.add("Line " + lineNum + ": Invalid score " + s + " (must be 0-5)");
                            validScores = false;
                            break;
                        }
                    }
                    if (!validScores) {
                        lineNum++;
                        continue;
                    }

                    // Decide subclass based on category (Stage 6)
                    Team team;
                    if (cat.equalsIgnoreCase("Cybersecurity")) {
                        team = new CyberTeam(id, name, uni, cat, scores);
                    } else if (cat.equalsIgnoreCase("Artificial Intelligence")) {
                        team = new AITeam(id, name, uni, cat, scores);
                    } else {
                        // Use CyberTeam as default for other categories
                        team = new CyberTeam(id, name, uni, cat, scores); 
                    }
                    teams.add(team);

                } catch (NumberFormatException ex) {
                    // Data Validation 4: Check for non-integer ID/Scores
                    errors.add("Line " + lineNum + ": Invalid number format in ID or Scores.");
                } catch (Exception ex) {
                    errors.add("Line " + lineNum + ": Unexpected error during parsing: " + ex.getMessage());
                }

                lineNum++;
            }

        } catch (FileNotFoundException ex) {
            errors.add("FILE READ ERROR: The file '" + filename + "' was not found. Using empty list.");
        } catch (IOException ex) {
            errors.add("FILE READ ERROR: IO Exception reading file: " + ex.getMessage());
        }

        return errors;
    }

    public void saveToCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
             
             pw.println("teamID,teamName,university,category,score1,score2,score3,score4");
             
            for (Team t : teams) {
                
                int[] scores = t.getScoreArray(); 

                pw.println(t.getTeamID() + "," + t.getTeamName() + "," + t.getUniversity() + "," +
                        t.getCategory() + "," +
                        scores[0] + "," + scores[1] + "," + scores[2] + "," + scores[3]);
            }
        } catch (Exception ex) {
            System.out.println("ERROR SAVING CSV: " + ex.getMessage());
        }
    }

    // -------------------------- STATS (Stage 5) --------------------------
    public Team getHighestScoreTeam() {
        if (teams.isEmpty()) return null;
        return Collections.max(teams, Comparator.comparingDouble(Team::getOverallScore));
    }

    public double getAverageScore() {
        if (teams.isEmpty()) return 0;
        return teams.stream()
                    .mapToDouble(Team::getOverallScore)
                    .average()
                    .orElse(0.0);
    }
    
    public double getMinOverallScore() {
        if (teams.isEmpty()) return 0.0;
        return teams.stream()
                    .mapToDouble(Team::getOverallScore)
                    .min()
                    .orElse(0.0);
    }
    
    public double getMaxOverallScore() {
        if (teams.isEmpty()) return 0.0;
        return teams.stream()
                    .mapToDouble(Team::getOverallScore)
                    .max()
                    .orElse(0.0);
    }

    // Frequency report (Stage 5 - Challenging Stat)
    public int[] getScoreFrequency() {
        int[] freq = new int[6]; // scores 0-5
        for (Team t : teams) {
            // No downcasting needed!
            int[] scores = t.getScoreArray();
            for (int s : scores) {
                if (s >= 0 && s <= 5) {
                    freq[s]++;
                }
            }
        }
        return freq;
    }

    // -------------------------- SAVE FULL REPORT (Stage 5) --------------------------
    public void saveReport(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("=========================================");
            pw.println("         Hackathon Final Report          ");
            pw.println("=========================================\n");
            
            // 1. A table of teams with full details
            pw.println("=== 1. Full Teams Detail Table ===\n");
            for (Team t : teams) {
                pw.println(t.getFullDetails() + "\n");
            }

            // 2. Details of the team with the highest overall score
            Team top = getHighestScoreTeam();
            pw.println("\n=== 2. Team with the Highest Overall Score ===");
            if (top != null) {
                pw.println(top.getFullDetails() + "\n");
            } else {
                pw.println("No teams available.");
            }

            // 3. Four other summary statistics
            pw.println("\n=== 3. Summary Statistics ===");
            pw.println("Total Teams: " + teams.size());
            pw.println("Average Overall Score: " + String.format("%.2f", getAverageScore()));
            pw.println("Minimum Overall Score: " + String.format("%.2f", getMinOverallScore()));
            pw.println("Maximum Overall Score: " + String.format("%.2f", getMaxOverallScore()));

            // 4. Frequency report
            pw.println("\n=== 4. Individual Score Frequency Report ===");
            int[] freq = getScoreFrequency();
            for (int i = 0; i < freq.length; i++) {
                pw.println("Score " + i + ": " + freq[i] + " times awarded");
            }
            pw.println("\nReport Generation Complete.");

        } catch (Exception ex) {
            System.out.println("ERROR SAVING REPORT: " + ex.getMessage());
        }
    }
}

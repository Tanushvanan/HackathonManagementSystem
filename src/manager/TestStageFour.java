// File: TestStageFour.java (for isolated testing only)
package manager;


public class TestStageFour {
    
    public static void main(String[] args) {
        
        System.out.println("--- Stage 4 Isolated Unit Testing ---");
        
        // 1. Test Case 1: CyberTeam (Weighted Score: 4.20)
        int[] cyberScores = {5, 4, 3, 5}; 
        Team team1 = new CyberTeam(101, "Cyber Titans", "UTM", cyberScores);
        
        System.out.println("\n--- Testing CyberTeam Methods (TID 101) ---");
        System.out.println(team1.getFullDetails()); 
        System.out.println(team1.getShortDetails());
        
        // 2. Test Case 2: AITeam (Trimmed Mean Score: 3.00)
        int[] aiScores = {2, 5, 4, 1}; 
        Team team2 = new AITeam(202, "AI Innovators", "Monash", aiScores);
        
        System.out.println("\n--- Testing AITeam Methods (TID 202) ---");
        System.out.println(team2.getFullDetails());
        System.out.println(team2.getShortDetails());
    }
}

package manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class LeaderboardGUI extends JFrame {

    private TeamList teamList; // Reference to the main TeamList
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryCombo;

    public LeaderboardGUI(TeamList teamList) {
        this.teamList = teamList;

        setTitle("Hackathon Leaderboard");
        setSize(820, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Category:"));

        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("Cybersecurity");
        categoryCombo.addItem("Artificial Intelligence");
        categoryCombo.addItem("Web Development");
        categoryCombo.addItem("Mobile Application");
        categoryCombo.addItem("Data Science");
        categoryCombo.addItem("Cloud Computing");
        categoryCombo.addItem("Sustainability Tech");
        topPanel.add(categoryCombo);

        JButton loadButton = new JButton("Load Leaderboard");
        topPanel.add(loadButton);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Rank", "Team ID", "Team Name", "University", "Scores", "Overall Score"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadLeaderboard());
        loadLeaderboard();
    }

    public JComboBox<String> getCategoryCombo() {
        return categoryCombo;
    }

    public void loadLeaderboard() {
        String category = (String) categoryCombo.getSelectedItem();
        ArrayList<Team> rankedTeams = teamList.getLeaderboardByCategory(category);

        tableModel.setRowCount(0);
        int rank = 1;
        for (Team t : rankedTeams) {
            int[] s = t.getScoreArray();
            String scores = "[" + s[0] + ", " + s[1] + ", " + s[2] + ", " + s[3] + "]";
            tableModel.addRow(new Object[]{rank, t.getTeamID(), t.getTeamName(), t.getUniversity(), scores, String.format("%.2f", t.getOverallScore())});
            rank++;
        }

        if (rankedTeams.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No teams found in category: " + category);
        }
    }
}

package manager.gui;

import manager.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

// This class is the View, handling user interaction and display.
public class HackathonGUI extends JFrame {

    private TeamList teamList; // The Model
    private JTable teamTable;
    private DefaultTableModel tableModel;

    // GUI fields for Add/Edit panels 
    private JTextField searchField;
    private JTextArea statsArea;

    public HackathonGUI(TeamList teamList) {
        this.teamList = teamList;
        setTitle("Hackathon Manager - MVC Implemented");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Set custom close operation
        setLocationRelativeTo(null);

        // Stage 6 requirement: GUI can be one window containing 3 separate panels 
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("1. Teams Table", createTeamTablePanel());
        tabs.add("2. Edit/Remove Team", createEditUpdatePanel());
        tabs.add("3. Add Team", createAddTeamPanel());
        tabs.add("4. Statistics & Report", createStatsPanel());

        add(tabs);
        refreshTable();
        refreshStats();
        
        // Custom window closing action (Stage 6 requirement)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeProgram();
            }
        });
    }
    
    private void closeProgram() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to close? The final report will be saved.", "Exit", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            teamList.saveReport("HackathonReport.txt");
            JOptionPane.showMessageDialog(this, "Report saved. Closing program.");
            System.exit(0);
        }
    }


    // ---------------------- 1. TEAMS TABLE ----------------------
    private JPanel createTeamTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "University", "Category", "Scores", "Overall"}, 0
        );
        teamTable = new JTable(tableModel);
        panel.add(new JScrollPane(teamTable), BorderLayout.CENTER);

        // Sorting and Filtering Panel (Stage 6 requirements)
        JPanel topPanel = new JPanel();
        JComboBox<String> sortBox = new JComboBox<>(new String[]{
                "Team ID", "Name", "Category", "Overall Score"
        });
        JComboBox<String> filterBox = new JComboBox<>(new String[]{
                "All", "Cybersecurity", "Artificial Intelligence"
        });
        JButton sortFilterBtn = new JButton("Sort/Filter");

        topPanel.add(new JLabel("Sort by:")); topPanel.add(sortBox);
        topPanel.add(new JLabel("Filter by Category (Subclass):")); topPanel.add(filterBox);
        topPanel.add(sortFilterBtn);
        panel.add(topPanel, BorderLayout.NORTH);

        sortFilterBtn.addActionListener(e -> refreshTable((String)sortBox.getSelectedItem(), (String)filterBox.getSelectedItem()));

        // Control Buttons
        JPanel btnPanel = new JPanel();
        JButton loadBtn = new JButton("Load CSV");
        JButton saveBtn = new JButton("Save CSV");
        JButton refreshBtn = new JButton("Refresh Table");
        JButton closeBtn = new JButton("Close Program & Save Report");
        
        btnPanel.add(loadBtn); btnPanel.add(saveBtn); btnPanel.add(refreshBtn); btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> loadCSV());
        saveBtn.addActionListener(e -> {
            teamList.saveToCSV("HackathonTeams.csv");
            JOptionPane.showMessageDialog(this, "CSV Saved Successfully!");
        });
        refreshBtn.addActionListener(e -> refreshTable());
        closeBtn.addActionListener(e -> closeProgram());

        return panel;
    }
    
    private void refreshTable(String sortBy, String filterBy) {
        // Create a copy to sort/filter without changing the original ArrayList order (Stage 6 requirement)
        List<Team> teams = new ArrayList<>(teamList.getAllTeams()); 

        // Filter by Subclass/Category
        if (!filterBy.equalsIgnoreCase("All")) {
            teams.removeIf(t -> !t.getCategory().equalsIgnoreCase(filterBy));
        }

        // Sort
        teams.sort((t1, t2) -> {
            switch(sortBy) {
                case "Team ID": return Integer.compare(t1.getTeamID(), t2.getTeamID());
                case "Name": return t1.getTeamName().compareToIgnoreCase(t2.getTeamName());
                case "Category": return t1.getCategory().compareToIgnoreCase(t2.getCategory());
                case "Overall Score": return Double.compare(t2.getOverallScore(), t1.getOverallScore()); // Descending
                default: return 0;
            }
        });

        tableModel.setRowCount(0);
        for (Team t : teams) {
         
            int[] scores = t.getScoreArray(); 

            tableModel.addRow(new Object[]{
                    t.getTeamID(),
                    t.getTeamName(),
                    t.getUniversity(),
                    t.getCategory(),
                    Arrays.toString(scores),
                    String.format("%.2f", t.getOverallScore())
            });
        }
    }

    private void refreshTable() {
        refreshTable("Team ID", "All");
    }

    // ---------------------- 2. EDIT/UPDATE TEAM ----------------------
    private JPanel createEditUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Team ID:"));
        searchField = new JTextField(5);
        searchPanel.add(searchField);
        JButton loadBtn = new JButton("Load Team");
        searchPanel.add(loadBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Edit Fields Panel
        JPanel editPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        
        JTextField editNameField = new JTextField();
        JTextField editUniversityField = new JTextField();
        JComboBox<String> editCategoryBox = new JComboBox<>(new String[]{
                "Cybersecurity", "Artificial Intelligence", "Web Development", 
                "Data Science", "Cloud Computing", "Sustainability Tech"
        });

        
        SpinnerModel scoreModel = new SpinnerNumberModel(0, 0, 5, 1);
        JSpinner editCreativityBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner editTechnicalBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner editTeamworkBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner editPresentationBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        
        JLabel overallScoreLabel = new JLabel("Overall Score: N/A");
        
        editPanel.add(new JLabel("Team Name:")); editPanel.add(editNameField);
        editPanel.add(new JLabel("University:")); editPanel.add(editUniversityField);
        editPanel.add(new JLabel("Category:")); editPanel.add(editCategoryBox);
        editPanel.add(new JLabel("Creativity Score:")); editPanel.add(editCreativityBox);
        editPanel.add(new JLabel("Technical Score:")); editPanel.add(editTechnicalBox);
        editPanel.add(new JLabel("Teamwork Score:")); editPanel.add(editTeamworkBox);
        editPanel.add(new JLabel("Presentation Score:")); editPanel.add(editPresentationBox);
        editPanel.add(new JLabel("Current Overall:")); editPanel.add(overallScoreLabel);
        
        panel.add(editPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel();
        JButton updateBtn = new JButton("Update Team");
        JButton removeBtn = new JButton("Remove Team");
        actionPanel.add(updateBtn); actionPanel.add(removeBtn);

        JTextArea editDetailsArea = new JTextArea(6, 60);
        editDetailsArea.setEditable(false);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(actionPanel, BorderLayout.NORTH);
        southPanel.add(new JScrollPane(editDetailsArea), BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        // --- Controller Logic for Edit Panel ---
        
        // Listener to update score label immediately
        Runnable updateOverall = () -> {
            try {
                int id = Integer.parseInt(searchField.getText());
                Team t = teamList.getTeamByID(id);
                if (t == null) return;
                
              
                int[] tempScores = {
                    (Integer) editCreativityBox.getValue(),
                    (Integer) editTechnicalBox.getValue(),
                    (Integer) editTeamworkBox.getValue(),
                    (Integer) editPresentationBox.getValue()
                };
                
                
                t.setScores(tempScores);
                overallScoreLabel.setText("Overall Score: " + String.format("%.2f", t.getOverallScore()));
                
            } catch (Exception ignored) {
                overallScoreLabel.setText("Overall Score: Error");
            }
        };
        
        editCreativityBox.addChangeListener(e -> updateOverall.run());
        editTechnicalBox.addChangeListener(e -> updateOverall.run());
        editTeamworkBox.addChangeListener(e -> updateOverall.run());
        editPresentationBox.addChangeListener(e -> updateOverall.run());


        loadBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(searchField.getText());
                Team t = teamList.getTeamByID(id);
                if (t == null) { editDetailsArea.setText("Team not found."); return; }
                
                // View full/short details for a team, given a team number (Stage 6)
                editDetailsArea.setText(t.getFullDetails() + "\n\nShort Details: " + t.getShortDetails());

                editNameField.setText(t.getTeamName());
                editUniversityField.setText(t.getUniversity());
                editCategoryBox.setSelectedItem(t.getCategory());

              
                int[] sc = t.getScoreArray();

                editCreativityBox.setValue(sc[0]);
                editTechnicalBox.setValue(sc[1]);
                editTeamworkBox.setValue(sc[2]);
                editPresentationBox.setValue(sc[3]);
                
                updateOverall.run();

            } catch (Exception ex) {
                editDetailsArea.setText("Invalid ID. Please enter a number.");
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(searchField.getText());
                Team t = teamList.getTeamByID(id);
                if (t == null) { editDetailsArea.setText("Team not found."); return; }

                // Edit team details (Stage 6)
                t.setTeamName(editNameField.getText());
                t.setUniversity(editUniversityField.getText());
                t.setCategory((String)editCategoryBox.getSelectedItem());

                // Update scores (and implicitly update overall score via setScores in Team)
                int[] scores = new int[]{
                        (Integer) editCreativityBox.getValue(),
                        (Integer) editTechnicalBox.getValue(),
                        (Integer) editTeamworkBox.getValue(),
                        (Integer) editPresentationBox.getValue()
                };
                
                t.setScores(scores); 

                teamList.saveToCSV("HackathonTeams.csv");
                refreshTable();
                refreshStats();
                updateOverall.run();
                editDetailsArea.setText("Team updated successfully:\n" + t.getFullDetails());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or update failed.");
            }
        });

        removeBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(searchField.getText());
                Team t = teamList.getTeamByID(id);
                if (t == null) { editDetailsArea.setText("Team not found."); return; }

                // Remove a team (Stage 6)
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove team " + t.getTeamName() + "?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    teamList.removeTeam(t);
                    teamList.saveToCSV("HackathonTeams.csv");
                    refreshTable();
                    refreshStats();
                    editDetailsArea.setText("Team removed.");
                }
            } catch (Exception ex) {
                editDetailsArea.setText("Invalid ID.");
            }
        });

        return panel;
    }

    // ---------------------- 3. ADD TEAM PANEL ----------------------
    private JPanel createAddTeamPanel() {
        JPanel panel = new JPanel(new GridLayout(10, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField addIDField = new JTextField();
        JTextField addNameField = new JTextField();
        JTextField addUniversityField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "Cybersecurity", "Artificial Intelligence", "Web Development", 
                "Data Science", "Cloud Computing", "Sustainability Tech"
        });
        JSpinner creativityBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner technicalBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner teamworkBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner presentationBox = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));

        panel.add(new JLabel("Team ID (Unique):")); panel.add(addIDField);
        panel.add(new JLabel("Team Name:")); panel.add(addNameField);
        panel.add(new JLabel("University:")); panel.add(addUniversityField);
        panel.add(new JLabel("Category:")); panel.add(categoryBox);
        panel.add(new JLabel("Creativity Score:")); panel.add(creativityBox);
        panel.add(new JLabel("Technical Score:")); panel.add(technicalBox);
        panel.add(new JLabel("Teamwork Score:")); panel.add(teamworkBox);
        panel.add(new JLabel("Presentation Score:")); panel.add(presentationBox);

        JButton addBtn = new JButton("Add Team");
        JButton clearBtn = new JButton("Clear Fields");
        panel.add(addBtn); panel.add(clearBtn);

        addBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(addIDField.getText());
                if (teamList.getTeamByID(id) != null) {
                    JOptionPane.showMessageDialog(this, "Team ID already exists. Must be unique.");
                    return;
                }

                int[] scores = new int[]{
                        (Integer)creativityBox.getValue(),
                        (Integer)technicalBox.getValue(),
                        (Integer)teamworkBox.getValue(),
                        (Integer)presentationBox.getValue()
                };

                Team t;
                String cat = (String) categoryBox.getSelectedItem();
                // Determine subclass based on category
                if (cat.equalsIgnoreCase("Cybersecurity")) {
                    t = new CyberTeam(id, addNameField.getText(), addUniversityField.getText(), cat, scores);
                } else if (cat.equalsIgnoreCase("Artificial Intelligence")) {
                    t = new AITeam(id, addNameField.getText(), addUniversityField.getText(), cat, scores);
                } else {
                    t = new CyberTeam(id, addNameField.getText(), addUniversityField.getText(), cat, scores); // Default/Other
                }

                teamList.addTeam(t);
                teamList.saveToCSV("HackathonTeams.csv");
                JOptionPane.showMessageDialog(this, "Team added: " + t.getShortDetails());
                refreshTable();
                refreshStats();

                // Clear fields
                addIDField.setText(""); 
                addNameField.setText(""); 
                addUniversityField.setText("");
                creativityBox.setValue(0);
                technicalBox.setValue(0);
                teamworkBox.setValue(0);
                presentationBox.setValue(0);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Team ID must be a number.");
            } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, "Error adding team: " + ex.getMessage());
            }
        });

        clearBtn.addActionListener(e -> {
            addIDField.setText(""); 
            addNameField.setText(""); 
            addUniversityField.setText("");
        });

        return panel;
    }

    // ---------------------- 4. STATISTICS PANEL ----------------------
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statsArea = new JTextArea();
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setEditable(false);
        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh Stats");
        JButton saveReportBtn = new JButton("Generate & Save Full Report");
        
        refreshBtn.addActionListener(e -> refreshStats());
        saveReportBtn.addActionListener(e -> {
            teamList.saveReport("HackathonReport.txt");
            JOptionPane.showMessageDialog(this, "Report saved to HackathonReport.txt");
        });
        
        controlPanel.add(refreshBtn);
        controlPanel.add(saveReportBtn);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshStats() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=========================================\n");
        sb.append("        Current Hackathon Statistics     \n");
        sb.append("=========================================\n");
        
        // Four other summary statistics (Stage 5)
        sb.append("Total Teams:         ").append(teamList.getAllTeams().size()).append("\n");
        sb.append("Avg Overall Score:   ").append(String.format("%.2f", teamList.getAverageScore())).append("\n");
        sb.append("Min Overall Score:   ").append(String.format("%.2f", teamList.getMinOverallScore())).append("\n");
        sb.append("Max Overall Score:   ").append(String.format("%.2f", teamList.getMaxOverallScore())).append("\n");


        Team top = teamList.getHighestScoreTeam();
        if (top != null) {
            sb.append("\n=== Top Scoring Team ===\n");
            sb.append(top.getFullDetails()).append("\n");
        }

        // Frequency report (Stage 5)
        sb.append("\n--- Score Frequency (0-5) ---\n");
        int[] freq = teamList.getScoreFrequency();
        for (int i = 0; i < freq.length; i++) {
            sb.append(String.format("Score %d: %d times awarded\n", i, freq[i]));
        }

        statsArea.setText(sb.toString());
    }

    private void loadCSV() {
        ArrayList<String> errors = teamList.loadFromCSV("HackathonTeams.csv");
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("CSV Load Errors:\n");
            for (String e : errors) sb.append(e).append("\n");
            JOptionPane.showMessageDialog(this, sb.toString(), "CSV Error/Validation Issues", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "CSV Loaded Successfully!");
        }
        refreshTable();
        refreshStats();
    }
}

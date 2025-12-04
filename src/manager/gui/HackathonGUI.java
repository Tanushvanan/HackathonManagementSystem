package manager.gui;

// Import necessary Java Swing and utility classes
import manager.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * HackathonGUI is the main JFrame for the Hackathon Management System.
 * It provides different views and functionalities based on the user's role.
 * - Judges can open the "Edit / Remove Team" tab but only edit scores.
 * - Add Team tab is shown only to users with modification rights (not Judge/Competitor/Public).
 * - Any anonymous Team creation provides getOverallScore() implementation to avoid abstract-class errors.
 */
public class HackathonGUI extends JFrame {

    // TeamList object to manage the teams data
    private TeamList teamList;
    // The current logged-in Staff object (Judge, Organizer, etc.)
    private Staff currentUser;
    // The role of the current user (e.g., "Judge", "Organizer")
    private String role;

    // References for the Teams Table tab
    private JTable teamTable;
    private DefaultTableModel tableModel;
    // Text area for displaying statistics and reports
    private JTextArea statsArea;

    // Leaderboard references
    private JTable lbTable;
    private DefaultTableModel lbModel;
    private JComboBox<String> lbCatBox; // Dropdown for category filtering on leaderboard

    // Constructor: Initializes the GUI with team data and user context
    public HackathonGUI(TeamList teamList, Staff staff, String role) {
        this.teamList = teamList;
        this.currentUser = staff;
        this.role = role;

        // Set up the main frame properties
        setTitle("Hackathon Manager — Role: " + role);
        setSize(1200, 780);// set window size
        setLocationRelativeTo(null); // center the window
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handle close event manually

        // Create the root panel with a border layout
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(10, 12, 12, 12));
        add(root);

        // ---------- Header ----------
        // Create and configure the header panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 78, 136)); // professional blue
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        // Create and configure the main title label
        JLabel title = new JLabel("Hackathon Manager", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        // Create and configure the role label
        JLabel roleLabel = new JLabel("Logged as: " + role, SwingConstants.RIGHT);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleLabel.setForeground(Color.WHITE);
        header.add(roleLabel, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH); // Add header to the top of the root panel

        /// ---------- Tabs ----------
        // Create the JTabbedPane to hold different sections
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Always visible to everyone: Add the main display tabs
        tabs.addTab("Teams Table", createTeamTablePanel());
        tabs.addTab("Leaderboard", createLeaderboardPanel());
        tabs.addTab("Statistics & Report", createStatsPanel());

        // Judge: Can score only → needs Edit tab (but with restrictions)
        if (role.equalsIgnoreCase("Judge")) {
            tabs.addTab("Edit / Remove Team", createEditUpdatePanel());
        }

        // Competitor: Can only register, view boards + stats
        if (role.equalsIgnoreCase("Competitor")) {
            tabs.addTab("Registration", createRegistrationPanel());
        }

        // Public: NO registration, NO edit, NO add (only the always-visible tabs)
        // (nothing to add here)

        // Registration Clerk: Can add + edit (with full rights on team info, scores)
        if (role.equalsIgnoreCase("Registration Clerk")) {
            tabs.addTab("Add Team", createAddTeamPanel());
            tabs.addTab("Edit / Remove Team", createEditUpdatePanel());
        }

        // Organizer/Admin: Full access (add and edit/remove)
        if (role.equalsIgnoreCase("Organizer") || role.equalsIgnoreCase("Admin")) {
            tabs.addTab("Add Team", createAddTeamPanel());
            tabs.addTab("Edit / Remove Team", createEditUpdatePanel());
            // All other tabs are already added above
        }

        // Add tabs to window center
        root.add(tabs, BorderLayout.CENTER);


        // ---------- Footer ----------
        // Create and configure the footer panel for action buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        // Create footer buttons
        JButton saveCSV = new JButton("Save CSV");
        JButton saveReport = new JButton("Save Report");
        JButton closeBtn = new JButton("Exit");

        // Add action listener for Save CSV button
        saveCSV.addActionListener(e -> {
            teamList.saveToCSV("HackathonTeams.csv"); // Save data to CSV file
            JOptionPane.showMessageDialog(this, "CSV saved.");
        });

        // Add action listener for Save Report button (role-restricted)
        saveReport.addActionListener(e -> {
            if (role.equalsIgnoreCase("Judge") || role.equalsIgnoreCase("Public")) {
                // Deny permission for Judge/Public roles
                JOptionPane.showMessageDialog(this, "You do not have permission to save reports.");
            } else {
                teamList.saveReport("HackathonReport.txt"); // Save report to text file
                JOptionPane.showMessageDialog(this, "Report saved.");
            }
        });

        // Add action listener for Exit button (calls custom exit method)
        closeBtn.addActionListener(e -> exitAndSave());

        // Add buttons to the footer
        footer.add(saveCSV);
        footer.add(saveReport);
        footer.add(closeBtn);
        root.add(footer, BorderLayout.SOUTH); // Add footer to the bottom

        // initial population: Load data into tables and stats area
        refreshTable();
        refreshStats();

        // confirm exit -> save report: Handle window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitAndSave(); }
        });
    }

    // Custom method to handle exiting the program, prompting for confirmation and saving report
    private void exitAndSave() {
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Exit program? Any unsaved CSV will be lost.", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            teamList.saveReport("HackathonReport.txt"); // Save report on exit
            dispose(); // Close the frame
            System.exit(0); // Exit the application
        }
    }

    // ---------------- Teams Table ----------------
    // Creates the panel containing the main table view of teams
    private JPanel createTeamTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Define the table model with columns, making cells non-editable
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "University", "Category", "Scores", "Overall"}, 0) {
            // make cells non-editable in table view
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        // Create the JTable with the model
        teamTable = new JTable(tableModel);
        teamTable.setFillsViewportHeight(true);
        teamTable.setRowHeight(28);
        teamTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // alternate row colors and selection highlight using a custom renderer
        teamTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                // Call super method for basic rendering
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // Apply custom colors based on selection and row index (zebra striping)
                if (isSelected) {
                    setBackground(new Color(183, 212, 255)); // Highlight color
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 255)); // Alternate colors
                }
                setBorder(null); // Remove default focus border
                return this;
            }
        });

        // Add the table inside a JScrollPane
        panel.add(new JScrollPane(teamTable), BorderLayout.CENTER);

        // Controls: Setup for sorting and filtering
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.setBorder(new EmptyBorder(6, 6, 6, 6));
        JComboBox<String> sortBox = new JComboBox<>(new String[]{"Team ID", "Name", "Category", "Overall Score"});
        JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Cybersecurity", "Artificial Intelligence", "Web Development", "Data Science", "Cloud Computing", "Sustainability Tech"});
        JButton apply = new JButton("Apply");
        apply.setPreferredSize(new Dimension(90, 28));
        // Add action to refresh the table with current sort and filter options
        apply.addActionListener(e -> refreshTable((String) sortBox.getSelectedItem(), (String) filterBox.getSelectedItem()));

        // Add controls to the panel
        controls.add(new JLabel("Sort:"));
        controls.add(sortBox);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(new JLabel("Filter:"));
        controls.add(filterBox);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(apply);
        panel.add(controls, BorderLayout.NORTH);

        return panel;
    }

    // Overloaded method to refresh the table with default sort/filter
    private void refreshTable() { refreshTable("Team ID", "All"); }

    // Method to refresh and re-populate the team table based on sort and filter options
    private void refreshTable(String sortBy, String filterBy) {
        // Get all teams and create a mutable list
        List<Team> teams = new ArrayList<>(teamList.getAllTeams());
        // Apply category filter if not "All"
        if (!"All".equalsIgnoreCase(filterBy)) teams.removeIf(t -> !t.getCategory().equalsIgnoreCase(filterBy));

        // Sort the list of teams based on the selected criteria
        teams.sort((a, b) -> {
            switch (sortBy) {
                // Sorting logic for different columns
                case "Team ID": return Integer.compare(a.getTeamID(), b.getTeamID());
                case "Name": return a.getTeamName().compareToIgnoreCase(b.getTeamName());
                case "Category": return a.getCategory().compareToIgnoreCase(b.getCategory());
                // Sort Overall Score in descending order (highest score first)
                case "Overall Score": return Double.compare(b.getOverallScore(), a.getOverallScore());
                default: return 0; // Default: no change
            }
        });

        // Clear existing table rows
        tableModel.setRowCount(0);
        // Add sorted/filtered teams to the table model
        for (Team t : teams) {
            int[] s = t.getScoreArray();
            tableModel.addRow(new Object[]{t.getTeamID(), t.getTeamName(), t.getUniversity(),
                    t.getCategory(), Arrays.toString(s), String.format("%.2f", t.getOverallScore())});
        }

        loadLeaderboard(); // keep leaderboard in sync with current data
    }

    // ---------------- Edit / Remove Panel ----------------
    // Creates the panel for editing team details and scores, or removing a team
    private JPanel createEditUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Load Team by ID section
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        north.setBackground(Color.WHITE);
        north.add(new JLabel("Team ID:"));
        JTextField idField = new JTextField(8);
        idField.setPreferredSize(new Dimension(140, 28));
        north.add(idField);
        JButton loadBtn = new JButton("Load");
        loadBtn.setPreferredSize(new Dimension(100, 28));
        north.add(loadBtn);
        panel.add(north, BorderLayout.NORTH);

        // Form center: Team details and scores
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        // Team detail fields
        JLabel lblName = new JLabel("Team Name:");
        JTextField nameField = new JTextField(); nameField.setPreferredSize(new Dimension(260, 30));
        JLabel lblUni = new JLabel("University:");
        JTextField uniField = new JTextField(); uniField.setPreferredSize(new Dimension(260, 30));
        JLabel lblCat = new JLabel("Category:");
        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "Cybersecurity", "Artificial Intelligence", "Web Development",
                "Data Science", "Cloud Computing", "Sustainability Tech"});
        catBox.setPreferredSize(new Dimension(260, 30));

        // Score spinners (0 to 5)
        JSpinner s1 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s2 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s3 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s4 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        Dimension spinnerSize = new Dimension(72, 30);
        s1.setPreferredSize(spinnerSize); s2.setPreferredSize(spinnerSize); s3.setPreferredSize(spinnerSize); s4.setPreferredSize(spinnerSize);

        // Overall score display label
        JLabel overallLabel = new JLabel("Overall: N/A");
        overallLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add fields to the form panel using GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; form.add(lblName, gbc);
        gbc.gridx = 1; form.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy++; form.add(lblUni, gbc);
        gbc.gridx = 1; form.add(uniField, gbc);
        gbc.gridx = 0; gbc.gridy++; form.add(lblCat, gbc);
        gbc.gridx = 1; form.add(catBox, gbc);

        // scores area: Panel to group the score spinners
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; // Span two columns
        JPanel scorePanel = new JPanel(new GridLayout(2, 4, 12, 12));
        scorePanel.setBorder(BorderFactory.createTitledBorder("Scores (0-5)"));
        scorePanel.add(new JLabel("Creativity:")); scorePanel.add(s1);
        scorePanel.add(new JLabel("Technical:")); scorePanel.add(s2);
        scorePanel.add(new JLabel("Teamwork:")); scorePanel.add(s3);
        scorePanel.add(new JLabel("Presentation:")); scorePanel.add(s4);
        form.add(scorePanel, gbc);

        // Add overall score label below scores
        gbc.gridy++;
        form.add(overallLabel, gbc);

        panel.add(form, BorderLayout.CENTER);

        // Buttons: Update and Remove actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton updateBtn = new JButton("Update"); updateBtn.setPreferredSize(new Dimension(120, 36));
        JButton removeBtn = new JButton("Remove"); removeBtn.setPreferredSize(new Dimension(120, 36));
        actions.add(updateBtn); actions.add(removeBtn);
        panel.add(actions, BorderLayout.SOUTH);

        // Judge restrictions: Disable editing of non-score fields and the remove button
        boolean isJudge = role.equalsIgnoreCase("Judge");
        if (isJudge) {
            nameField.setEditable(false);
            uniField.setEditable(false);
            catBox.setEnabled(false);
            removeBtn.setEnabled(false);
            // visually grey out disabled fields
            nameField.setBackground(new Color(238, 238, 238));
            uniField.setBackground(new Color(238, 238, 238));
            catBox.setBackground(new Color(238, 238, 238));
        }

        // Event listeners
        // Load button action: Fetch team data and populate fields
        loadBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Team t = teamList.getTeamByID(id);
                if (t == null) { JOptionPane.showMessageDialog(this, "Team not found."); return; }

                // Populate fields with team data
                nameField.setText(t.getTeamName());
                uniField.setText(t.getUniversity());
                catBox.setSelectedItem(t.getCategory());
                int[] sc = t.getScoreArray();
                s1.setValue(sc[0]); s2.setValue(sc[1]); s3.setValue(sc[2]); s4.setValue(sc[3]);
                overallLabel.setText(String.format("Overall: %.2f", t.getOverallScore()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid ID number.");
            }
        });

        // Update button action: Save changes to team data
        updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Team t = teamList.getTeamByID(id);
                if (t == null) { JOptionPane.showMessageDialog(this, "Team not found."); return; }

                // Non-Judges update all fields; Judges only update scores
                if (!isJudge) {
                    t.setTeamName(nameField.getText().trim());
                    t.setUniversity(uniField.getText().trim());
                    t.setCategory((String) catBox.getSelectedItem());
                }
                // Update scores from spinners
                int[] scores = {(Integer) s1.getValue(), (Integer) s2.getValue(), (Integer) s3.getValue(), (Integer) s4.getValue()};
                t.setScores(scores);

                teamList.saveToCSV("HackathonTeams.csv"); // Save changes
                refreshTable(); refreshStats(); // Update displays
                JOptionPane.showMessageDialog(this, "Updated.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
            }
        });

        // Remove button action: Delete the team
        removeBtn.addActionListener(e -> {
            if (isJudge) return; // judges cannot remove
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Team t = teamList.getTeamByID(id);
                if (t == null) { JOptionPane.showMessageDialog(this, "Team not found."); return; }
                // Confirmation before removal
                int c = JOptionPane.showConfirmDialog(this, "Remove team " + t.getTeamName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    teamList.removeTeam(t); // Remove the team
                    teamList.saveToCSV("HackathonTeams.csv"); // Save changes
                    refreshTable(); refreshStats(); // Update displays
                    JOptionPane.showMessageDialog(this, "Removed.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid ID.");
            }
        });

        return panel;
    }

    // ---------------- Add Team Panel ----------------
    // Creates the panel for adding a new team (visible only to modifying roles)
    private JPanel createAddTeamPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        // Team details fields, pre-filling ID with the next available ID
        JTextField idField = new JTextField("" + teamList.generateNextTeamID());
        idField.setEditable(false); idField.setPreferredSize(new Dimension(220, 30));
        JTextField nameField = new JTextField(); nameField.setPreferredSize(new Dimension(220, 30));
        JTextField uniField = new JTextField(); uniField.setPreferredSize(new Dimension(220, 30));
        JComboBox<String> catBox = new JComboBox<>(new String[]{"Cybersecurity","Artificial Intelligence","Web Development","Data Science","Cloud Computing","Sustainability Tech"});
        catBox.setPreferredSize(new Dimension(220, 30));

        // Score spinners (default to 0)
        JSpinner s1 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s2 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s3 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        JSpinner s4 = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        Dimension spinnerSize = new Dimension(72, 28);
        s1.setPreferredSize(spinnerSize); s2.setPreferredSize(spinnerSize); s3.setPreferredSize(spinnerSize); s4.setPreferredSize(spinnerSize);

        // Layout fields
        gbc.gridx = 0; form.add(new JLabel("Team ID:"), gbc); gbc.gridx = 1; form.add(idField, gbc);
        gbc.gridy++; gbc.gridx = 0; form.add(new JLabel("Team Name:"), gbc); gbc.gridx = 1; form.add(nameField, gbc);
        gbc.gridy++; gbc.gridx = 0; form.add(new JLabel("University:"), gbc); gbc.gridx = 1; form.add(uniField, gbc);
        gbc.gridy++; gbc.gridx = 0; form.add(new JLabel("Category:"), gbc); gbc.gridx = 1; form.add(catBox, gbc);

        // Score panel layout
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel scorePanel = new JPanel(new GridLayout(2, 4, 12, 12));
        scorePanel.setBorder(BorderFactory.createTitledBorder("Scores (0-5)"));
        scorePanel.add(new JLabel("Creativity:")); scorePanel.add(s1);
        scorePanel.add(new JLabel("Technical:")); scorePanel.add(s2);
        scorePanel.add(new JLabel("Teamwork:")); scorePanel.add(s3);
        scorePanel.add(new JLabel("Presentation:")); scorePanel.add(s4);
        form.add(scorePanel, gbc);

        panel.add(form, BorderLayout.CENTER);

        // Action buttons (Add and Clear)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton addBtn = new JButton("Add Team"); addBtn.setPreferredSize(new Dimension(120, 36));
        JButton clearBtn = new JButton("Clear"); clearBtn.setPreferredSize(new Dimension(120, 36));
        actions.add(clearBtn); actions.add(addBtn);
        panel.add(actions, BorderLayout.SOUTH);

        // Add button action: Create and register the new team
        addBtn.addActionListener(e -> {
            try {
                int id = teamList.generateNextTeamID();
                String name = nameField.getText().trim();
                String uni = uniField.getText().trim();
                String cat = (String) catBox.getSelectedItem();
                // Input validation
                if (name.isEmpty() || uni.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name and University required.");
                    return;
                }
                int[] scores = {(Integer)s1.getValue(), (Integer)s2.getValue(), (Integer)s3.getValue(), (Integer)s4.getValue()};

                Team t;
                // Instantiate specific Team subclasses based on category for special score calculation
                switch (cat.toLowerCase()) {
                    case "cybersecurity":
                        t = new CyberTeam(id, name, uni, scores);
                        break;
                    case "artificial intelligence":
                        t = new AITeam(id, name, uni, scores);
                        break;
                    default:
                        // Fallback: Create an anonymous subclass of Team with a default overall score calculation (simple average)
                        Category fallbackCat = new Category(0, cat, "General Category");
                        t = new Team(id, name, uni, fallbackCat, scores) {
                            @Override public double getOverallScore() {
                                return Arrays.stream(getScoreArray()).average().orElse(0.0);
                            }
                        };
                        break;
                }

                if (!teamList.registerTeam(t)) {
                    JOptionPane.showMessageDialog(this, "Team ID or team+category already exists.");
                    return;
                }
                teamList.saveToCSV("HackathonTeams.csv"); // Save new team
                refreshTable(); refreshStats(); // Update displays
                JOptionPane.showMessageDialog(this, "Added team: " + name);

                // Reset fields for next entry
                idField.setText("" + teamList.generateNextTeamID());
                nameField.setText(""); uniField.setText("");
                s1.setValue(0); s2.setValue(0); s3.setValue(0); s4.setValue(0);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding team: " + ex.getMessage());
            }
        });

        // Clear button action: Reset input fields
        clearBtn.addActionListener(e -> {
            nameField.setText(""); uniField.setText("");
            s1.setValue(0); s2.setValue(0); s3.setValue(0); s4.setValue(0);
        });

        return panel;
    }

    // ---------------- Registration Panel ----------------
    // Creates the panel for competitors to register a new team (no scores initially)
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(new EmptyBorder(8,8,8,8));

        // Form for input fields
        JPanel form = new JPanel(new GridLayout(3,2,8,8));
        JTextField nameField = new JTextField();
        JTextField uniField = new JTextField();
        JComboBox<String> catBox = new JComboBox<>(new String[]{"Cybersecurity","Artificial Intelligence","Web Development","Data Science","Cloud Computing","Sustainability Tech"});

        form.add(new JLabel("Team Name:")); form.add(nameField);
        form.add(new JLabel("University:")); form.add(uniField);
        form.add(new JLabel("Category:")); form.add(catBox);

        panel.add(form, BorderLayout.NORTH);

        // Text area to display registration result/messages
        JTextArea result = new JTextArea(6, 40);
        result.setEditable(false);
        result.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(new JScrollPane(result), BorderLayout.CENTER);

        // Registration button
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton registerBtn = new JButton("Register (assign ID)");
        actions.add(registerBtn);
        panel.add(actions, BorderLayout.SOUTH);

        // Register button action: Create and register the new team with blank scores
        registerBtn.addActionListener(e -> {
            String teamName = nameField.getText().trim();
            String uni = uniField.getText().trim();
            String cat = (String) catBox.getSelectedItem();
            // Input validation
            if (teamName.isEmpty() || uni.isEmpty()) {
                result.setText("Team name and university required.");
                return;
            }
            int id = teamList.generateNextTeamID(); // Get next available ID
            int[] blank = {0,0,0,0}; // Initial blank scores

            Team t;
            // Instantiate specific Team subclasses based on category
            switch (cat.toLowerCase()) {
                case "cybersecurity": t = new CyberTeam(id, teamName, uni, blank); break;
                case "artificial intelligence": t = new AITeam(id, teamName, uni, blank); break;
                default:
                    // Fallback to anonymous subclass for default scoring
                    Category fallbackCat = new Category(0, cat, "General Category");
                    t = new Team(id, teamName, uni, fallbackCat, blank) {
                        @Override public double getOverallScore() { return Arrays.stream(getScoreArray()).average().orElse(0.0); }
                    }; break;
            }

            if (!teamList.registerTeam(t)) {
                result.setText("Registration failed (duplicate).");
                return;
            }
            teamList.saveToCSV("HackathonTeams.csv"); // Save registered team
            teamList.saveReport("HackathonReport.txt"); // Save report
            refreshTable(); refreshStats(); // Update displays
            result.setText("Registered. Assigned Team Number: " + id);
            // Clear input fields
            nameField.setText(""); uniField.setText("");
        });

        return panel;
    }

    // ---------------- Leaderboard Panel ----------------
    // Creates the panel for displaying the sorted leaderboard
    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Top control panel for category selection and refresh button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        lbCatBox = new JComboBox<>(new String[]{
                "All","Cybersecurity","Artificial Intelligence","Web Development",
                "Data Science","Cloud Computing","Sustainability Tech"
        });
        JButton refreshBtn = new JButton("Refresh Leaderboard");
        top.add(new JLabel("Category:")); top.add(lbCatBox); top.add(refreshBtn);
        panel.add(top, BorderLayout.NORTH);

        // Leaderboard table setup
        String[] cols = {"Rank", "Team ID", "Team Name", "University", "Category", "Overall Score"};
        lbModel = new DefaultTableModel(cols, 0) {
            // Make cells non-editable
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        lbTable = new JTable(lbModel);
        lbTable.setFillsViewportHeight(true);
        lbTable.setRowHeight(26);
        lbTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Apply custom renderer for alternate row colors
        lbTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) setBackground(new Color(183, 212, 255));
                else setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 255));
                setBorder(null);
                return this;
            }
        });

        panel.add(new JScrollPane(lbTable), BorderLayout.CENTER);
        // Action listener to reload leaderboard when button is pressed or category changes
        refreshBtn.addActionListener(e -> loadLeaderboard());
        // Listener to reload leaderboard when the category dropdown selection changes
        lbCatBox.addActionListener(e -> loadLeaderboard());

        return panel;
    }

    // Method to load/refresh the leaderboard data
    private void loadLeaderboard() {
        if (lbTable == null || lbModel == null) return;
        String cat = (String) lbCatBox.getSelectedItem(); // Get selected category
        lbModel.setRowCount(0); // Clear existing rows

        List<Team> teams = new ArrayList<>(teamList.getAllTeams());
        // Filter teams by category if "All" is not selected
        if (!"All".equalsIgnoreCase(cat)) teams.removeIf(t -> !t.getCategory().equalsIgnoreCase(cat));

        // Sort teams by Overall Score in descending order (highest score first)
        teams.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));

        // Populate the leaderboard table
        int rank = 1;
        for (Team t : teams) {
            lbModel.addRow(new Object[]{
                    rank++, t.getTeamID(), t.getTeamName(), t.getUniversity(),
                    t.getCategory(), String.format("%.2f", t.getOverallScore())
            });
        }
    }

    // ---------------- Statistics Panel ----------------
    // Creates the panel to display general statistics and allow team detail search
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Search functionality panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Enter Team ID:");
        JTextField searchField = new JTextField(8);
        JButton searchButton = new JButton("Show Short Details");
        searchPanel.add(searchLabel); searchPanel.add(searchField); searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Text area for general statistics display
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        // Result label (currently unused but kept for potential future use)
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(resultLabel, BorderLayout.SOUTH);

        // Search button action: Search for team by ID and display short details
        searchButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(searchField.getText().trim());
                Team team = teamList.getTeamByID(id);
                if (team != null) {
                    // Display short details in a dialog
                    JOptionPane.showMessageDialog(panel, team.getShortDetails(), "Team Short Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Team ID not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid numeric Team ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    // ---------------- Refresh Stats ----------------
    // Method to calculate and display general hackathon statistics
    private void refreshStats() {
        int total = teamList.getAllTeams().size(); // Get total number of teams
        // Calculate team count per category
        Map<String, Long> catCounts = new LinkedHashMap<>();
        for (Team t : teamList.getAllTeams()) catCounts.put(t.getCategory(), catCounts.getOrDefault(t.getCategory(), 0L) + 1);

        // Format statistics into a string
        StringBuilder sb = new StringBuilder();
        sb.append("Total teams: ").append(total).append("\n\n");
        sb.append("Teams per Category:\n");
        for (Map.Entry<String, Long> e : catCounts.entrySet()) sb.append(String.format("  %s: %d\n", e.getKey(), e.getValue()));

        // Update the statistics text area
        statsArea = (statsArea == null) ? new JTextArea() : statsArea;
        statsArea.setText(sb.toString());

        // Also ensure the leaderboard is updated
        loadLeaderboard();
    }
}

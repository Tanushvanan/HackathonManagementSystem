package manager.gui;

import manager.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginGUI - role selector. Adds "Public" and "Competitor" options.
 */
public class LoginGUI extends JFrame {

    private TeamList list;
    private JComboBox<String> roleComboBox;
    private Map<String, Staff> staffMap;

    public LoginGUI(TeamList list, Admin admin, Judge judge, RegistrationClerk clerk, Organizer organizer) {
        super("Hackathon System Login");
        this.list = list;

        staffMap = new HashMap<>();
        if (admin != null) staffMap.put("Admin", admin);
        if (judge != null) staffMap.put("Judge", judge);
        if (clerk != null) staffMap.put("Registration Clerk", clerk);
        if (organizer != null) staffMap.put("Organizer", organizer);

        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titleLabel = new JLabel("Hackathon System — Select Role", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        centerPanel.add(titleLabel);

        // build roles list + add Public and Competitor
        String[] roles = staffMap.keySet().toArray(new String[0]);
        String[] extended = new String[roles.length + 2];
        System.arraycopy(roles, 0, extended, 0, roles.length);
        extended[roles.length] = "Public";
        extended[roles.length + 1] = "Competitor";

        roleComboBox = new JComboBox<>(extended);
        centerPanel.add(roleComboBox);

        JButton loginButton = new JButton("Launch System");
        loginButton.setBackground(new Color(40, 116, 166));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> attemptLogin());
        centerPanel.add(loginButton);

        add(centerPanel, BorderLayout.CENTER);

        JLabel tip = new JLabel("Pick a role to open the system with the appropriate access.", SwingConstants.CENTER);
        tip.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        add(tip, BorderLayout.SOUTH);
    }

    private void attemptLogin() {
        String selected = (String) roleComboBox.getSelectedItem();
        Staff staff = staffMap.get(selected);

        if ("Public".equalsIgnoreCase(selected) || "Competitor".equalsIgnoreCase(selected)) {
            HackathonGUI gui = new HackathonGUI(list, null, selected);
            gui.setVisible(true);
            this.dispose();
        } else if (staff != null) {
            HackathonGUI gui = new HackathonGUI(list, staff, selected);
            gui.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Select a valid role.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

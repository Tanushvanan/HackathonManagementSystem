package manager;

import manager.gui.LoginGUI;
import javax.swing.*;

public class main {
    public static void main(String[] args) {

        TeamList list = new TeamList();
        list.loadFromCSV("HackathonTeams.csv");

        // Create staff objects with default IDs and Names
        Admin admin = new Admin(1, new Name("Default", "Admin"));
        Judge judge = new Judge(2, new Name("Default", "Judge"));
        RegistrationClerk clerk = new RegistrationClerk(3, new Name("Default", "Clerk"));
        Organizer organizer = new Organizer(4, new Name("Default", "Organizer"));

        SwingUtilities.invokeLater(() -> {
            new LoginGUI(list, admin, judge, clerk, organizer).setVisible(true);
        });
    }
}

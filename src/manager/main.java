package manager;

import manager.gui.HackathonGUI;
import javax.swing.*;

// This class acts as the HackathonManager/Entry Point
public class main { 

    public static void main(String[] args) {

        TeamList list = new TeamList();

        // loading CSV on startup (Stage 5 requirement)
        list.loadFromCSV("HackathonTeams.csv");

        // Stage 6 requirement: GUI opened by the manager class
        SwingUtilities.invokeLater(() -> {
            new HackathonGUI(list).setVisible(true);
        });
    }
}

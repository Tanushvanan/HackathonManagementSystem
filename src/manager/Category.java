package manager;

import java.util.ArrayList;

public class Category {

    private int categoryID;
    private String categoryName;
    private String description;

    private ArrayList<Team> teams = new ArrayList<>();

    public Category(int categoryID, String categoryName, String description) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.description = description;
    }

    public void addTeam(Team t) { teams.add(t); }
    public Team getTeam(int index) { return teams.get(index); }
    
    // 🌟 ADDED: Getter for categoryName
    public String getCategoryName() { return categoryName; }
    
    // 🌟 ADDED: Setter to support updates in the GUI
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
package manager;

public class Score {

    private Judge judge;
    private Team team;

    private int creativity;
    private int technical;
    private int teamwork;
    private int presentation;

    public Score(Judge judge, Team team, int creativity, int technical, int teamwork, int presentation) {
        this.judge = judge;
        this.team = team;

        this.creativity = creativity;
        this.technical = technical;
        this.teamwork = teamwork;
        this.presentation = presentation;
    }

    public double calculateOverall() {
        return (creativity + technical + teamwork + presentation) / 4.0;
    }
}

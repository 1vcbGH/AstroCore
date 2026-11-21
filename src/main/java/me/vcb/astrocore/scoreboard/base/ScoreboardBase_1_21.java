package me.vcb.astrocore.scoreboard.base;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardBase_1_21 {

    protected Player player;
    protected final Scoreboard scoreboard;

    public ScoreboardBase_1_21(Player player, Scoreboard scoreboard) {
        this.player = player;
        this.scoreboard = scoreboard;
    }

    protected void updateTeam(String name, String prefix, String suffix) {
        Team team = this.scoreboard.getTeam(name);
        if (team == null) return;

        team.setPrefix(prefix);
        team.setSuffix(suffix);
    }

    protected void setScore(String entry, int value) {
        Objective obj = this.scoreboard.getObjective("astrocore");
        if (obj == null) return;

        Score score = obj.getScore(entry);
        score.setScore(value);
    }

    @SuppressWarnings("deprecation")
    protected void resetScore(String entry) {
        this.scoreboard.resetScores(entry);
    }

    // Tab list methods — no longer needed in 1.21
    protected void createPlayerInfo(Object gameProfile) { }

    protected void updateDisplayName(Object gameProfile, String displayName) { }

    protected void removePlayerInfo(Object gameProfile) { }
}

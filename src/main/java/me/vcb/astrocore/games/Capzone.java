package me.vcb.astrocore.games;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.StringUtils.FormatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Capzone {

    private final Cuboid cuboid;

    @Setter private int time;
    private final List<String> players;

    public Capzone(Cuboid cuboid, int time) {
        this.cuboid = cuboid;
        this.time = time;
        // CopyOnWriteArrayList: async tick reads (hasNoPlayers/getCapper) safely
    // while main thread event handlers write (addPlayer/removePlayer)
    this.players = new CopyOnWriteArrayList<>();
    }

    public boolean hasNoPlayers() {
        return this.players.isEmpty();
    }

    public void addPlayer(Player player) {
        this.players.add(player.getName());
    }

    public void removePlayer(Player player) {
        this.players.remove(player.getName());
    }

    public Player getNextCapper() {
        return Bukkit.getPlayer(this.players.get(1));
    }

    public Player getCapper() {
        return Bukkit.getPlayer(this.getCapperName());
    }

    public String getCapperName() {
        return this.players.get(0);
    }

    public boolean isCapper(Player player) {
        return this.getCapperName().equals(player.getName());
    }

    public int decreaseTime() {
        return --this.time;
    }

    public String getTimeLeft() {
        return StringUtils.formatTime(this.time, FormatType.SECONDS_TO_MINUTES);
    }
}

package me.vcb.astrocore.hologram.impl;

import lombok.Getter;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.hologram.Hologram;
import me.vcb.astrocore.hologram.type.LeaderboardHologramType;
import org.bukkit.Location;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class LeaderboardHologram extends Hologram {

    private LeaderboardHologramType leaderboardType;
    private final transient AtomicBoolean update;

    public LeaderboardHologram() {
        this.update = new AtomicBoolean();
    }

    public LeaderboardHologram(int id, Location location, LeaderboardHologramType leaderboardType) {
        super(id, location);
        this.leaderboardType = leaderboardType;
        this.update = new AtomicBoolean();
        this.createHologramLines();
    }

    public void updateHologramLines() {
        // offset = header lines before entries
        int offset = this.leaderboardType.getHeader().size();
        int index = 1;

        for (UuidCacheEntry<Integer> entry : this.leaderboardType.getLeaderboard()) {
            this.updateMessage(offset++, this.leaderboardType.getLineFormat()
                .replace("<number>", String.valueOf(index++))
                .replace("<player>", entry.getName())
                .replace("<value>", this.leaderboardType.formatValue(entry.getValue())));
        }

        this.update.set(false);
    }

    @Override
    public void createHologramLines() {
        this.entries.clear();
        Location lineLocation = this.location.clone();

        // Header (includes blank spacer line from language.yml)
        for (String line : this.leaderboardType.getHeader()) {
            this.addEntry(line, lineLocation = this.getLineLocation(lineLocation));
        }

        // Entries
        int index = 1;
        for (UuidCacheEntry<Integer> entry : this.leaderboardType.getLeaderboard()) {
            lineLocation = this.getLineLocation(lineLocation);
            this.addEntry(this.leaderboardType.getLineFormat()
                .replace("<number>", String.valueOf(index++))
                .replace("<player>", entry.getName())
                .replace("<value>", this.leaderboardType.formatValue(entry.getValue())), lineLocation);
        }

        // Empty slots
        while (index <= 10) {
            lineLocation = this.getLineLocation(lineLocation);
            this.addEntry(Language.HOLOGRAM_EMPTY_LINE_FORMAT
                .replace("<number>", String.valueOf(index++)), lineLocation);
        }

        // Footer
        for (String line : this.leaderboardType.getFooter()) {
            this.addEntry(line, lineLocation = this.getLineLocation(lineLocation));
        }
    }
}

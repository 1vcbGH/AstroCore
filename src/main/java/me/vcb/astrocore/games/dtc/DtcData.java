package me.vcb.astrocore.games.dtc;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class DtcData {

    private Location location;

    private transient int breaksLeft;
    private transient long startTime;

    int decreaseBreaks() {
        return --this.breaksLeft;
    }
}

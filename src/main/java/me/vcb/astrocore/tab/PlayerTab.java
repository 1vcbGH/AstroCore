package me.vcb.astrocore.tab;

import org.bukkit.entity.Player;

public interface PlayerTab {

    Player getPlayer();

    void unregister();

    void set(int index, String line);

    /**
     * Called after all set() calls are done for one update cycle.
     * Implementations use this to send the accumulated content to the client.
     */
    default void flush() { }
}

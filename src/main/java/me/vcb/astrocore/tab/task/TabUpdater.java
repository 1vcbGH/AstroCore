package me.vcb.astrocore.tab.task;

import me.vcb.astrocore.tab.PlayerTab;
import me.vcb.astrocore.tab.module.TabModule;

public interface TabUpdater extends Runnable {

    void cancel();

    void initialSet(PlayerTab tab);

    void registerTabModule(TabModule module);

    /** Re-reads tab.yml and re-applies to all online players (used on reload). */
    void reload();
}

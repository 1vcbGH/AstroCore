package me.vcb.astrocore.tab.module;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.tab.PlayerTab;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public abstract class TabModule {

    protected final boolean enabled;
    protected final int startSlot;

    public TabModule(String configSection) {
        ConfigFile tabConfig = AstroCore.getInstance().getTabFile();
        ConfigurationSection section = tabConfig.getSection(configSection);

        this.enabled = section.getBoolean("ENABLED");
        this.startSlot = section.getInt("START_SLOT");

        this.loadAdditionalData(section);
    }

    public abstract void apply(PlayerTab tab);

    public void loadAdditionalData(ConfigurationSection section) {

    }
}

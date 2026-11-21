package me.vcb.astrocore.abilities.type;

import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.EnderPearlTimer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class SecondChanceAbility extends AbilityItem {

    public SecondChanceAbility(ConfigFile config) {
        super(AbilityType.SECOND_CHANCE, "SECOND_CHANCE", config);
    }

    @Override
    protected boolean onItemClick(Player player, PlayerInteractEvent event) {
        EnderPearlTimer enderPearlTimer = TimerManager.getInstance().getEnderPearlTimer();

        if(!enderPearlTimer.isActive(player)) {
            player.sendMessage(Language.ABILITIES_PREFIX + Language.ABILITIES_SECOND_CHANCE_NOT_ON_COOLDOWN);
            return false;
        }

        enderPearlTimer.cancel(player);

        event.setCancelled(true);
        return true;
    }
}

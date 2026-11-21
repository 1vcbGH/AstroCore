package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.scoreboard.HomeTimer;
import me.vcb.astrocore.utils.LocationUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand extends SubCommand {

    public HomeCommand() {
        super("home", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(faction.getHome() == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_NOT_SET);
            return;
        }

        if(!Config.PVP_PROTECTION_CAN_ENTER_OWN_CLAIM && TimerManager.getInstance().getPvpProtTimer().isActive(player)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_CANNOT_TELEPORT_WITH_PVP_TIMER);
            return;
        }

        if(TimerManager.getInstance().getCombatTagTimer().isActive(player)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_CANNOT_TELEPORT_IN_COMBAT);
            return;
        }

        Faction factionAt = ClaimManager.getInstance().getFactionAt(player);

        if(factionAt.isSafezone()) {
            LocationUtils.teleportWithChunkLoad(player, faction.getHome());
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_TELEPORTED);
            return;
        }

        boolean isEnemyTerritory = faction != factionAt && factionAt instanceof PlayerFaction;

        if(Config.DENY_HOME_TELEPORT_FROM_ENEMY_TERRITORY && isEnemyTerritory) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_CANNOT_TELEPORT_IN_ENEMY_TERRITORY);
            return;
        }

        HomeTimer timer = TimerManager.getInstance().getHomeTimer();

        if(timer.isActive(player)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_CANNOT_TELEPORT_ALREADY_TELEPORTING);
            return;
        }

        int delay = isEnemyTerritory
            ? Config.HOME_WARMUP_ENEMY_TERRITORY
            : Config.HOME_WARMUPS.get(player.getWorld().getEnvironment());

        // Home-warmup upgrade: -1s per level (floor 1s).
        delay = Math.max(1, delay - faction.getUpgradeHomeWarmup());

        timer.activate(player, delay, faction.getHome());

        player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_HOME_TASK_STARTED
            .replace("<seconds>", String.valueOf(delay)));
    }
}

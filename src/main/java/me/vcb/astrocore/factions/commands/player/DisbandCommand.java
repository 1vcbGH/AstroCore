package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisbandCommand extends SubCommand {

    public DisbandCommand() {
        super("disband", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(faction.getMember(player).getRole() != Role.LEADER) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_MUST_BE_LEADER);
            return;
        }

        if(faction.isDisqualified()) {
            player.sendMessage(Language.FACTION_PREFIX + "&cEste equipo ha sido &l§m§rdescalificado&c y no puede disbandearse.");
            return;
        }

        if(!Config.FACTION_DISBAND_WHILE_FROZEN && faction.isFrozen()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CANNOT_DISBAND_WHILE_REGENERATING);
            return;
        }

        if(faction.isRaidable() && !AstroCore.getInstance().getEotwHandler().isActive()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_DISBAND_RAIDABLE_DENY);
            return;
        }

        if(!FactionsManager.getInstance().disbandFaction(faction.getId(), player)) return;

        Messages.sendMessage(Language.FACTIONS_DISBANDED.replace("<player>", player.getName()).replace("<name>", faction.getName()));
    }
}

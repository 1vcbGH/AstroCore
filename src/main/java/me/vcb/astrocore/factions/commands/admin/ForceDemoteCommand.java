package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionPlayer;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class ForceDemoteCommand extends SubCommand {

    public ForceDemoteCommand() {
        super("forcedemote", "astrocore.factions.forcedemote");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FORCE_DEMOTE_USAGE);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if(!this.checkOfflinePlayer(sender, target, args[0])) return;

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(target.getUniqueId());

        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION.replace("<player>", target.getName()));
            return;
        }

        FactionPlayer targetPlayer = faction.getMember(target);

        if(targetPlayer.getRole() == Role.LEADER) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FORCE_DEMOTE_CANNOT_DEMOTE_LEADER);
            return;
        }

        if(targetPlayer.getRole().getDemote() == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FORCE_DEMOTE_MIN_DEMOTE);
            return;
        }

        targetPlayer.setRole(targetPlayer.getRole().getDemote());

        if(Config.TAB_ENABLED) {
            AstroCore.getInstance().getTabManager().updateFactionPlayerList(faction);
        }

        sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FORCE_DEMOTED_SENDER.replace("<player>",
        target.getName()).replace("<role>", targetPlayer.getRole().getName()));

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FORCE_DEMOTED_FACTION.replace("<player>",
        target.getName()).replace("<role>", targetPlayer.getRole().getName()));
    }
}

package me.vcb.astrocore.factions.commands.player;

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
import org.bukkit.entity.Player;

public class LeaderCommand extends SubCommand {

    public LeaderCommand() {
        super("leader", true);

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEADER_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        FactionPlayer fplayer = faction.getMember(player);

        if(fplayer.getRole() != Role.LEADER) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_MUST_BE_LEADER);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if(!this.checkOfflinePlayer(sender, target, args[0])) return;

        if(player == target) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEADER_ALREADY_LEADER);
            return;
        }

        FactionPlayer targetPlayer = faction.getMember(target);

        if(targetPlayer == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_OTHERS.replace("<player>", target.getName()));
            return;
        }

        fplayer.setRole(Role.CO_LEADER);
        targetPlayer.setRole(Role.LEADER);

        if(Config.TAB_ENABLED) {
            AstroCore.getInstance().getTabManager().updateFactionPlayerList(faction);
        }

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LEADER_LEADER_CHANGED
        .replace("<player>", player.getName()).replace("<target>", target.getName()));
    }
}

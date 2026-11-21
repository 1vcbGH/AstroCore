package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TellLocationCommand extends BaseCommand {

    public TellLocationCommand() {
        super("telllocation", Arrays.asList("tellloc", "tl"), "astrocore.telllocation", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(args.length > 0 && player.hasPermission("astrocore.telllocation.others")) {
            Player target = Bukkit.getPlayer(args[0]);

            if(!this.checkPlayer(sender, target, args[0])) return;

            PlayerFaction playerFaction = FactionsManager.getInstance().getPlayerFaction(player);
            PlayerFaction targetFaction = FactionsManager.getInstance().getPlayerFaction(target);

            if(playerFaction != targetFaction) {
                player.sendMessage(Language.FACTIONS_NOT_IN_FACTION_OTHERS.replace("<player>", target.getName()));
                return;
            }

            player.sendMessage(Language.TELL_LOCATION_MESSAGE
                .replace("<player>", target.getName())
                .replace("<world>", StringUtils.getWorldName(target.getLocation()))
                .replace("<location>", StringUtils.getLocationName(target.getLocation())));

            return;
        }

        faction.sendMessage(Language.TELL_LOCATION_MESSAGE
            .replace("<player>", player.getName())
            .replace("<world>", StringUtils.getWorldName(player.getLocation()))
            .replace("<location>", StringUtils.getLocationName(player.getLocation())));
    }
}
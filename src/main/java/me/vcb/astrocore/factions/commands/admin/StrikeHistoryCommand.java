package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.strike.StrikeEntry;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class StrikeHistoryCommand extends SubCommand {

    public StrikeHistoryCommand() {
        super("strikehistory", Arrays.asList("sh"), "astrocore.factions.strike");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage(Language.FACTION_PREFIX + "&cUso: /f strikehistory <faction>");
            return;
        }

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFactionByName(args[0]);
        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        List<StrikeEntry> strikes = faction.getStrikes();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&8&m                                              "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "  &c&lHistorial de strikes &8— &7" + faction.getName() +
            " &8(&c" + faction.getStrikeCount() + "/3&8)"));

        if(faction.isDisqualified()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "  &4&l⚠ &cDESCALIFICADA &8| &7Motivo: &e" + faction.getDisqualifyReason()));
        }

        sender.sendMessage("");

        if(strikes == null || strikes.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "  &7Sin strikes."));
        } else {
            for(int i = 0; i < strikes.size(); i++) {
                StrikeEntry s = strikes.get(i);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "  &c#" + (i + 1) + " &8| &7Motivo: &e" + s.getReason() +
                    " &8| &7Staff: &b" + s.getStaffName() + " &8| &7Fecha: &f" + s.getDate()));
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&8&m                                              "));
    }
}

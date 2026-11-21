package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StrikeRemoveCommand extends SubCommand {

    public StrikeRemoveCommand() {
        super("removestrike", "astrocore.factions.strike");
    }

    private static String t(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private PlayerFaction resolveFaction(String arg) {
        PlayerFaction f = FactionsManager.getInstance().getPlayerFactionByName(arg);
        if(f == null) f = FactionsManager.getInstance().getPlayerFaction(arg);
        return f;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage(t(Language.FACTION_PREFIX + "&cUso: /f removestrike <faction/jugador>"));
            return;
        }

        PlayerFaction faction = this.resolveFaction(args[0]);
        if(faction == null) {
            sender.sendMessage(t(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0])));
            return;
        }

        if(!faction.removeLastStrike()) {
            sender.sendMessage(t(Language.FACTION_PREFIX + "&cEsa faction no tiene strikes."));
            return;
        }

        sender.sendMessage(t(Language.FACTION_PREFIX + "&7Strike eliminado de &c" + faction.getName() +
            "&7. Strikes restantes: &c" + faction.getStrikeCount() + "/3&7."));

        faction.sendMessage(t(Language.FACTION_PREFIX + "&aSe ha eliminado un strike de tu faction. Strikes: &c" +
            faction.getStrikeCount() + "/3&a."));
    }
}

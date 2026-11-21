package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/** /f log — registro de acciones de la facción (depósitos, claims, kicks...). */
public class LogCommand extends SubCommand {

    public LogCommand() {
        super("log", java.util.List.of("logs", "registro"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);
        if (faction == null) {
            player.sendMessage(Color.translate("&cNo estás en una facción."));
            return;
        }

        List<String> entries = faction.getLogEntries();
        player.sendMessage(Color.translate("&6&lRegistro de Facción &7(últimas " + Math.min(20, entries.size()) + ")"));
        if (entries.isEmpty()) {
            player.sendMessage(Color.translate("&7Sin acciones registradas."));
            return;
        }
        int shown = 0;
        for (String e : entries) {
            if (shown++ >= 20) break;
            player.sendMessage(Color.translate("  " + e));
        }
    }
}

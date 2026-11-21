package me.vcb.astrocore.missions;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /misiones — menú de misiones diarias. /misiones reload (staff). */
public class MissionsCommand extends BaseCommand {

    public MissionsCommand() {
        super("misiones", java.util.List.of("missions", "mision"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("astrocore.staff")) {
            MissionsManager.getInstance().reload();
            sender.sendMessage(Color.translate("&aMisiones recargadas."));
            return;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage(Color.translate("&cSolo jugadores.")); return; }
        MissionsManager.getInstance().open(player);
    }
}

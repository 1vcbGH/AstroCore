package me.vcb.astrocore.battlepass;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /battlepass — menú del pase de temporada. /battlepass reload (staff). */
public class BattlePassCommand extends BaseCommand {

    public BattlePassCommand() {
        super("battlepass", java.util.List.of("bp", "pase"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("astrocore.staff")) {
            BattlePassManager.getInstance().reload();
            sender.sendMessage(Color.translate("&aBattlepass recargado."));
            return;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage(Color.translate("&cSolo jugadores.")); return; }
        BattlePassManager.getInstance().open(player);
    }
}

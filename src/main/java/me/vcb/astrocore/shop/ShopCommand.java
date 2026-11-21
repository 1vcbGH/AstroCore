package me.vcb.astrocore.shop;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /shop — tienda del servidor. /shop reload (staff). */
public class ShopCommand extends BaseCommand {

    public ShopCommand() {
        super("shop", java.util.List.of("tienda"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("astrocore.staff")) {
            ShopManager.getInstance().reload();
            sender.sendMessage(Color.translate("&aTienda recargada."));
            return;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage(Color.translate("&cSolo jugadores.")); return; }
        ShopManager.getInstance().openMain(player);
    }
}

package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class InventoryRestoreCommand extends BaseCommand {

    public InventoryRestoreCommand() {
        super("inventoryrestore", Arrays.asList("invrestore", "lastinv", "invrollback"), "astrocore.invrestore", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.INVENTORY_RESTORE_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        AstroCore.getInstance().getInventoryRestoreManager().openRestoreInventory((Player) sender, target);
    }
}

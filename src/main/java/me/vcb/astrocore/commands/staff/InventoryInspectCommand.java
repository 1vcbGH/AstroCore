package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class InventoryInspectCommand extends BaseCommand {

    public InventoryInspectCommand() {
        super("inventoryinspect", Arrays.asList("invinspect", "invi", "ii"), "astrocore.invinspect", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.INVENTORY_INSPECT_USAGE);
            return;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if(this.checkPlayer(player, target, args[0])) {
            AstroCore.getInstance().getStaffModeManager().inventoryInspect(player, target);
        }
    }
}

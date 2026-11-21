package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ClearInventoryCommand extends BaseCommand {

    public ClearInventoryCommand() {
        super("clearinventory", Arrays.asList("ci", "clearinv"), "astrocore.clearinventory");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            if(!this.checkConsoleSender(sender)) return;

            Player player = (Player) sender;
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.sendMessage(Language.PREFIX + Language.CLEARINVENTORY_MESSAGE_SELF);
            return;
        }

        if(!this.checkPermission(sender, "astrocore.clearinventory.others")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        target.getInventory().clear();
        target.getInventory().setArmorContents(null);

        target.sendMessage(Language.PREFIX + Language.CLEARINVENTORY_MESSAGE_SELF);
        sender.sendMessage(Language.PREFIX + Language.CLEARINVENTORY_MESSAGE_OTHERS.replace("<player>", target.getName()));
    }
}

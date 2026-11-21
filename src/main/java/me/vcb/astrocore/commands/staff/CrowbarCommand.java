package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrowbarCommand extends BaseCommand {

    public CrowbarCommand() {
        super("crowbar", "astrocore.crowbar");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Language.PREFIX + Language.CROWBAR_COMMAND_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if(!this.checkPlayer(sender, target, args[1])) return;

        target.getInventory().addItem(AstroCore.getInstance().getCrowbarHandler().getNewCrowbar());
        target.updateInventory();

        sender.sendMessage(Language.PREFIX + Language.CROWBAR_GAVE.replace("<player>", target.getName()));
        target.sendMessage(Language.PREFIX + Language.CROWBAR_RECEIVED);
    }
}

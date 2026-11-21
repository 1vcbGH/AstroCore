package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FilterCommand extends BaseCommand {

    public FilterCommand() {
        super("filter", "astrocore.filter", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            Language.FILTER_USAGE.forEach(sender::sendMessage);
            return;
        }

        if(args.length == 1) {
            switch(args[0].toLowerCase()) {
                case "list": {
                    AstroCore.getInstance().getFilterHandler().listFilter(player);
                    return;
                }
                case "toggle": {
                    AstroCore.getInstance().getFilterHandler().toggleFilter(player);
                    return;
                }
                case "clear": {
                    AstroCore.getInstance().getFilterHandler().clearFilter(player);
                    return;
                }
                default: {
                    Language.FILTER_USAGE.forEach(sender::sendMessage);
                    return;
                }
            }
        }

        switch(args[0].toLowerCase()) {
            case "add": {
                AstroCore.getInstance().getFilterHandler().addItem(player, args[1]);
                return;
            }
            case "remove": {
                AstroCore.getInstance().getFilterHandler().removeItem(player, args[1]);
                return;
            }
            default: Language.FILTER_USAGE.forEach(sender::sendMessage);
        }
    }
}

package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.kit.KitCategory;
import me.vcb.astrocore.kits.kit.KitData;
import org.bukkit.command.CommandSender;

public class KitCategoryCommand extends SubCommand {

    KitCategoryCommand() {
        super("category", "astrocore.kits.admin", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.KIT_PREFIX + "§cUso: /kit category <kit> <GRATUITO|GKIT>");
            return;
        }

        KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[0]);
        if(kit == null) {
            sender.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_DOESNT_EXISTS.replace("<kit>", args[0]));
            return;
        }

        KitCategory category = KitCategory.fromName(args[1]);
        if(category == null) {
            sender.sendMessage(Language.KIT_PREFIX + "§cCategoría inválida. Usá: GRATUITO o GKIT");
            return;
        }

        kit.setCategory(category);
        sender.sendMessage(Language.KIT_PREFIX + "§aKit §f" + kit.getName()
            + " §acategorizado como " + category.getDisplayName() + "§a.");
    }
}

package me.vcb.astrocore.games.loot.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.loot.LootData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LootEditCommand extends SubCommand {

    LootEditCommand() {
        super("edit", "astrocore.loot.edit", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_EDIT_USAGE);
            return;
        }

        LootData loot = AstroCore.getInstance().getLootManager().getLootByName(args[0]);

        if(loot == null) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_EXCEPTION_DOESNT_EXIST.replace("<loot>", args[0]));
            return;
        }

        AstroCore.getInstance().getLootManager().editLoot((Player) sender, loot);
    }
}

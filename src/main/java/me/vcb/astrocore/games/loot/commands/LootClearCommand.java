package me.vcb.astrocore.games.loot.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.utils.InventoryUtils;
import org.bukkit.command.CommandSender;

public class LootClearCommand extends SubCommand {

    LootClearCommand() {
        super("clear", "astrocore.loot.clear");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_CLEAR_USAGE);
            return;
        }

        LootData loot = AstroCore.getInstance().getLootManager().getLootByName(args[0]);

        if(loot == null) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_EXCEPTION_DOESNT_EXIST.replace("<loot>", args[0]));
            return;
        }

        loot.getInventory().clear();
        loot.setItems(InventoryUtils.getItemsFromInventory(loot.getInventory()));

        sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_CLEAR_CLEARED.replace("<loot>", loot.getName()));
    }
}

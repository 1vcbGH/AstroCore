package me.vcb.astrocore.games.loot.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.loot.LootData;
import org.bukkit.command.CommandSender;

public class LootSetAmountCommand extends SubCommand {

    LootSetAmountCommand() {
        super("setamount", "astrocore.loot.setamount");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_SET_AMOUNT_USAGE);
            return;
        }

        LootData loot = AstroCore.getInstance().getLootManager().getLootByName(args[0]);

        if(loot == null) {
            sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_EXCEPTION_DOESNT_EXIST.replace("<loot>", args[0]));
            return;
        }

        if(!this.checkNumber(sender, args[1])) return;

        loot.setAmount(Math.abs(Integer.parseInt(args[1])));

        sender.sendMessage(Language.LOOT_PREFIX + Language.LOOT_SET_AMOUNT_CHANGED
        .replace("<loot>", loot.getName()).replace("<amount>", args[1]));
    }
}

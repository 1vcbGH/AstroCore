package me.vcb.astrocore.games.conquest.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.games.loot.LootData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConquestLootCommand extends SubCommand {

    ConquestLootCommand() {
        super("loot", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        LootData loot = AstroCore.getInstance().getLootManager().getLootByName("Conquest");
        ((Player) sender).openInventory(loot.getInventory());
    }
}

package me.vcb.astrocore.games.loot.commands;

import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

import java.util.List;

public class LootCommandExecutor extends SubCommandExecutor {

    public LootCommandExecutor() {
        super("loot", null);

        this.setPrefix(Language.LOOT_PREFIX);

        this.addSubCommand(new LootClearCommand());
        this.addSubCommand(new LootEditCommand());
        this.addSubCommand(new LootListCommand());
        this.addSubCommand(new LootSetAmountCommand());
        this.addSubCommand(new LootViewCommand());
    }

    @Override
    protected List<String> getUsageMessage(CommandSender sender) {
        return sender.hasPermission("astrocore.loot.admin")
            ? Language.LOOT_COMMAND_USAGE_ADMIN
            : Language.LOOT_COMMAND_USAGE_PLAYER;
    }
}

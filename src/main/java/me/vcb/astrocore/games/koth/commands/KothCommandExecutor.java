package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

import java.util.List;

public class KothCommandExecutor extends SubCommandExecutor {

    public KothCommandExecutor() {
        super("kothadmin", java.util.List.of("kadmin"), null);

        this.setPrefix(Language.KOTH_PREFIX);

        this.addSubCommand(new KothAreaCommand());
        this.addSubCommand(new KothDebugCommand());
        this.addSubCommand(new KothCreateCommand());
        this.addSubCommand(new KothDeleteCommand());
        this.addSubCommand(new KothEndCommand());
        this.addSubCommand(new KothFactionPointsCommand());
        this.addSubCommand(new KothListCommand());
        this.addSubCommand(new KothLootCommand());
        this.addSubCommand(new KothSetTimeCommand());
        this.addSubCommand(new KothStartCommand());
        this.addSubCommand(new KothStartingTimeCommand());
        this.addSubCommand(new KothStopAllCommand());
        this.addSubCommand(new KothStopCommand());
        this.addSubCommand(new KothTeleportCommand());
    }

    @Override
    public List<String> getUsageMessage(CommandSender sender) {
        return sender.hasPermission("astrocore.koth.admin")
            ? Language.KOTH_COMMAND_USAGE_ADMIN
            : Language.KOTH_COMMAND_USAGE_PLAYER;
    }
}

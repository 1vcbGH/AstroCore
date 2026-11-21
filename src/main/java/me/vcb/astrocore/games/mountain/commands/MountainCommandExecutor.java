package me.vcb.astrocore.games.mountain.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class MountainCommandExecutor extends SubCommandExecutor {

    private final List<String> PLAYER_COMMAND_USAGE;

    public MountainCommandExecutor() {
        super("mountain", null);

        this.setPrefix(Language.MOUNTAIN_PREFIX);

        this.addSubCommand(new MountainCreateCommand());
        this.addSubCommand(new MountainDeleteCommand());
        this.addSubCommand(new MountainListCommand());
        this.addSubCommand(new MountainRespawnCommand());
        this.addSubCommand(new MountainTeleportCommand());
        this.addSubCommand(new MountainTimeCommand());
        this.addSubCommand(new MountainUpdateCommand());

        PLAYER_COMMAND_USAGE = Collections.singletonList(AstroCore
            .getInstance().getMountainManager().nextRespawnString());
    }

    @Override
    public List<String> getUsageMessage(CommandSender sender) {
        return sender.hasPermission("astrocore.mountain.admin")
            ? Language.MOUNTAIN_COMMAND_USAGE_ADMIN : PLAYER_COMMAND_USAGE;
    }
}

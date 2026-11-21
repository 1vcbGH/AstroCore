package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.factions.vault.FactionVaultManager;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f leadervault — cofre compartido entre los líderes de la facción. */
public class LeaderVaultCommand extends SubCommand {

    public LeaderVaultCommand() {
        super("leadervault", java.util.List.of("lvault", "cofrelideres"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        FactionVaultManager.getInstance().openLeader((Player) sender);
    }
}

package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.factions.vault.FactionVaultManager;
import me.vcb.astrocore.commands.manager.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /f vault — cofre compartido de la facción. */
public class VaultCommand extends SubCommand {

    public VaultCommand() {
        super("vault", java.util.List.of("cofre"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        FactionVaultManager.getInstance().openFaction((Player) sender);
    }
}

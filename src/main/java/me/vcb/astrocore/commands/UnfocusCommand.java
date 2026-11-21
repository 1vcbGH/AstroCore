package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnfocusCommand extends BaseCommand {

    public UnfocusCommand() {
        super("unfocus", true);

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CAPTAIN)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION.replace("<role>", Role.CAPTAIN.getName()));
            return;
        }

        if(faction.getFocusedPlayer() == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.UNFOCUS_NOT_FOCUSING);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(faction.getFocusedPlayer());

        faction.unfocusPlayer(target);
        player.sendMessage(Language.FACTION_PREFIX + Language.UNFOCUS_UNFOCUSED.replace("<player>", target.getName()));
    }
}
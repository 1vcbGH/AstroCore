package me.vcb.astrocore.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FocusCommand extends BaseCommand {

    public FocusCommand() {
        super("focus", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FOCUS_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CAPTAIN)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION
                .replace("<role>", Role.CAPTAIN.getName()));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        if(faction.getMember(target) != null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FOCUS_CANNOT_FOCUS);
            return;
        }

        if(faction.getFocusedPlayer() == target.getUniqueId()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FOCUS_ALREADY_FOCUSING
                .replace("<player>", target.getName()));
            return;
        }

        faction.focusPlayer(target);
        Language.FOCUS_FOCUSED.forEach(line -> faction.sendMessage(line.replace("<target>", target.getName())));
    }
}
package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FocusCommand extends SubCommand {

    public FocusCommand() {
        super("focus", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FOCUS_USAGE);
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

        PlayerFaction targetFaction = FactionsManager.getInstance().searchForFaction(args[0]);

        if(targetFaction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        if(faction == targetFaction || faction.isAlly(targetFaction)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FOCUS_CANNOT_FOCUS);
            return;
        }

        if(faction.isFocusing(targetFaction)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FOCUS_ALREADY_FOCUSING
                .replace("<faction>", targetFaction.getName()));
            return;
        }

        faction.focusFaction(targetFaction);
        Language.FACTIONS_FOCUS_FOCUSED.forEach(line -> faction.sendMessage(line.replace("<faction>", targetFaction.getName())));
    }
}

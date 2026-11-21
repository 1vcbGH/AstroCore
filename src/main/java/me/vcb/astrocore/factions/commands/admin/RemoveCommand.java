package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.factions.type.SystemFaction;
import org.bukkit.command.CommandSender;

public class RemoveCommand extends SubCommand {

    public RemoveCommand() {
        super("remove", "astrocore.factions.remove");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVE_USAGE);
            return;
        }

        Faction faction = FactionsManager.getInstance().getAnyFaction(args[0]);

        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        if(!(faction instanceof PlayerFaction) && faction.getClass() != SystemFaction.class) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVE_CANNOT_REMOVE_THIS_TYPE);
            return;
        }

        if(!FactionsManager.getInstance().disbandFaction(faction.getId(), sender)) return;

        sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVED_SENDER.replace("<faction>", faction.getDisplayName(sender)));

        if(faction instanceof PlayerFaction) {
            PlayerFaction playerFaction = (PlayerFaction) faction;
            playerFaction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_REMOVED_FACTION.replace("<player>", sender.getName()));
        }
    }
}

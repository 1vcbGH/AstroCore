package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.SystemFaction;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class AbilitiesCommand extends SubCommand {

    public AbilitiesCommand() {
        super("abilities", Collections.singletonList("ability"), "astrocore.factions.abilities");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_ABILITIES_USAGE);
            return;
        }

        Faction faction = FactionsManager.getInstance().getFactionByName(args[0]);

        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        if(!(faction instanceof SystemFaction)) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_ABILITIES_NOT_SYSTEM_FACTION);
            return;
        }

        SystemFaction systemFaction = (SystemFaction) faction;
        systemFaction.setAbilities(!systemFaction.isAbilities());

        sender.sendMessage(Language.FACTION_PREFIX + (systemFaction.isAbilities()
            ? Language.FACTIONS_ABILITIES_ENABLED
            : Language.FACTIONS_ABILITIES_DISABLED)
        .replace("<faction>", systemFaction.getDisplayName(sender)));
    }
}

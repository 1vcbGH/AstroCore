package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class JoinRandomCommand extends SubCommand {

    public JoinRandomCommand() {
        super("joinrandom", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(FactionsManager.getInstance().getPlayerFaction(player) != null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_ALREADY_IN_FACTION_SELF);
            return;
        }

        List<PlayerFaction> openFactions = FactionsManager.getInstance().getFactions().values().stream()
            .filter(f -> f instanceof PlayerFaction).map(f -> (PlayerFaction) f)
            .filter(f -> f.isOpen() && f.getMembers().size() < Config.FACTION_PLAYER_LIMIT && !f.isFrozen())
            .collect(Collectors.toList());

        if(openFactions.isEmpty()) {
            player.sendMessage(Language.FACTION_PREFIX + Color.translate("&eNo hay facciones abiertas disponibles."));
            return;
        }

        PlayerFaction faction = openFactions.get((int)(Math.random() * openFactions.size()));

        if(!FactionsManager.getInstance().joinFaction(player, faction)) return;

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_JOINED.replace("<player>", player.getName()));
    }
}

package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.handlers.leaderboard.type.FactionLeaderboardType;
import me.vcb.astrocore.handlers.leaderboard.type.LeaderboardType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.NavigableSet;

public class TopCommand extends SubCommand {

    public TopCommand() {
        super("top");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            if(sender instanceof Player p) {
                me.vcb.astrocore.utils.Tasks.sync(() ->
                    me.vcb.astrocore.factions.leaderboard.FactionLeaderboardGui.getInstance().open(p, 0));
                return;
            }
            sender.sendMessage(Language.PREFIX + Language.LEADERBOARDS_FACTION_COMMAND_USAGE);
            return;
        }

        LeaderboardType type = FactionLeaderboardType.getByName(args[0]);

        if(type == null) {
            sender.sendMessage(Language.PREFIX + Language.LEADERBOARDS_TYPE_DOESNT_EXIST
                .replace("<type>", args[0]));
            return;
        }

        NavigableSet<UuidCacheEntry<Integer>> leaderboard = type.getLeaderboard();

        if(leaderboard.isEmpty()) {
            sender.sendMessage(Language.PREFIX + Language.LEADERBOARDS_NO_LEADERBOARDS);
            return;
        }

        Player player = (Player) sender;
        String title = type.getTitle();
        String lineFormat = type.getLineFormat();

        int index = 1;

        sender.sendMessage(Language.LEADERBOARDS_COMMAND_HEADER);
        sender.sendMessage(title);

        for(UuidCacheEntry<Integer> entry : leaderboard) {
            Faction faction = FactionsManager.getInstance().getFactionByUuid(entry.getKey());

            String factionName = faction != null ? faction.getName(sender) : entry.getName();
            String clickTarget = faction != null ? faction.getName() : entry.getName();

            String displayName;
            String hoverText;

            if(faction instanceof PlayerFaction && ((PlayerFaction) faction).isDisqualified()) {
                PlayerFaction pf = (PlayerFaction) faction;
                displayName = "§m" + factionName;
                hoverText = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&cEste equipo ha sido descalificado\n" +
                    "&7Motivo: &e" + pf.getDisqualifyReason() + "\n" +
                    "&7Staff: &b" + pf.getDisqualifiedBy() + "\n" +
                    "&7Fecha: &f" + pf.getDisqualifyDate());
            } else {
                displayName = factionName;
                hoverText = Language.FACTIONS_SHOW_HOVER_TEXT.replace("<faction>", factionName);
            }

            ComponentBuilder message = new ComponentBuilder(lineFormat
                .replace("<number>", String.valueOf(index))
                .replace("<faction>", displayName)
                .replace("<value>", String.valueOf(entry.getValue())))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f show " + clickTarget));

            player.spigot().sendMessage(message.create());
            index++;
        }

        sender.sendMessage(Language.LEADERBOARDS_COMMAND_FOOTER);
    }
}

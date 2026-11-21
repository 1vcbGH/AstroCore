package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.rank.RankManager;
import me.vcb.astrocore.tags.ChatTag;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PerfilCommand extends BaseCommand {

    public PerfilCommand() {
        super("perfil", Arrays.asList("profile", "stats"), null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String targetName = args.length > 0 ? args[0] : sender.getName();
        Player online = Bukkit.getPlayer(targetName);

        if (online != null) {
            showProfile(sender, AstroCore.getInstance().getUserdataManager().getUserdata(online), true);
            return;
        }

        Tasks.async(() -> {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(targetName);
            Tasks.sync(() -> {
                if (ud == null) {
                    sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado."));
                    return;
                }
                showProfile(sender, ud, false);
            });
        });
    }

    private void showProfile(CommandSender sender, Userdata ud, boolean online) {
        if (ud == null) return;

        // K/D
        int kills  = ud.getKills();
        int deaths = ud.getDeaths();
        String kd  = deaths == 0 ? String.valueOf(kills)
            : String.format("%.2f", (double) kills / deaths);

        // Facción
        String factionStr = "&7Sin facción";
        Player p = Bukkit.getPlayer(ud.getUuid());
        if (p != null) {
            PlayerFaction pf = FactionsManager.getInstance().getPlayerFaction(p);
            if (pf != null) factionStr = "&e" + pf.getName();
        }

        // Rank / ELO
        String rankStr;
        if (RankManager.getInstance() != null && ud.getRank() != null) {
            rankStr = ud.getRank().getColoredName();
        } else {
            rankStr = "&7Sin rango";
        }

        // Tag activo
        ChatTag tag = ud.getActiveTag();
        String tagStr = tag != null ? tag.getDisplay() : "&7Ninguno";

        // Estado
        String statusStr = online ? "&a[En línea]" : "&c[Desconectado]";

        sender.sendMessage(Color.translate("&8&m─────────────────────────────────"));
        sender.sendMessage(Color.translate("  &6&l" + ud.getName() + " " + statusStr));
        sender.sendMessage(Color.translate("&8&m─────────────────────────────────"));
        sender.sendMessage(Color.translate("  &7Facción&8: " + factionStr));
        sender.sendMessage(Color.translate("  &7Rango&8: " + rankStr + "  &7ELO&8: &b" + ud.getElo()));
        sender.sendMessage(Color.translate("  &7Kills&8: &a" + kills + "  &7Muertes&8: &c" + deaths + "  &7K/D&8: &e" + kd));
        sender.sendMessage(Color.translate("  &7Racha&8: &e" + ud.getKillstreak() + "  &7Máx. racha&8: &6" + ud.getHighestKillstreak()));
        sender.sendMessage(Color.translate("  &7Balance&8: &6$" + ud.getBalance()));
        sender.sendMessage(Color.translate("  &7Tag&8: " + tagStr));
        sender.sendMessage(Color.translate("  &7KOTHs ganados&8: &b" + ud.getKothsCapped()));
        sender.sendMessage(Color.translate("  &7Bounties cobrados&8: &b" + ud.getBountiesCollected()));
        sender.sendMessage(Color.translate("&8&m─────────────────────────────────"));
    }
}

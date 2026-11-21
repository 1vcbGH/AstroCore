package me.vcb.astrocore.rank;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class RankCommand extends BaseCommand {

    public RankCommand() {
        super("rank", Collections.emptyList(), null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) { sender.sendMessage("§cSolo jugadores."); return; }
            Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata((Player) sender);
            if (ud != null) this.sendRankInfo(sender, ud);
            return;
        }

        switch (args[0].toLowerCase()) {

            case "tops":
            case "top":
            case "leaderboard":
            case "lb": {
                int limit = 10;
                if (args.length >= 2) {
                    try { limit = Math.min(50, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {}
                }
                RankManager.getInstance().sendGlobalLeaderboard(sender, limit);
                break;
            }

            case "info": {
                if (args.length < 2) {
                    if (!(sender instanceof Player)) { sender.sendMessage("§cSolo jugadores."); return; }
                    Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata((Player) sender);
                    if (ud != null) this.sendRankInfo(sender, ud);
                    return;
                }
                Userdata ud = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(args[1]);
                if (ud == null) { sender.sendMessage("§cJugador §e" + args[1] + " §cno encontrado."); return; }
                this.sendRankInfo(sender, ud);
                break;
            }

            default:
                this.sendHelp(sender);
        }
    }

    private void sendRankInfo(CommandSender sender, Userdata ud) {
        PlayerRank rank = ud.getRank() != null ? ud.getRank() : PlayerRank.fromElo(ud.getElo());
        PlayerRank next = rank.next();

        // Top-3 tag
        int pos = RankManager.getInstance().getTopPosition(ud.getUuid());
        String[] posLabels = {"§6[🏆 #1] ", "§7[🥈 #2] ", "§c[🥉 #3] "};
        String posLine = (pos >= 1 && pos <= 3) ? "  §7Top Global: " + posLabels[pos - 1] + "\n" : "";

        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("§6§l  Rango de §e" + ud.getName());
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("  §7Rango: " + rank.getColoredName());
        sender.sendMessage("  §7ELO: §e" + ud.getElo());
        sender.sendMessage("  §7Kills: §e" + ud.getKills() + "  §7Muertes: §e" + ud.getDeaths());
        if (pos >= 1 && pos <= 3) sender.sendMessage("  §7Posición global: " + posLabels[pos - 1]);
        if (next != null) {
            int needed = Math.max(0, next.getEloRequired() - ud.getElo());
            sender.sendMessage("  §7Siguiente: " + next.getColoredName() + " §8(§7faltan §e" + needed + " ELO§8)");
        } else {
            sender.sendMessage("  §6¡Rango máximo! §c✦");
        }
        sender.sendMessage("§8§m                                        ");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("§6§l  /rank — Sistema de rangos");
        sender.sendMessage("§e  /rank              §8— Tu rango e info");
        sender.sendMessage("§e  /rank tops [n]     §8— Top ELO global");
        sender.sendMessage("§e  /rank info <nick>  §8— Info de otro jugador");
        sender.sendMessage("§8§m                                        ");
    }
}

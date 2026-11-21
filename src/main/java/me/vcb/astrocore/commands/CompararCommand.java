package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.rank.PlayerRank;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class CompararCommand extends BaseCommand {

    public CompararCommand() {
        super("comparar", Collections.singletonList("compare"), null, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cUso: §e/comparar <jugador>");
            return;
        }

        Userdata a = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        Userdata b = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(args[0]);

        if (a == null) return;
        if (b == null) {
            player.sendMessage("§cJugador §e" + args[0] + " §cno encontrado.");
            return;
        }

        if (a.getUuid().equals(b.getUuid())) {
            player.sendMessage("§cNo puedes compararte contigo mismo.");
            return;
        }

        this.sendComparison(player, a, b);
    }

    private void sendComparison(Player player, Userdata a, Userdata b) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§6§l  Comparación: §e" + a.getName() + " §7vs §e" + b.getName());
        player.sendMessage("§8§m                                        ");

        // Header
        player.sendMessage(String.format("  §7%-14s §e%-12s §e%-12s", "Estadística", a.getName(), b.getName()));
        player.sendMessage("  §8§m──────────────────────────────────");

        // ELO
        this.sendRow(player, "ELO", a.getElo(), b.getElo(), true);
        // Rank
        PlayerRank ra = a.getRank() != null ? a.getRank() : PlayerRank.fromElo(a.getElo());
        PlayerRank rb = b.getRank() != null ? b.getRank() : PlayerRank.fromElo(b.getElo());
        player.sendMessage(String.format("  §7%-14s %s%-12s %s%-12s",
            "Rango",
            ra.getColor(), ra.getDisplayName(),
            rb.getColor(), rb.getDisplayName()));
        // Kills
        this.sendRow(player, "Kills", a.getKills(), b.getKills(), true);
        // Deaths
        this.sendRow(player, "Muertes", a.getDeaths(), b.getDeaths(), false);
        // K/D ratio
        double kdA = a.getDeaths() == 0 ? a.getKills() : (double) a.getKills() / a.getDeaths();
        double kdB = b.getDeaths() == 0 ? b.getKills() : (double) b.getKills() / b.getDeaths();
        this.sendRowDouble(player, "K/D", kdA, kdB, true);
        // Best killstreak
        this.sendRow(player, "Mejor racha", a.getHighestKillstreak(), b.getHighestKillstreak(), true);

        player.sendMessage("§8§m                                        ");
    }

    private void sendRow(Player player, String label, int va, int vb, boolean higherIsBetter) {
        String ca = "§f", cb = "§f";
        if (va > vb) { ca = higherIsBetter ? "§a" : "§c"; cb = higherIsBetter ? "§c" : "§a"; }
        else if (vb > va) { cb = higherIsBetter ? "§a" : "§c"; ca = higherIsBetter ? "§c" : "§a"; }
        player.sendMessage(String.format("  §7%-14s %s%-12d %s%-12d", label, ca, va, cb, vb));
    }

    private void sendRowDouble(Player player, String label, double va, double vb, boolean higherIsBetter) {
        String ca = "§f", cb = "§f";
        if (va > vb) { ca = higherIsBetter ? "§a" : "§c"; cb = higherIsBetter ? "§c" : "§a"; }
        else if (vb > va) { cb = higherIsBetter ? "§a" : "§c"; ca = higherIsBetter ? "§c" : "§a"; }
        player.sendMessage(String.format("  §7%-14s %s%-12.2f %s%-12.2f", label, ca, va, cb, vb));
    }
}

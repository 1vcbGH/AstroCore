package me.vcb.astrocore.rank;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.userdata.UserdataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class RankAdminCommand extends BaseCommand {

    public RankAdminCommand() {
        super("rankadmin", Collections.emptyList(), "astrocore.rankadmin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) { this.sendHelp(sender); return; }

        switch (args[0].toLowerCase()) {

            case "set": {
                if (args.length < 3) { sender.sendMessage("§cUso: /rankadmin set <jugador> <rango>"); return; }
                Userdata ud = this.resolveUserdata(sender, args[1]);
                if (ud == null) return;
                PlayerRank rank = this.resolveRank(sender, args[2]);
                if (rank == null) return;
                Player online = Bukkit.getPlayer(ud.getUuid());
                RankManager.getInstance().setRank(online, ud, rank);
                sender.sendMessage("§aRango de §e" + ud.getName() + " §aestablecido a " + rank.getColoredName() + "§a.");
                if (online != null) online.sendMessage("§6[Rango] §7Tu rango fue cambiado a " + rank.getColoredName() + " §7por un administrador.");
                break;
            }

            case "get": {
                if (args.length < 2) { sender.sendMessage("§cUso: /rankadmin get <jugador>"); return; }
                Userdata ud = this.resolveUserdata(sender, args[1]);
                if (ud == null) return;
                PlayerRank rank = ud.getRank() != null ? ud.getRank() : PlayerRank.fromElo(ud.getElo());
                sender.sendMessage("§7Rango de §f" + ud.getName() + "§7: " + rank.getColoredName()
                    + " §8(§7ELO: §e" + ud.getElo() + " §7| §7Kills: §e" + ud.getKills() + "§8)");
                break;
            }

            case "reset": {
                if (args.length < 2) { sender.sendMessage("§cUso: /rankadmin reset <jugador>"); return; }
                Userdata ud = this.resolveUserdata(sender, args[1]);
                if (ud == null) return;
                PlayerRank rank = PlayerRank.fromElo(ud.getElo());
                Player online = Bukkit.getPlayer(ud.getUuid());
                RankManager.getInstance().setRank(online, ud, rank);
                sender.sendMessage("§aRango de §e" + ud.getName() + " §aresetado a " + rank.getColoredName()
                    + " §a(ELO: §e" + ud.getElo() + "§a).");
                break;
            }

            case "top": case "leaderboard": case "lb": {
                int limit = 10;
                if (args.length >= 2) { try { limit = Math.min(50, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {} }
                RankManager.getInstance().sendGlobalLeaderboard(sender, limit);
                break;
            }

            case "setelo": {
                if (args.length < 3) { sender.sendMessage("§cUso: /rankadmin setelo <jugador> <cantidad>"); return; }
                Userdata ud = this.resolveUserdata(sender, args[1]);
                if (ud == null) return;
                int elo;
                try { elo = Math.max(0, Integer.parseInt(args[2])); } catch (NumberFormatException e) { sender.sendMessage("§cCantidad inválida."); return; }
                ud.addElo(elo - ud.getElo()); // set to exact value
                PlayerRank rank = PlayerRank.fromElo(elo);
                Player online = Bukkit.getPlayer(ud.getUuid());
                RankManager.getInstance().setRank(online, ud, rank);
                sender.sendMessage("§aELO de §e" + ud.getName() + " §aestablecido a §e" + elo + "§a. Rango: " + rank.getColoredName() + "§a.");
                break;
            }

            case "addelo": {
                if (args.length < 3) { sender.sendMessage("§cUso: /rankadmin addelo <jugador> <cantidad>"); return; }
                Userdata ud = this.resolveUserdata(sender, args[1]);
                if (ud == null) return;
                int amount;
                try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException e) { sender.sendMessage("§cCantidad inválida."); return; }
                ud.addElo(amount);
                Player online = Bukkit.getPlayer(ud.getUuid());
                if (online != null) RankManager.getInstance().checkRankUp(online, ud);
                sender.sendMessage("§a+" + amount + " ELO añadidos a §e" + ud.getName() + "§a. Total: §e" + ud.getElo());
                break;
            }

            case "list": {
                sender.sendMessage("§8§m                                        ");
                sender.sendMessage("§6§l  Rangos (ELO-based):");
                for (PlayerRank r : PlayerRank.values()) {
                    PlayerRank next = r.next();
                    String nextInfo = next != null
                        ? " §8→ §7" + next.getEloRequired() + " ELO para " + next.getColoredName()
                        : " §8(§6Rango máximo§8)";
                    sender.sendMessage("  " + r.getColoredName() + " §8[§7" + r.getEloRequired() + "+ ELO§8]" + nextInfo);
                }
                sender.sendMessage("§8§m                                        ");
                break;
            }

            default: this.sendHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("§6§l  /rankadmin — Gestión de rangos");
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("§e  set §7<jugador> <rango>    §8— Asigna rango");
        sender.sendMessage("§e  get §7<jugador>            §8— Ver rango y ELO");
        sender.sendMessage("§e  reset §7<jugador>          §8— Recalcular desde ELO");
        sender.sendMessage("§e  setelo §7<jugador> <n>     §8— Fijar ELO exacto");
        sender.sendMessage("§e  addelo §7<jugador> <n>     §8— Sumar ELO");
        sender.sendMessage("§e  top §7<rango> [cantidad]   §8— Leaderboard de división");
        sender.sendMessage("§e  list                      §8— Ver todos los rangos");
        sender.sendMessage("§8§m                                        ");
    }

    private void sendRankList(CommandSender sender) {
        sender.sendMessage("§7Rangos: "
            + Arrays.stream(PlayerRank.values())
                .map(r -> r.getColor() + r.getDisplayName())
                .reduce((a, b) -> a + "§7, " + b).orElse(""));
    }

    private Userdata resolveUserdata(CommandSender sender, String name) {
        UserdataManager um = AstroCore.getInstance().getUserdataManager();
        Player online = Bukkit.getPlayer(name);
        if (online != null) {
            Userdata ud = um.getUserdata(online);
            if (ud != null) return ud;
        }
        Userdata ud = um.getOfflineUserdataByName(name);
        if (ud == null) { sender.sendMessage("§cJugador §e" + name + " §cno encontrado."); return null; }
        return ud;
    }

    private PlayerRank resolveRank(CommandSender sender, String name) {
        for (PlayerRank rank : PlayerRank.values()) {
            if (rank.name().equalsIgnoreCase(name) || rank.getDisplayName().equalsIgnoreCase(name)) return rank;
        }
        sender.sendMessage("§cRango inválido: §e" + name);
        this.sendRankList(sender);
        return null;
    }
}

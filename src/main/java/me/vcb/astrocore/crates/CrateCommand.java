package me.vcb.astrocore.crates;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.crates.gui.CrateEditorGui;
import me.vcb.astrocore.crates.gui.CrateEditorListGui;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CrateCommand extends BaseCommand {

    private static final String PERM = "astrocore.crate.admin";

    public CrateCommand() {
        super("crate", Arrays.asList("crates"), PERM, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) { sendHelp(sender); return; }
            CrateEditorListGui.open((Player) sender);
            return;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (args.length < 2) { sender.sendMessage(Color.translate("§cUso: /crate create <id>")); return; }
                String id = args[1].toLowerCase();
                if (CrateManager.getInstance().getCrate(id) != null) {
                    sender.sendMessage(Color.translate("§cYa existe una crate con id §f" + id)); return;
                }
                Crate crate = CrateManager.getInstance().createCrate(id);
                sender.sendMessage(Color.translate("§aCrate §f" + id + " §acreada."));
                if (sender instanceof Player p) CrateEditorGui.open(p, crate);
            }

            case "setchest" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return; }
                if (args.length < 2) { p.sendMessage(Color.translate("§cUso: /crate setchest <id>")); return; }
                String id = args[1].toLowerCase();
                Crate crate = CrateManager.getInstance().getCrate(id);
                if (crate == null) { p.sendMessage(Color.translate("§cCrate §f" + id + " §cno existe.")); return; }

                Block target = p.getTargetBlockExact(5);
                if (target == null) { p.sendMessage(Color.translate("§cMirá un bloque (máx 5 bloques).")); return; }

                // Destroy old hologram before moving
                CrateManager.getInstance().destroyHologram(id);
                crate.setChestLocation(target.getLocation());
                CrateManager.getInstance().save();
                CrateManager.getInstance().refreshHologram(crate);
                var loc = target.getLocation();
                p.sendMessage(Color.translate("§aUbicación de la crate §f" + id + " §adefinida en "
                    + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()));
            }

            case "edit" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return; }
                if (args.length < 2) { CrateEditorListGui.open(p); return; }
                String id = args[1].toLowerCase();
                Crate crate = CrateManager.getInstance().getCrate(id);
                if (crate == null) { p.sendMessage(Color.translate("§cCrate §f" + id + " §cno existe.")); return; }
                CrateEditorGui.open(p, crate);
            }

            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(Color.translate("§cUso: /crate give <jugador> <id> [cantidad]")); return;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(Color.translate("§cJugador §f" + args[1] + " §cno encontrado.")); return; }
                String id = args[2].toLowerCase();
                Crate crate = CrateManager.getInstance().getCrate(id);
                if (crate == null) { sender.sendMessage(Color.translate("§cCrate §f" + id + " §cno existe.")); return; }
                int amount = args.length >= 4 ? parseIntSafe(args[3], 1) : 1;
                target.getInventory().addItem(CrateKey.createKey(crate, amount));
                target.sendMessage(Color.translate("§aRecibiste §f" + amount + "x Llave §r"
                    + crate.getDisplayName()));
                sender.sendMessage(Color.translate("§aDiste §f" + amount + "x Llave §r"
                    + crate.getDisplayName() + " §aa §f" + target.getName()));
            }

            case "giveall" -> {
                if (args.length < 2) {
                    sender.sendMessage(Color.translate("§cUso: /crate giveall <id> [cantidad]")); return;
                }
                String id = args[1].toLowerCase();
                Crate crate = CrateManager.getInstance().getCrate(id);
                if (crate == null) { sender.sendMessage(Color.translate("§cCrate §f" + id + " §cno existe.")); return; }
                int amount = args.length >= 3 ? parseIntSafe(args[2], 1) : 1;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.getInventory().addItem(CrateKey.createKey(crate, amount));
                    online.sendMessage(Color.translate("§aRecibiste §f" + amount + "x Llave §r"
                        + crate.getDisplayName()));
                }
                sender.sendMessage(Color.translate("§aDiste §f" + amount + "x llave a todos los online."));
            }

            case "delete" -> {
                if (args.length < 2) { sender.sendMessage(Color.translate("§cUso: /crate delete <id>")); return; }
                String id = args[1].toLowerCase();
                if (CrateManager.getInstance().deleteCrate(id))
                    sender.sendMessage(Color.translate("§cCrate §f" + id + " §celiminada."));
                else
                    sender.sendMessage(Color.translate("§cCrate §f" + id + " §cno existe."));
            }

            case "reload" -> {
                sender.sendMessage(Color.translate("§eRecarga de crates: reiniciá el servidor para recargar desde YAML."));
            }

            case "list" -> {
                var crates = CrateManager.getInstance().getCrates();
                if (crates.isEmpty()) { sender.sendMessage(Color.translate("§7No hay crates.")); return; }
                sender.sendMessage(Color.translate("§6§lCrates (" + crates.size() + "):"));
                for (Crate c : crates) {
                    String loc = c.hasLocation()
                        ? c.getChestLocation().getBlockX() + "," + c.getChestLocation().getBlockY() + "," + c.getChestLocation().getBlockZ()
                        : "§cNo definida";
                    sender.sendMessage(Color.translate("  §f" + c.getId() + " §7| "
                        + c.getDisplayName() + " §7| §f" + c.getRewards().size() + " premios §7| " + loc));
                }
            }

            default -> sendHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Color.translate("§6§lCrates — Comandos:"));
        sender.sendMessage(Color.translate("§f/crate create <id>"));
        sender.sendMessage(Color.translate("§f/crate setchest <id>"));
        sender.sendMessage(Color.translate("§f/crate edit [id]"));
        sender.sendMessage(Color.translate("§f/crate give <jugador> <id> [cantidad]"));
        sender.sendMessage(Color.translate("§f/crate giveall <id> [cantidad]"));
        sender.sendMessage(Color.translate("§f/crate delete <id>"));
        sender.sendMessage(Color.translate("§f/crate list"));
    }

    private int parseIntSafe(String s, int def) {
        try { return Math.max(1, Integer.parseInt(s)); }
        catch (Exception e) { return def; }
    }
}

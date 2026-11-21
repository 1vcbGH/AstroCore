package me.vcb.astrocore.supplydrop;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * /supplysmanagement [drop <cantidad>|loot <add|remove|list>|items <cantidad>]
 */
public class SupplyDropCommand extends BaseCommand {

    public SupplyDropCommand() {
        super("supplysmanagement", Arrays.asList("supplydrop", "sd"), "astrocore.supplydrop", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SupplyDropManager mgr = AstroCore.getInstance().getSupplyDropManager();

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {

            // /supplysmanagement drop [cantidad]
            case "drop": {
                int amount = 1;
                if (args.length >= 2) {
                    if (!this.checkNumber(sender, args[1])) return;
                    amount = Math.max(1, Math.min(10, Integer.parseInt(args[1])));
                }
                final int finalAmount = amount;
                sender.sendMessage(Color.translate("&7Spawneando &e" + finalAmount + " &7supply drop(s)..."));
                Tasks_run(sender, mgr, finalAmount);
                return;
            }

            // /supplysmanagement loot  →  abre el GUI
            case "loot": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Color.translate("&cSolo jugadores pueden usar este subcomando."));
                    return;
                }
                mgr.openLootGui((Player) sender);
                return;
            }

            // /supplysmanagement items <cantidad>
            case "items": {
                if (args.length < 2) {
                    sender.sendMessage(Color.translate("&cUso: /supplysmanagement items <cantidad>"));
                    return;
                }
                if (!this.checkNumber(sender, args[1])) return;
                int qty = Math.max(1, Math.min(27, Integer.parseInt(args[1])));
                mgr.setItemsPerDrop(qty);
                sender.sendMessage(Color.translate("&aItems por drop seteados a: &e" + qty));
                return;
            }

            // /supplysmanagement status
            case "status": {
                sender.sendMessage(Color.translate("&6Supply Drops activos: &e" + mgr.getActiveDropCount()));
                return;
            }

            // /supplysmanagement stop  — remove nearest drop
            case "stop": {
                org.bukkit.Location from = (sender instanceof Player p) ? p.getLocation() : null;
                if (mgr.stopNearest(from, sender.getName())) {
                    sender.sendMessage(Color.translate("&aSupply drop eliminado."));
                } else {
                    sender.sendMessage(Color.translate("&cNo hay supply drops activos."));
                }
                return;
            }

            // /supplysmanagement stopall  — remove all drops
            case "stopall": {
                int removed = mgr.stopAll(sender.getName());
                if (removed == 0) {
                    sender.sendMessage(Color.translate("&cNo había supply drops activos."));
                } else {
                    sender.sendMessage(Color.translate("&aEliminados &e" + removed + " &asupply drop(s)."));
                }
                return;
            }

            // /supplysmanagement test  — teleport to nearest active drop
            case "test": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Color.translate("&cSolo jugadores pueden usar este subcomando."));
                    return;
                }
                if (mgr.getActiveDropCount() == 0) {
                    sender.sendMessage(Color.translate("&cNo hay supply drops activos. Usa /sd drop primero."));
                    return;
                }
                me.vcb.astrocore.supplydrop.SupplyDrop drop = mgr.getActiveDrops().iterator().next();
                org.bukkit.Location tp = drop.getChestLocation().clone().add(0.5, 1, 0.5);
                player.teleport(tp);
                player.sendMessage(Color.translate("&aTeleportado al supply drop en &eX:" + tp.getBlockX() + " Z:" + tp.getBlockZ()));
                return;
            }

            default:
                sendHelp(sender);
        }
    }

    private void Tasks_run(CommandSender sender, SupplyDropManager mgr, int amount) {
        int spawned = mgr.spawnDrops(amount);
        if (spawned == 0) {
            sender.sendMessage(Color.translate("&cNo se pudo encontrar una ubicación válida en la warzone."));
        } else {
            sender.sendMessage(Color.translate("&a¡Spawneados &e" + spawned + " &asupply drop(s) en la warzone!"));
            if (spawned < amount) {
                sender.sendMessage(Color.translate("&e(Solo se encontraron ubicaciones para " + spawned + " de " + amount + ")"));
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Color.translate("&6&lSupply Drop Management &8— Comandos:"));
        sender.sendMessage(Color.translate("  &e/sd drop [cantidad] &7— Spawnea supply drops manualmente"));
        sender.sendMessage(Color.translate("  &e/sd loot &7— Abre el editor de loot (GUI)"));
        sender.sendMessage(Color.translate("  &e/sd items <cantidad> &7— Items por supply drop"));
        sender.sendMessage(Color.translate("  &e/sd status &7— Drops activos en el mapa"));
        sender.sendMessage(Color.translate("  &e/sd test &7— Teleportarte al supply drop más cercano"));
        sender.sendMessage(Color.translate("  &e/sd stop &7— Elimina el supply drop más cercano"));
        sender.sendMessage(Color.translate("  &e/sd stopall &7— Elimina todos los supply drops"));
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}

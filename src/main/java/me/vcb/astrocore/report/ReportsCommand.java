package me.vcb.astrocore.report;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReportsCommand extends BaseCommand implements Listener {

    private static final String TITLE      = "§4§lReportes Pendientes";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    private final Set<UUID> viewing = new HashSet<>();

    public ReportsCommand() {
        super("reports", "astrocore.report.receive", true);
        Bukkit.getPluginManager().registerEvents(this,
            me.vcb.astrocore.AstroCore.getInstance());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // /reports clear
        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            ReportManager.getInstance().dismissAll();
            player.sendMessage("§c§lReportes §r§7limpiados.");
            return;
        }

        openGui(player);
    }

    // ------------------------------------------------------------------

    private void openGui(Player player) {
        List<ReportData> reports = ReportManager.getInstance().getPending();
        int size = Math.max(9, ((reports.size() / 9) + 1) * 9);
        size = Math.min(size, 54);

        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        for (int i = 0; i < Math.min(reports.size(), size); i++) {
            ReportData r = reports.get(i);

            List<String> lore = new ArrayList<>();
            lore.add("§7Reportado por: §e" + r.getReporterName());
            lore.add("§7Sospechoso:    §c" + r.getSuspectName());
            lore.add("§7Motivo: §f" + r.getReason());
            lore.add("§7Hora: §b" + SDF.format(new Date(r.getTimestamp())));
            lore.add("");
            lore.add("§a▶ Click §7— teleportar a sospechoso");
            lore.add("§c▶ Click derecho §7— descartar reporte");

            boolean online = Bukkit.getPlayer(r.getSuspectUuid()) != null;
            Material mat = online ? Material.LIME_DYE : Material.GRAY_DYE;

            inv.setItem(i, new ItemBuilder(mat)
                .setName((online ? "§a" : "§7") + r.getSuspectName()
                    + " §8| §e" + r.getReporterName())
                .setLore(lore)
                .build());
        }

        if (reports.isEmpty()) {
            inv.setItem(4, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .setName("§a§lSin reportes pendientes")
                .build());
        }

        viewing.add(player.getUniqueId());
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        int slot = e.getSlot();
        List<ReportData> reports = ReportManager.getInstance().getPending();
        if (slot >= reports.size()) return;

        ReportData r = reports.get(slot);

        boolean rightClick = e.isRightClick();

        if (rightClick) {
            ReportManager.getInstance().dismiss(slot);
            player.sendMessage("§7Reporte de §e" + r.getReporterName()
                + " §7contra §c" + r.getSuspectName() + " §7descartado.");
        } else {
            Player suspect = Bukkit.getPlayer(r.getSuspectUuid());
            if (suspect == null) {
                player.sendMessage("§c" + r.getSuspectName() + " §7no está online.");
            } else {
                player.teleport(suspect.getLocation());
                player.sendMessage("§7Teletransportado a §c" + suspect.getName() + "§7.");
            }
        }

        // Refresh GUI
        player.closeInventory();
        openGui(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        viewing.remove(e.getPlayer().getUniqueId());
    }
}

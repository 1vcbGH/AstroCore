package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

/**
 * GUI for editing hologram lines.
 * Each line is shown as a paper item.
 * Left-click  → edit that line in chat.
 * Right-click → delete that line.
 * Slot 49     → add new line.
 * Slot 45     → back to editor.
 */
public class CrateHologramGui {

    public static void open(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Color.translate("§bHolograma — " + crate.getId()));

        var bg = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        List<String> lines = crate.getHologramLines();
        for (int i = 0; i < lines.size() && i < 36; i++) {
            inv.setItem(i + 9, new ItemBuilder(Material.PAPER)
                .setName(Color.translate("§fLínea §e" + (i + 1)))
                .setLore(Arrays.asList(
                    Color.translate("§7" + lines.get(i)),
                    Color.translate("§8──────────────────"),
                    Color.translate("§eClick izq §7para editar en chat"),
                    Color.translate("§cClick der §7para borrar")
                )).build());
        }

        inv.setItem(49, new ItemBuilder(Material.LIME_DYE)
            .setName(Color.translate("§a§lAgregar línea"))
            .setLore(Arrays.asList(
                Color.translate("§7Agrega una nueva línea al final.")
            )).build());

        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("§c§lVolver")).build());

        CrateGuiTracker.markHologramEditor(player, crate.getId());
        player.openInventory(inv);
    }

    /** Returns the line index (0-based) from inventory slot, or -1 if not a line slot. */
    public static int getLineIndex(int slot) {
        int idx = slot - 9;
        return (idx >= 0 && idx < 36) ? idx : -1;
    }
}

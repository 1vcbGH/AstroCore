package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.crates.CrateManager;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;

public class CrateEditorListGui {

    public static void open(Player player) {
        List<Crate> crates = CrateManager.getInstance().getCrates();
        int size = Math.max(27, ((crates.size() / 9) + 1) * 9);
        if (size > 54) size = 54;

        Inventory inv = Bukkit.createInventory(null, size,
            Color.translate("§5§lEditor de Crates"));

        var border = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < size; i++) inv.setItem(i, border);

        int slot = 10;
        for (Crate crate : crates) {
            if (slot >= size - 1) break;
            inv.setItem(slot, new ItemBuilder(Material.CHEST)
                .setName(Color.translate(crate.getDisplayName()))
                .setLore(Arrays.asList(
                    Color.translate("§7ID: §f" + crate.getId()),
                    Color.translate("§7Premios: §f" + crate.getRewards().size()),
                    Color.translate("§7Ubicación: §f" + (crate.hasLocation()
                        ? locStr(crate) : "§cNo definida")),
                    Color.translate("§8──────────────────"),
                    Color.translate("§eClick para editar")
                ))
                .build());
            slot++;
            if (slot % 9 == 8) slot += 2; // skip edges
        }

        // Create new crate button
        inv.setItem(size - 5, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .setName(Color.translate("§a§lNueva Crate"))
            .setLore(List.of(Color.translate("§7Usa §f/crate create <id>")))
            .build());

        CrateGuiTracker.markEditorList(player);
        player.openInventory(inv);
    }

    private static String locStr(Crate crate) {
        var l = crate.getChestLocation();
        return l.getWorld().getName() + " " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
}

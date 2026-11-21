package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.crates.CrateReward;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Config GUI for a single reward slot.
 *
 * Slot 13 : item preview
 * Slot 10 : toggle winnable
 * Slot 12 : set chance (only when winnable)
 * Slot 16 : manage commands
 * Slot 40 : back to grid
 */
public class CrateRewardSlotConfigGui {

    public static void open(Player player, Crate crate, int gridSlot) {
        CrateReward reward = crate.getRewardAtSlot(gridSlot);
        if (reward == null) return;

        Inventory inv = Bukkit.createInventory(null, 45,
            Color.translate("§5Config Slot #" + (gridSlot + 1)));

        ItemStack border = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
            .setName("§r").build();
        for (int i = 0; i < 45; i++) inv.setItem(i, border);

        // Preview item
        var item = reward.getItem();
        if (item != null) inv.setItem(13, item.clone());

        // Toggle winnable
        inv.setItem(10, new ItemBuilder(
                reward.isWinnable() ? Material.LIME_DYE : Material.GRAY_DYE)
            .setName(reward.isWinnable()
                ? Color.translate("§a§l✔ GANABLE")
                : Color.translate("§7§l✖ NO GANABLE"))
            .setLore(Arrays.asList(
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para " + (reward.isWinnable() ? "§cdeshabilitar" : "§ahabilitar"))
            )).build());

        // Chance (only when winnable)
        if (reward.isWinnable()) {
            inv.setItem(12, new ItemBuilder(Material.COMPARATOR)
                .setName(Color.translate("§b§lPeso (Chance)"))
                .setLore(Arrays.asList(
                    Color.translate("§7Actual: §f" + reward.getChance()),
                    Color.translate("§8──────────────────"),
                    Color.translate("§7Mayor peso = más probable."),
                    Color.translate("§eClick para cambiar en chat")
                )).build());
        }

        // Commands
        inv.setItem(16, new ItemBuilder(Material.COMMAND_BLOCK)
            .setName(Color.translate("§9§lComandos"))
            .setLore(buildCommandsLore(reward))
            .build());

        // Back
        inv.setItem(40, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("§c§lVolver al grid")).build());

        CrateGuiTracker.markRewardSlotConfig(player, crate.getId(), gridSlot);
        player.openInventory(inv);
    }

    private static List<String> buildCommandsLore(CrateReward r) {
        List<String> lore = new java.util.ArrayList<>();
        lore.add(Color.translate("§8──────────────────"));
        if (r.getCommands().isEmpty()) {
            lore.add(Color.translate("§7Sin comandos"));
        } else {
            for (String cmd : r.getCommands())
                lore.add(Color.translate("  §f/" + cmd));
        }
        lore.add(Color.translate("§8──────────────────"));
        lore.add(Color.translate("§eClick §7→ agregar comando en chat"));
        lore.add(Color.translate("§cShift+Click §7→ limpiar todos"));
        lore.add(Color.translate("§7Usa §f%player% §7de placeholder."));
        return lore;
    }
}

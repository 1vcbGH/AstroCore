package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.crates.CrateReward;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CratePreviewGui {

    public static void open(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Color.translate("§6Premios — " + crate.getDisplayName()));

        int totalWeight = crate.getRewards().stream()
            .filter(CrateReward::isWinnable)
            .mapToInt(CrateReward::getChance).sum();

        for (CrateReward reward : crate.getRewards()) {
            int slot = reward.getSlot();
            if (slot < 0 || slot >= 54) continue;

            if (reward.isWinnable()) {
                var item = reward.buildPreviewItem(totalWeight);
                if (item != null) inv.setItem(slot, item);
            } else {
                var raw = reward.getItem();
                if (raw != null) inv.setItem(slot, raw.clone());
            }
        }

        CrateGuiTracker.markPreview(player, crate.getId());
        player.openInventory(inv);
    }
}

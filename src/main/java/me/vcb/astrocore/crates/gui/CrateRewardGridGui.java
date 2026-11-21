package me.vcb.astrocore.crates.gui;

import me.vcb.astrocore.crates.Crate;
import me.vcb.astrocore.crates.CrateReward;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 54-slot large chest for arranging crate rewards.
 * All 54 slots are free — works like a normal chest.
 * Right-click an item → toggle winnable / open chance config.
 * Auto-saves on close.
 */
public class CrateRewardGridGui {

    public static final int GRID_SIZE = 54;

    public static void open(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, GRID_SIZE,
            Color.translate("§5Rewards — " + crate.getId()));

        for (CrateReward reward : crate.getRewards()) {
            int slot = reward.getSlot();
            if (slot < 0 || slot >= GRID_SIZE) continue;
            ItemStack display = reward.buildGridItem();
            if (display != null) inv.setItem(slot, display);
        }

        CrateGuiTracker.markRewardGrid(player, crate.getId());
        player.openInventory(inv);
    }
}

package me.vcb.astrocore.handlers;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.item.ItemBuilder;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InventoryHandler extends Handler implements Listener {

    private final Set<UUID> invsee;

    public InventoryHandler() {
        this.invsee = new HashSet<>();
    }

    @Override
    public void disable() {
        this.invsee.clear();
    }

    public void openInvseeInventory(Player player, Player target) {
        this.invsee.add(player.getUniqueId());
        player.openInventory(target.getInventory());
    }

    public void fillWaterBottles(Player player) {
        ItemStack item = player.getItemInHand();

        if(item == null || item.getType() != Material.GLASS_BOTTLE) {
            player.sendMessage(Language.PREFIX + Language.FILL_BOTTLE_NO_GLASS_BOTTLE);
            return;
        }

        Block block = player.getTargetBlock(Set.of(), 5);

        if(block == null || (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)) {
            player.sendMessage(Language.PREFIX + Language.FILL_BOTTLE_CHEST_NOT_FOUND);
            return;
        }

        Chest chest = (Chest) block.getState();

        if(chest.getInventory().firstEmpty() == -1) {
            player.sendMessage(Language.PREFIX + Language.FILL_BOTTLE_ALREADY_FULL);
            return;
        }

        int total = 0;

        for(int i = 0; i < item.getAmount(); i++) {
            if(chest.getInventory().firstEmpty() == -1) break;

            chest.getInventory().addItem(new ItemBuilder(Material.POTION).build());
            total++;
        }

        if(item.getAmount() == total) {
            player.setItemInHand(new ItemStack(Material.AIR));
        } else {
            item.setAmount(item.getAmount() - total);
        }

        player.sendMessage(Language.PREFIX + Language.FILL_BOTTLE_FILLED.replace("<amount>", String.valueOf(total)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(event.getBlock().getType() != Material.STONE && event.getBlock().getType() != Material.COBBLESTONE) return;
        me.vcb.astrocore.userdata.Userdata cobbleData = AstroCore.getInstance().getUserdataManager().getUserdata(event.getPlayer());
        if(cobbleData == null || cobbleData.getSettings().isCobble()) return;

        NmsUtils.getInstance().increaseStatistic(event.getPlayer(), Statistic.MINE_BLOCK, event.getBlock().getType());
        event.getBlock().setType(Material.AIR);

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(event.getItem().getItemStack().getType() != Material.COBBLESTONE) return;
        me.vcb.astrocore.userdata.Userdata pickupData = AstroCore.getInstance().getUserdataManager().getUserdata(event.getPlayer());
        if(pickupData == null || pickupData.getSettings().isCobble()) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!this.invsee.contains(event.getWhoClicked().getUniqueId())) return;
        if(event.getWhoClicked().hasPermission("astrocore.invsee.edit")) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        this.invsee.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.invsee.remove(event.getPlayer().getUniqueId());
    }
}

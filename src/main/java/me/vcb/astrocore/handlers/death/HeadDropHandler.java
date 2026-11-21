package me.vcb.astrocore.handlers.death;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HeadDropHandler extends Handler implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!Config.HEAD_DROP_ENABLED) return;

        Player player = event.getEntity();
        Player killer = player.getKiller();

        if(killer == null || player == killer) return;
        if(!killer.hasPermission("astrocore.headdrop")) return;

        Location location = player.getLocation();
        String skullName = Config.HEAD_DROP_NAME_FORMAT.replace("<player>", player.getName());

        ItemStack skullDrop = new ItemBuilder(Material.PLAYER_HEAD, 1)
            .setName(skullName)
            .setSkullOwner(player.getName()).build();

        location.getWorld().dropItemNaturally(location, skullDrop);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!Config.HEAD_DROP_ENABLED) return;

        Block block = event.getBlock();
        if(block.getType() != Material.PLAYER_HEAD) return;

        Skull skull = (Skull) block.getState();
        if(!skull.hasOwner()) return;

        event.setCancelled(true);

        String skullName = Config.HEAD_DROP_NAME_FORMAT.replace("<player>", skull.getOwner());

        ItemStack skullDrop = new ItemBuilder(Material.PLAYER_HEAD, 1).setName(skullName)
        .setSkullOwner(skull.getOwner()).build();

        block.getWorld().dropItemNaturally(block.getLocation(), skullDrop);
        block.setType(Material.AIR);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!Config.HEAD_DROP_ENABLED || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(event.getClickedBlock().getType() != Material.PLAYER_HEAD) return;

        Skull skull = (Skull) event.getClickedBlock().getState();
        if(skull.getOwner() == null) return;

        Player player = event.getPlayer();
        player.sendMessage(Language.HEAD_DROP_CLICK_MESSAGE.replace("<player>", skull.getOwner()));
    }
}

package me.vcb.astrocore.handlers;

import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisenchantHandler extends Handler implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_BLOCK || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(!event.hasItem() || event.getItem().getType() != Material.ENCHANTED_BOOK) return;
        if(event.getClickedBlock().getType() != Material.ENCHANTING_TABLE) return;

        event.setCancelled(true);

        event.getPlayer().setItemInHand(new ItemBuilder(Material.BOOK).build());
        event.getPlayer().sendMessage(Language.PREFIX + Language.BOOK_DISENCHANT);
    }
}

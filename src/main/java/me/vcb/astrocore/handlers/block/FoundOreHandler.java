package me.vcb.astrocore.handlers.block;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class FoundOreHandler extends Handler implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!Config.FOUND_ORE_ENABLED || event.getBlock().getType() != Material.DIAMOND_ORE) return;
        event.getBlock().setMetadata("foundOre", PlayerUtils.TRUE_METADATA_VALUE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!Config.FOUND_ORE_ENABLED || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(!AstroCore.getInstance().getChatControlHandler().isShowFoundOre()) return;

        Block block = event.getBlock();
        if(block.getType() != Material.DIAMOND_ORE || block.hasMetadata("foundOre")) return;

        AtomicInteger count = new AtomicInteger();
        this.countDiamonds(block, count);

        block.removeMetadata("foundOre", AstroCore.getInstance());

        Bukkit.getOnlinePlayers().forEach(online -> {
            if(!online.hasPermission("astrocore.foundore.receive")) return;
            me.vcb.astrocore.userdata.Userdata oreData = AstroCore.getInstance().getUserdataManager().getUserdata(online);
            if(event.getPlayer() != online && (oreData == null || !oreData.getSettings().isFoundOre())) return;

            online.sendMessage(Language.FOUND_ORE_MESSAGE.replace("<player>", event.getPlayer().getName())
            .replace("<amount>", String.valueOf(count.get())));
        });
    }

    private void countDiamonds(Block block, AtomicInteger count) {
        for(int[] face : ItemUtils.BLOCK_RELATIVES) {
            if(count.get() >= 50) return;

            Block newBlock = block.getRelative(face[0], face[1], face[2]);
            if(newBlock.getType() != Material.DIAMOND_ORE || newBlock.hasMetadata("foundOre")) continue;

            count.getAndIncrement();
            newBlock.setMetadata("foundOre", PlayerUtils.TRUE_METADATA_VALUE);
            this.countDiamonds(newBlock, count);
        }
    }
}

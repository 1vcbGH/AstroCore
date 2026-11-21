package me.vcb.astrocore.games.koth.listener;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.koth.event.KothCappedEvent;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KothEventListeners implements Listener {

    public KothEventListeners() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    private boolean isKothSign(Block block) {
        if(!Tag.SIGNS.isTagged(block.getType())) return false;

        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();
        return lines.length > 0 && lines[1] != null && lines[1].equals(Config.KOTH_CAPPED_SIGN_LORE.get(1));
    }

    private ItemStack getKothCapSign(Player capper, KothData koth) {
        FastDateFormat fastDateFormat = FastDateFormat.getInstance(Config
        .DATE_FORMAT, Config.TIMEZONE, Locale.ENGLISH);

        ItemBuilder builder = new ItemBuilder(Material.OAK_SIGN, 1).setName(Config.KOTH_CAPPED_SIGN_NAME)
        .setLore(Config.KOTH_CAPPED_SIGN_LORE.stream().map(line -> line
        .replace("<koth>", koth.getName()).replace("<capper>", capper.getName())
        .replace("<date>", fastDateFormat.format(System.currentTimeMillis()))).collect(Collectors.toList()));

        return builder.build();
    }

    @EventHandler
    public void onKothCapped(KothCappedEvent event) {
        Player capper = event.getCapper();
        KothData koth = event.getKoth();

        // getLoot() may be null if koth was loaded from JSON (transient field).
        // startKoth() should restore it, but guard here as failsafe.
        me.vcb.astrocore.games.loot.LootData loot = koth.getLoot();
        if(loot == null) {
            loot = AstroCore.getInstance().getLootManager().getLootByName(koth.getName());
        }
        if(loot != null) loot.handleRewards(capper);

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(event.getCapper());

        if(faction != null) {
            faction.incrementPoints(koth.getFactionPoints());
            faction.incrementKothsCapped();
        }

        if(!Config.KOTH_CAPPED_SIGN_ENABLED) return;

        if(capper.getInventory().firstEmpty() == -1) {
            capper.getWorld().dropItemNaturally(capper.getLocation(), this.getKothCapSign(capper, koth));
        } else {
            capper.getInventory().addItem(this.getKothCapSign(capper, koth));
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(!this.isKothSign(event.getBlock())) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!this.isKothSign(event.getBlock())) return;

        event.setCancelled(true);

        Block block = event.getBlock();
        Sign sign = (Sign) block.getState();

        ItemBuilder builder = new ItemBuilder(Material.OAK_SIGN, 1).setName(Config
        .KOTH_CAPPED_SIGN_NAME).setLore(Arrays.asList(sign.getLines()));

        block.getWorld().dropItemNaturally(block.getLocation(), builder.build());
        block.setType(Material.AIR);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if(!Tag.SIGNS.isTagged(block.getType())) return;

        ItemStack item = event.getPlayer().getItemInHand();
        if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().hasLore()) return;
        if(!item.getItemMeta().getDisplayName().equalsIgnoreCase(Config.KOTH_CAPPED_SIGN_NAME)) return;

        Sign sign = (Sign) block.getState();

        IntStream.range(0, 4).forEach(i -> sign.setLine(i, item.getItemMeta().getLore().get(i)));

        sign.update();
        event.getPlayer().closeInventory();
    }
}

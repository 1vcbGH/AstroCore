package me.vcb.astrocore.handlers;

import lombok.Getter;
import lombok.Setter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.item.ItemBuilder;
import me.vcb.astrocore.utils.item.ItemUtils;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StatsHandler extends Handler implements Listener {

    private final Map<UUID, Inventory> stats;
    private final List<StatsItem> statsItems;
    private final Set<UUID> viewingStats;

    public StatsHandler() {
        this.stats = new HashMap<>();
        this.statsItems = new ArrayList<>();
        this.viewingStats = new HashSet<>();

        this.loadStatsItems();
    }

    @Override
    public void disable() {
        this.stats.clear();
        this.statsItems.clear();
        this.viewingStats.clear();
    }

    private void loadStatsItems() {
        ConfigurationSection section = AstroCore.getInstance().getConfig()
            .getSection("STATS_COMMAND.INVENTORY_ITEMS");

        section.getKeys(false).forEach(item -> {
            StatsItem statsItem = new StatsItem();

            ItemStack itemStack = ItemUtils.parseItem(section.getString(item + ".MATERIAL_ID"));
            if(itemStack == null) return;

            statsItem.setItem(itemStack);
            statsItem.setName(section.getString(item + ".NAME"));
            statsItem.setReplace(section.getString(item + ".REPLACE"));
            statsItem.setSlot(section.getInt(item + ".SLOT") - 1);

            this.statsItems.add(statsItem);
        });
    }

    public void openStatsInventory(Player sender, Player player) {
        this.cacheInventory(player);

        Inventory inventory = this.stats.get(player.getUniqueId());
        this.updateItems(player, inventory);

        this.viewingStats.add(sender.getUniqueId());
        sender.openInventory(inventory);
    }

    private void cacheInventory(Player player) {
        // Always recreate so stats are fresh
        String name = Config.STATS_INVENTORY_NAME.replace("<player>", player.getName());
        Inventory inv = Bukkit.createInventory(null, Config.STATS_INVENTORY_SIZE, name);
        this.stats.put(player.getUniqueId(), inv);
    }

    private void updateItems(Player player, Inventory inv) {
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);

        this.statsItems.forEach(item -> {
            ItemBuilder builder = new ItemBuilder(item.getItem());

            switch(item.getReplace()) {
                case "<kills>": builder.setName(item.getName() + data.getKills()); break;
                case "<deaths>": builder.setName(item.getName() + data.getDeaths()); break;
                case "<balance>": builder.setName(item.getName() + data.getBalance()); break;
                case "<diamonds>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE)); break;
                case "<emeralds>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.EMERALD_ORE)); break;
                case "<gold>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.GOLD_ORE)); break;
                case "<iron>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.IRON_ORE)); break;
                case "<lapis>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.LAPIS_ORE)); break;
                case "<coal>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.COAL_ORE)); break;
                case "<redstone>": builder.setName(item.getName() + player.getStatistic(Statistic.MINE_BLOCK, Material.REDSTONE_ORE)); break;
                case "<elo>": builder.setName(item.getName() + (data != null ? data.getElo() : 0)); break;
                case "<kdr>": {
                    if (data != null) {
                        double kdr = data.getDeaths() == 0
                            ? data.getKills()
                            : (double) data.getKills() / data.getDeaths();
                        builder.setName(item.getName() + String.format("%.2f", kdr));
                    } else { builder.setName(item.getName() + "0.00"); }
                    break;
                }
                case "<killstreak>": builder.setName(item.getName() + (data != null ? data.getKillstreak() : 0)); break;
                case "<highestkillstreak>": builder.setName(item.getName() + (data != null ? data.getHighestKillstreak() : 0)); break;
                case "<kothscapped>": builder.setName(item.getName() + (data != null ? data.getKothsCapped() : 0)); break;
            }

            inv.setItem(item.getSlot(), builder.build());
        });

        // Hardcoded extra stats (always present regardless of config)
        if (data != null) {
            double kdr = data.getDeaths() == 0
                ? data.getKills()
                : (double) data.getKills() / data.getDeaths();

            me.vcb.astrocore.rank.PlayerRank rank = data.getRank() != null
                ? data.getRank()
                : me.vcb.astrocore.rank.PlayerRank.fromElo(data.getElo());

            inv.setItem(10, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§eELO: §f" + data.getElo() + " §8| " + rank.getColoredName())
                .build());
            inv.setItem(11, new ItemBuilder(Material.GOLDEN_SWORD)
                .setName("§aKDR: §f" + String.format("%.2f", kdr))
                .build());
            inv.setItem(12, new ItemBuilder(Material.BLAZE_ROD)
                .setName("§6Racha actual: §f" + data.getKillstreak())
                .build());
            inv.setItem(16, new ItemBuilder(Material.NETHER_STAR)
                .setName("§cMayor racha: §f" + data.getHighestKillstreak())
                .build());
            inv.setItem(17, new ItemBuilder(Material.WHITE_BANNER)
                .setName("§5KOTHs capturados: §f" + data.getKothsCapped())
                .build());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!this.viewingStats.contains(event.getWhoClicked().getUniqueId())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.viewingStats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if(!ItemUtils.isOre(event.getBlock().getType())) return;
        if(!event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

        NmsUtils.getInstance().decreaseStatistic(event.getPlayer(), Statistic.MINE_BLOCK, event.getBlock().getType());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.stats.remove(event.getPlayer().getUniqueId());
    }

    @Getter
    @Setter
    private static class StatsItem {

        private String name;
        private String replace;
        private ItemStack item;
        private int slot;
    }
}

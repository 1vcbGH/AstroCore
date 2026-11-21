package me.vcb.astrocore.factions.leaderboard;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.handlers.leaderboard.entry.UuidCacheEntry;
import me.vcb.astrocore.handlers.leaderboard.type.FactionLeaderboardType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;

/** /f top (sin args) — GUI con los tops de facciones (kills/points/balance/koths). */
public class FactionLeaderboardGui implements Listener {

    private static final String TITLE = "§6§lTop de Facciones";
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);
    private static final int[] CATEGORY_SLOTS = {2, 3, 5, 6};

    private static FactionLeaderboardGui instance;

    // Resolved lazily — referencing FactionLeaderboardType at class-load would
    // trigger its static init before the leaderboard handler exists (NPE).
    private FactionLeaderboardType catType(int c) {
        return switch (c) {
            case 0 -> FactionLeaderboardType.FACTION_KILLS;
            case 1 -> FactionLeaderboardType.FACTION_POINTS;
            case 2 -> FactionLeaderboardType.FACTION_BALANCE;
            default -> FactionLeaderboardType.FACTION_KOTHS_CAPPED;
        };
    }
    private static final int CAT_COUNT = 4;

    public FactionLeaderboardGui() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static FactionLeaderboardGui getInstance() { return instance; }

    public void open(Player player, int category) {
        if (category < 0 || category >= CAT_COUNT) category = 0;
        FactionLeaderboardType type = catType(category);

        Inventory inv = Bukkit.createInventory(null, 54, Color.translate(TITLE + " §8» §7" + label(category)));
        var border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        // Category selectors.
        for (int i = 0; i < CAT_COUNT; i++) {
            boolean sel = i == category;
            ItemBuilder b = new ItemBuilder(catIcon(i))
                .setName(Color.translate((sel ? "&a&l" : "&7") + label(i)))
                .setLore(List.of(Color.translate(sel ? "&aSeleccionado" : "&eClick para ver")));
            if (sel) b.addFakeGlow();
            inv.setItem(CATEGORY_SLOTS[i], b.build());
        }

        // Top 10 entries.
        NavigableSet<UuidCacheEntry<Integer>> board = type.getLeaderboard();
        int rank = 1, slot = 18;
        for (UuidCacheEntry<Integer> entry : board) {
            if (rank > 10) break;
            Faction faction = FactionsManager.getInstance().getFactionByUuid(entry.getKey());
            String name = faction != null ? faction.getName() : entry.getName();
            String valueStr = category == 2 ? "$" + NF.format(entry.getValue()) : NF.format(entry.getValue());

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&7" + capitalLabel(category) + ": &f" + valueStr));
            inv.setItem(slot++, new ItemBuilder(rankMaterial(rank))
                .setName(Color.translate("&e#" + rank + " &f" + name))
                .setLore(lore).build());
            rank++;
        }
        if (rank == 1) {
            inv.setItem(22, new ItemBuilder(Material.BARRIER)
                .setName(Color.translate("&cSin datos todavía")).build());
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().startsWith(Color.translate(TITLE))) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        for (int i = 0; i < CATEGORY_SLOTS.length; i++)
            if (CATEGORY_SLOTS[i] == slot) { open(player, i); return; }
    }

    private static String label(int c) {
        return switch (c) { case 0 -> "Kills"; case 1 -> "Puntos"; case 2 -> "Balance"; default -> "KOTHs"; };
    }
    private static String capitalLabel(int c) { return label(c); }
    private static Material catIcon(int c) {
        return switch (c) {
            case 0 -> Material.DIAMOND_SWORD;
            case 1 -> Material.NETHER_STAR;
            case 2 -> Material.GOLD_INGOT;
            default -> Material.BEACON;
        };
    }
    private static Material rankMaterial(int rank) {
        return switch (rank) {
            case 1 -> Material.GOLD_BLOCK;
            case 2 -> Material.IRON_BLOCK;
            case 3 -> Material.COPPER_BLOCK;
            default -> Material.PAPER;
        };
    }
}

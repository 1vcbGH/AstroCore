package me.vcb.astrocore.partner.menu;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.partner.Partner;
import me.vcb.astrocore.partner.PartnerManager;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartnerMenu implements Listener {

    private static final String TITLE = "§6§lPartners §8| §eApoyanos!";
    private static final int SIZE = 54;

    // Slots in the inner 4x7 area (rows 1-4, columns 1-7, 0-indexed)
    private static final int[] INNER_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private static final ItemStack FILLER;
    private static final ItemStack ORANGE_PANE;

    static {
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = gray.getItemMeta();
        gm.setDisplayName(" ");
        gray.setItemMeta(gm);
        FILLER = gray;

        ItemStack orange = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta om = orange.getItemMeta();
        om.setDisplayName(" ");
        orange.setItemMeta(om);
        ORANGE_PANE = orange;
    }

    // slot -> partnerNick for click resolution
    private final Map<Integer, String> slotToPartner = new HashMap<>();

    public PartnerMenu() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        slotToPartner.clear();

        // Fill all with gray panes first
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, FILLER);
        }

        // Border rows (0 and 5) get orange panes
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, ORANGE_PANE);
            inv.setItem(45 + i, ORANGE_PANE);
        }
        // Border columns (0 and 8) for middle rows
        for (int row = 1; row <= 4; row++) {
            inv.setItem(row * 9, ORANGE_PANE);
            inv.setItem(row * 9 + 8, ORANGE_PANE);
        }

        PartnerManager manager = AstroCore.getInstance().getPartnerManager();
        List<Partner> visible = manager.getVisiblePartnersSorted();

        // Center partners: if fewer than 7 in a row, offset to center
        int count = Math.min(visible.size(), INNER_SLOTS.length);

        // Calculate centered starting offset within inner slots
        // We have up to 4 rows x 7 cols = 28 inner slots
        // If count <= 7, put on row 2 (middle-ish), centered
        // Otherwise fill normally from top-left of inner area
        int[] slotsToUse;
        if (count <= 7) {
            // Single row centered in row 2 of inner area (inner row index 1 = slots 19-25)
            int rowStart = 19;
            int offset = (7 - count) / 2;
            slotsToUse = new int[count];
            for (int i = 0; i < count; i++) {
                slotsToUse[i] = rowStart + offset + i;
            }
        } else if (count <= 14) {
            // Two rows centered
            int row1Count = Math.min(7, count);
            int row2Count = count - row1Count;
            slotsToUse = new int[count];
            int off1 = (7 - row1Count) / 2;
            int off2 = (7 - row2Count) / 2;
            for (int i = 0; i < row1Count; i++) slotsToUse[i] = 19 + off1 + i;
            for (int i = 0; i < row2Count; i++) slotsToUse[row1Count + i] = 28 + off2 + i;
        } else {
            // Fill all inner slots normally
            slotsToUse = Arrays.copyOf(INNER_SLOTS, count);
        }

        for (int i = 0; i < count; i++) {
            Partner partner = visible.get(i);
            int invSlot = slotsToUse[i];
            int rank = i + 1;

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(partner.getNick()));
            skull.setItemMeta(skullMeta);

            List<String> lore = Arrays.asList(
                "§7Rank: §e#" + rank,
                "§7Usos: §a" + partner.getClaimCount(),
                "",
                "§8» §eClick para apoyar §ay reclamar recompensas!"
            );

            skull = new ItemBuilder(skull)
                .setName(partner.getDisplayName())
                .setLore(lore)
                .build();

            inv.setItem(invSlot, skull);
            slotToPartner.put(invSlot, partner.getNick());
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!TITLE.equals(event.getView().getTitle())) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        String nick = slotToPartner.get(slot);
        if (nick == null) return;

        Player player = (Player) event.getWhoClicked();
        PartnerManager manager = AstroCore.getInstance().getPartnerManager();
        Partner partner = manager.getPartner(nick);
        if (partner == null) return;

        if (manager.isOnCooldown(player.getUniqueId(), partner.getNick())) {
            player.sendMessage("§cYa reclamaste la recompensa de este partner. Espera 24 horas.");
            return;
        }

        manager.claimPartner(player, partner);
        player.sendMessage("§aHas reclamado la recompensa de §e" + partner.getDisplayName() + "§a!");
    }
}

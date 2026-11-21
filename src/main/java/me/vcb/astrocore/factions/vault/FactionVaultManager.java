package me.vcb.astrocore.factions.vault;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.InventoryUtils;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent paginated faction vaults:
 *   /f vault       — shared chest for the whole faction.
 *   /f leadervault — shared chest only for Co-Leaders+.
 * Each page holds 45 slots (bottom row = navigation). Pages are added with the
 * vault upgrade in /f upgrades. Contents persist to the faction data on close.
 */
public class FactionVaultManager implements Listener {

    private static final String TITLE_FACTION = "§5Cofre de Facción";
    private static final String TITLE_LEADER  = "§6Cofre de Líderes";

    private static final int PAGE_SLOTS = 45;          // storage slots per page
    private static final int PREV_SLOT = 45, INFO_SLOT = 49, NEXT_SLOT = 53;

    private static FactionVaultManager instance;

    private final Map<UUID, Session> sessions = new HashMap<>();

    public FactionVaultManager() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static FactionVaultManager getInstance() { return instance; }

    private static final class Session {
        UUID factionId;
        boolean leader;
        int pages;
        int page;
        ItemStack[] full; // pages * PAGE_SLOTS
        Inventory inv;
    }

    // ── Open ──────────────────────────────────────────────────────────────

    public void openFaction(Player player) { open(player, false); }
    public void openLeader(Player player)  { open(player, true); }

    private void open(Player player, boolean leaderVault) {
        PlayerFaction fac = FactionsManager.getInstance().getPlayerFaction(player);
        if (fac == null) { player.sendMessage(Color.translate("&cNo estás en una facción.")); return; }
        if (leaderVault && !fac.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Color.translate("&cSolo Co-Líder o superior puede abrir el cofre de líderes."));
            return;
        }

        int pages = fac.getVaultPages();
        ItemStack[] full = new ItemStack[pages * PAGE_SLOTS];
        String data = leaderVault ? fac.getLeaderVault() : fac.getFactionVault();
        if (data != null && !data.isEmpty()) {
            ItemStack[] stored = InventoryUtils.itemStackArrayFromBase64(data);
            if (stored != null) System.arraycopy(stored, 0, full, 0, Math.min(stored.length, full.length));
        }

        Session s = new Session();
        s.factionId = fac.getId();
        s.leader = leaderVault;
        s.pages = pages;
        s.page = 0;
        s.full = full;
        s.inv = Bukkit.createInventory(null, 54, Color.translate(leaderVault ? TITLE_LEADER : TITLE_FACTION));

        this.sessions.put(player.getUniqueId(), s);
        render(s);
        player.openInventory(s.inv);
    }

    /** Admin reset: clears the selected vault's items and resets the page level. */
    public void reset(PlayerFaction fac, boolean leader) {
        // Discard any open sessions for this faction + vault so they don't re-save.
        java.util.List<UUID> toClose = new java.util.ArrayList<>();
        for (Map.Entry<UUID, Session> e : this.sessions.entrySet()) {
            Session s = e.getValue();
            if (s.factionId.equals(fac.getId()) && s.leader == leader) toClose.add(e.getKey());
        }
        for (UUID uuid : toClose) {
            this.sessions.remove(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.closeInventory();
        }

        if (leader) fac.setLeaderVault(null); else fac.setFactionVault(null);
        fac.setUpgradeVaultSize(0);
        FactionsManager.getInstance().saveFactions(false, false);
    }

    private void render(Session s) {
        // storage
        for (int i = 0; i < PAGE_SLOTS; i++) s.inv.setItem(i, s.full[s.page * PAGE_SLOTS + i]);
        // nav row
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = PAGE_SLOTS; i < 54; i++) s.inv.setItem(i, filler);

        if (s.page > 0)
            s.inv.setItem(PREV_SLOT, new ItemBuilder(Material.ARROW).setName(Color.translate("&e« Página anterior")).build());
        if (s.page < s.pages - 1)
            s.inv.setItem(NEXT_SLOT, new ItemBuilder(Material.ARROW).setName(Color.translate("&eSiguiente página »")).build());

        s.inv.setItem(INFO_SLOT, new ItemBuilder(Material.BOOK)
            .setName(Color.translate("&fPágina &e" + (s.page + 1) + "&7/&e" + s.pages)).build());
    }

    private void savePage(Session s) {
        for (int i = 0; i < PAGE_SLOTS; i++) s.full[s.page * PAGE_SLOTS + i] = s.inv.getItem(i);
    }

    // ── Clicks ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Session s = this.sessions.get(player.getUniqueId());
        if (s == null || !event.getInventory().equals(s.inv)) return;

        int raw = event.getRawSlot();
        if (raw < 0 || raw >= 54) return; // clicking own inventory — allow

        // Navigation row is protected.
        if (raw >= PAGE_SLOTS) {
            event.setCancelled(true);
            if (raw == PREV_SLOT && s.page > 0) { savePage(s); s.page--; render(s); }
            else if (raw == NEXT_SLOT && s.page < s.pages - 1) { savePage(s); s.page++; render(s); }
        }
        // storage slots (0-44): allowed
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Session s = this.sessions.get(player.getUniqueId());
        if (s == null || !event.getInventory().equals(s.inv)) return;
        for (int raw : event.getRawSlots()) {
            if (raw >= PAGE_SLOTS && raw < 54) { event.setCancelled(true); return; }
        }
    }

    // ── Save on close ─────────────────────────────────────────────────────

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Session s = this.sessions.remove(player.getUniqueId());
        if (s == null || !event.getInventory().equals(s.inv)) return;

        savePage(s);
        if (FactionsManager.getInstance().getFactionByUuid(s.factionId) instanceof PlayerFaction fac) {
            String b64 = InventoryUtils.itemStackArrayToBase64(s.full);
            if (s.leader) fac.setLeaderVault(b64); else fac.setFactionVault(b64);
            FactionsManager.getInstance().saveFactions(false, false);
        }
    }
}

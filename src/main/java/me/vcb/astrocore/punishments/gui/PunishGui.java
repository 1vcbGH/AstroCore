package me.vcb.astrocore.punishments.gui;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.punishments.Punishment;
import me.vcb.astrocore.punishments.PunishmentCategory;
import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.punishments.PunishmentUtil;
import me.vcb.astrocore.sanctions.SanctionType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI to apply categorised punishments with automatic offense escalation.
 * Each category auto-selects the next offense tier (1ª/2ª/3ª...) based on the
 * player's prior offenses of that category. Opened via /punish &lt;nick&gt;.
 */
public class PunishGui implements Listener {

    private static final String TITLE_PREFIX = "§4§lCastigar: §f";

    private static final int INFO_SLOT   = 45;
    private static final int SILENT_SLOT = 47;
    private static final int UNBAN_SLOT  = 48;
    private static final int UNMUTE_SLOT = 50;
    private static final int CLOSE_SLOT  = 53;

    private static PunishGui instance;

    private record Target(UUID uuid, String name) {}
    private final Map<UUID, Target> open = new HashMap<>();
    // Per-staff silent-mode toggle (persists while the staff session is open)
    private final Map<UUID, Boolean> silentMode = new HashMap<>();

    public PunishGui() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static PunishGui getInstance() { return instance; }

    // ── Open ──────────────────────────────────────────────────────────────

    public void open(Player staff, UUID targetUuid, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Color.translate(TITLE_PREFIX + targetName));

        ItemBuilder border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r");
        for (int i = 0; i < 54; i++) inv.setItem(i, border.build());

        PunishmentManager pm = PunishmentManager.getInstance();
        List<PunishmentCategory> cats = PunishmentCategory.all();

        // Categories in slots 0.. (skip bottom control row)
        for (int i = 0; i < cats.size() && i < INFO_SLOT; i++) {
            PunishmentCategory cat = cats.get(i);
            int prior = pm.countOffenses(targetUuid, cat.id());
            int nextTier = Math.min(prior, cat.tierCount() - 1); // 0-based
            int nextOffense = nextTier + 1;

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("§7" + cat.getDescription()));
            lore.add(Color.translate("§7Tipo: " + (cat.getType() == SanctionType.MUTE ? "§eMute" : "§cBan")
                + " §8| §7Apelable: " + (cat.isAppealable() ? "§aSí" : "§cNo")));
            lore.add(Color.translate("§8──────────────────"));
            for (int t = 0; t < cat.tierCount(); t++) {
                long dur = cat.durationForTier(t);
                String durStr = dur <= 0 ? "Ban permanente" : PunishmentUtil.formatRemaining(dur);
                String mark = (t == nextTier) ? "§a➤ " : "§8";
                lore.add(Color.translate(mark + "Ofensa #" + (t + 1) + ": §f" + durStr));
            }
            lore.add(Color.translate("§8──────────────────"));
            lore.add(Color.translate("§7Ofensas previas: §f" + prior));
            lore.add(Color.translate("§eClick: aplicar Ofensa #" + nextOffense));

            inv.setItem(i, new ItemBuilder(cat.getIcon())
                .setName(Color.translate((cat.getType() == SanctionType.MUTE ? "§e§l" : "§c§l") + cat.getDisplayName()))
                .setLore(lore).build());
        }

        // Bottom controls
        inv.setItem(INFO_SLOT, buildInfoHead(targetUuid, targetName));

        boolean silent = silentMode.getOrDefault(staff.getUniqueId(), false);
        inv.setItem(SILENT_SLOT, new ItemBuilder(silent ? Material.SCULK_SENSOR : Material.NOTE_BLOCK)
            .setName(Color.translate(silent ? "§b§lSanción Silenciosa: §aACTIVADA" : "§7§lSanción Silenciosa: §cDESACTIVADA"))
            .setLore(List.of(
                Color.translate("§7Si está activada, las sanciones"),
                Color.translate("§7solo las ve el staff (no broadcast)."),
                Color.translate("§8──────────────────"),
                Color.translate("§eClick para " + (silent ? "desactivar" : "activar"))
            )).build());

        inv.setItem(UNBAN_SLOT, new ItemBuilder(Material.LIME_DYE)
            .setName(Color.translate("§a§lQuitar Baneo"))
            .setLore(List.of(Color.translate("§eClick para desbanear"))).build());
        inv.setItem(UNMUTE_SLOT, new ItemBuilder(Material.GREEN_DYE)
            .setName(Color.translate("§a§lQuitar Mute"))
            .setLore(List.of(Color.translate("§eClick para des-silenciar"))).build());
        inv.setItem(CLOSE_SLOT, new ItemBuilder(Material.BARRIER)
            .setName(Color.translate("§c§lCerrar")).build());

        open.put(staff.getUniqueId(), new Target(targetUuid, targetName));
        staff.openInventory(inv);
    }

    private org.bukkit.inventory.ItemStack buildInfoHead(UUID uuid, String name) {
        PunishmentManager pm = PunishmentManager.getInstance();
        int bans = 0, mutes = 0, warns = 0;
        for (Punishment p : pm.getPunishments(uuid)) {
            switch (p.getType()) {
                case BAN, TEMPBAN, BLACKLIST -> bans++;
                case MUTE, TEMPMUTE -> mutes++;
                case WARN -> warns++;
                default -> {}
            }
        }
        Punishment activeBan  = pm.getActiveBan(uuid);
        Punishment activeMute = pm.getActiveMute(uuid);

        List<String> lore = new ArrayList<>();
        lore.add(Color.translate("§8──────────────────"));
        lore.add(Color.translate("§7Baneos: §f" + bans + " §8| §7Mutes: §f" + mutes + " §8| §7Warns: §f" + warns));
        lore.add(Color.translate("§7Ban activo: " + (activeBan != null
            ? "§c" + (activeBan.isPermanent() ? "Permanente" : PunishmentUtil.formatRemaining(activeBan.remainingMillis()))
            : "§aNinguno")));
        lore.add(Color.translate("§7Mute activo: " + (activeMute != null
            ? "§e" + (activeMute.isPermanent() ? "Permanente" : PunishmentUtil.formatRemaining(activeMute.remainingMillis()))
            : "§aNinguno")));
        return new ItemBuilder(Material.PLAYER_HEAD)
            .setName(Color.translate("§e§l" + name))
            .setLore(lore).build();
    }

    // ── Click ─────────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player staff)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(Color.translate(TITLE_PREFIX))) return;

        event.setCancelled(true);
        Target target = open.get(staff.getUniqueId());
        if (target == null) { staff.closeInventory(); return; }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        PunishmentManager pm = PunishmentManager.getInstance();
        String staffName = staff.getName();

        if (slot == CLOSE_SLOT) { staff.closeInventory(); return; }

        boolean silent = silentMode.getOrDefault(staff.getUniqueId(), false);

        if (slot == SILENT_SLOT) {
            silentMode.put(staff.getUniqueId(), !silent);
            open(staff, target.uuid(), target.name());
            return;
        }

        if (slot == UNBAN_SLOT) {
            boolean changed = pm.unban(target.uuid(), staffName);
            if (changed) announce(staff, silent, "&8[&aUnban&8] &e" + target.name() + " &7fue desbaneado por &f" + staffName);
            else staff.sendMessage(Color.translate("&e" + target.name() + " &7no tenía baneos activos."));
            open(staff, target.uuid(), target.name());
            return;
        }
        if (slot == UNMUTE_SLOT) {
            boolean changed = pm.unmute(target.uuid(), staffName);
            if (changed) announce(staff, silent, "&8[&aUnmute&8] &e" + target.name() + " &7fue des-silenciado por &f" + staffName);
            else staff.sendMessage(Color.translate("&e" + target.name() + " &7no estaba silenciado."));
            open(staff, target.uuid(), target.name());
            return;
        }

        // Category slot
        List<PunishmentCategory> cats = PunishmentCategory.all();
        if (slot < 0 || slot >= cats.size() || slot >= INFO_SLOT) return;
        PunishmentCategory cat = cats.get(slot);

        int offense = pm.applyCategory(target.uuid(), target.name(), staffName, cat);
        long dur = cat.durationForTier(offense - 1);
        String durStr = dur <= 0 ? "Permanente" : PunishmentUtil.formatRemaining(dur);
        String typeLabel = cat.getType() == SanctionType.MUTE ? "&eMute" : "&cBan";
        announce(staff, silent, "&8[&4Sanción&8] &e" + target.name()
            + " &7→ " + typeLabel + " &7por &f" + cat.getDisplayName()
            + " &8(&7Ofensa #" + offense + " · " + durStr + "&8) &7por &f" + staffName);
        staff.closeInventory();
    }

    /** Broadcast to everyone, or only to staff if silent. */
    private void announce(Player actor, boolean silent, String coloredMsg) {
        String msg = Color.translate(coloredMsg);
        if (silent) {
            for (Player online : Bukkit.getOnlinePlayers())
                if (online.hasPermission("astrocore.staff"))
                    online.sendMessage(Color.translate("&7[&cSilencioso&7] ") + msg);
        } else {
            Bukkit.broadcastMessage(msg);
        }
    }
}

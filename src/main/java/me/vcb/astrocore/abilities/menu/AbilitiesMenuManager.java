package me.vcb.astrocore.abilities.menu;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.abilities.AbilitiesManager;
import me.vcb.astrocore.abilities.AbilityItem;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles /abilities menu (view) and /abilities menuadmin (edit enabled state).
 *
 * Config persisted in plugins/AstroCore/abilities_menu.yml:
 *   SLOT_<AbilityType.name()>: int  (inventory slot, 0-based)
 */
public class AbilitiesMenuManager implements Listener {

    private static final String MENU_TITLE      = Color.translate("&5&lAbilities &8| &7Season abilities");
    private static final String ADMIN_TITLE     = Color.translate("&c&lAbility Admin &8| &7Click to toggle");
    private static final int    MENU_SIZE       = 54;

    private static final ItemStack FILLER = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .setName(" ").build();

    private final File configFile;
    private YamlConfiguration menuConfig;

    /** Tracks which players have the admin menu open */
    private final Set<UUID> adminViewers = new HashSet<>();

    public AbilitiesMenuManager() {
        this.configFile = new File(AstroCore.getInstance().getDataFolder(), "abilities_menu.yml");
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    // ─── Config ──────────────────────────────────────────────────────────────

    private void loadConfig() {
        this.menuConfig = this.configFile.exists()
                ? YamlConfiguration.loadConfiguration(this.configFile)
                : new YamlConfiguration();
    }

    private int getSlot(AbilityType type) {
        return this.menuConfig.getInt("SLOT_" + type.name(), -1);
    }

    private void setSlot(AbilityType type, int slot) {
        this.menuConfig.set("SLOT_" + type.name(), slot);
        this.save();
    }

    private void save() {
        try {
            this.menuConfig.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── Public open methods ──────────────────────────────────────────────────

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);
        this.fillFiller(inv);
        this.placeAbilityItems(inv, false);
        player.openInventory(inv);
    }

    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, ADMIN_TITLE);
        this.fillFiller(inv);
        this.placeAbilityItems(inv, true);
        this.adminViewers.add(player.getUniqueId());
        player.openInventory(inv);
    }

    // ─── Build inventory ──────────────────────────────────────────────────────

    private void fillFiller(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, FILLER);
        }
    }

    private void placeAbilityItems(Inventory inv, boolean admin) {
        AbilitiesManager mgr = AbilitiesManager.getInstance();

        // Auto-assign slots if not configured (row by row, skip filler edges)
        int autoSlot = 10; // start at second row, second col
        for (AbilityType type : AbilityType.values()) {
            if (type == AbilityType.TIME_WARP) continue; // TODO stub

            AbilityItem abilityItem = mgr.getAbilityItemByType(type);
            boolean enabled = abilityItem != null;

            int slot = this.getSlot(type);
            if (slot < 0 || slot >= MENU_SIZE) {
                slot = this.nextValidSlot(autoSlot);
                autoSlot = slot + 1;
                this.setSlot(type, slot);
            }

            ItemStack display = this.buildDisplayItem(type, abilityItem, enabled, admin);
            inv.setItem(slot, display);
        }
    }

    private int nextValidSlot(int from) {
        int slot = from;
        while (slot < MENU_SIZE) {
            int col = slot % 9;
            if (col != 0 && col != 8) return slot; // avoid border cols
            slot++;
        }
        return Math.min(from, MENU_SIZE - 1);
    }

    private ItemStack buildDisplayItem(AbilityType type, AbilityItem abilityItem, boolean enabled, boolean admin) {
        Material mat;
        if (enabled && abilityItem.getItem() != null) {
            mat = abilityItem.getItem().getType();
        } else {
            mat = Material.BARRIER;
        }

        String name = enabled && abilityItem != null
                ? abilityItem.getItem().getItemMeta().getDisplayName()
                : ChatColor.RED + type.getName();

        List<String> lore = new ArrayList<>();
        if (enabled && abilityItem != null && abilityItem.getItem().getItemMeta().hasLore()) {
            lore.addAll(abilityItem.getItem().getItemMeta().getLore());
        }

        if (admin) {
            lore.add("");
            lore.add(enabled
                    ? ChatColor.GREEN + "✔ Enabled " + ChatColor.GRAY + "— click to disable"
                    : ChatColor.RED   + "✘ Disabled " + ChatColor.GRAY + "— click to enable");
        } else {
            lore.add("");
            lore.add(enabled ? ChatColor.GREEN + "✔ Available this season" : ChatColor.RED + "✘ Not available");
        }

        if (enabled && abilityItem != null) {
            lore.add(ChatColor.YELLOW + "Cooldown: " + ChatColor.WHITE + abilityItem.getCooldown() + "s");
        }

        return new ItemBuilder(mat).setName(name).setLore(lore).build();
    }

    // ─── Events ───────────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();

        if (title.equals(MENU_TITLE)) {
            event.setCancelled(true);
            return;
        }

        if (!title.equals(ADMIN_TITLE)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        // Find which type was clicked by slot
        int slot = event.getSlot();
        AbilityType found = null;
        for (AbilityType type : AbilityType.values()) {
            if (this.getSlot(type) == slot) {
                found = type;
                break;
            }
        }
        if (found == null) return;

        // Toggle in abilities.yml and reload
        AbilitiesManager mgr = AbilitiesManager.getInstance();
        boolean currentlyEnabled = mgr.getAbilityItemByType(found) != null;

        // Flip ENABLED in the live ConfigFile (in-memory) then save to disk
        me.vcb.astrocore.config.ConfigFile abilitiesCfg = AstroCore.getInstance().getAbilitiesFile();
        String sectionKey = this.findSectionKey(abilitiesCfg, found);
        if (sectionKey == null) {
            player.sendMessage(ChatColor.RED + "Could not find config section for " + found.getName());
            return;
        }

        boolean newState = !currentlyEnabled;
        abilitiesCfg.set(sectionKey + ".ENABLED", newState);
        abilitiesCfg.save(); // saves to disk; in-memory values already updated

        // Reload abilities
        mgr.disable();
        mgr.setupAbilityItems();

        player.sendMessage(Color.translate("&7Ability &5" + found.getName() + " &7is now "
                + (newState ? "&aenabled" : "&cdisabled") + "&7."));

        // Refresh the admin menu
        this.openAdminMenu(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ADMIN_TITLE)) {
            this.adminViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    private String findSectionKey(YamlConfiguration cfg, AbilityType type) {
        // AbilityType name: "AggressivePearl" → config key: "AGGRESSIVE_PEARL"
        String configKey = type.getName()
                .replaceAll("(?<=[a-z])(?=[A-Z])", "_")
                .toUpperCase();
        if (cfg.contains(configKey)) return configKey;
        // Fallback: search all top-level keys
        for (String key : cfg.getKeys(false)) {
            if (key.equalsIgnoreCase(configKey)) return key;
        }
        return null;
    }
}

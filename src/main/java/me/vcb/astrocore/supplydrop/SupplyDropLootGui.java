package me.vcb.astrocore.supplydrop;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI para configurar el loot de los Supply Drops.
 * Los items que se ponen en el inventario se guardan automáticamente al cerrar.
 * Cada item se registra con peso configurable via clic derecho.
 */
public class SupplyDropLootGui implements Listener {

    private static final String TITLE = "§6§l✈ Supply Drop §8— §eLoot";
    private static final int SIZE = 54;
    // Slots de loot: rows 1-5 excluyendo bordes
    private static final int[] LOOT_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };
    private static final int ITEMS_PER_DROP_SLOT = 49;
    private static final int SAVE_SLOT            = 52;
    private static final int INFO_SLOT            = 4;

    // Tracks which players have this GUI open
    private static final Map<UUID, Inventory> openGuis = new HashMap<>();

    public SupplyDropLootGui() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void openFor(Player player) {
        SupplyDropManager mgr = AstroCore.getInstance().getSupplyDropManager();

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        // Border
        ItemStack border = border();
        for (int i = 0; i < SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, border);
        }

        // Info item
        inv.setItem(INFO_SLOT, makeInfo(mgr));

        // Items-per-drop button
        inv.setItem(ITEMS_PER_DROP_SLOT, makeItemsPerDropBtn(mgr.getItemsPerDrop()));

        // Save button
        inv.setItem(SAVE_SLOT, makeSaveBtn());

        // Load current loot into slots
        List<String> entries = mgr.getLootEntries();
        for (int i = 0; i < entries.size() && i < LOOT_SLOTS.length; i++) {
            String[] parts = entries.get(i).split(":");
            if (parts.length < 2) continue;
            Material mat = Material.matchMaterial(parts[0]);
            if (mat == null) continue;
            int amount = safeInt(parts[1], 1);
            int weight = parts.length >= 3 ? safeInt(parts[2], 10) : 10;
            inv.setItem(LOOT_SLOTS[i], makeLootItem(mat, amount, weight));
        }

        openGuis.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory gui = openGuis.get(player.getUniqueId());
        if (gui == null || !event.getView().getTitle().equals(TITLE)) return;

        int slot = event.getRawSlot();

        // Outside inventory — player returning items from cursor: allow
        if (slot < 0) return;

        // Clicks in player's own inventory (bottom half): allow picking items
        if (slot >= SIZE) return;

        // Cancel clicks on border / special buttons
        if (isBorderSlot(slot)) {
            event.setCancelled(true);
            return;
        }

        // Items-per-drop button: left = +1, right = -1
        if (slot == ITEMS_PER_DROP_SLOT) {
            event.setCancelled(true);
            SupplyDropManager mgr = AstroCore.getInstance().getSupplyDropManager();
            int current = mgr.getItemsPerDrop();
            int newVal;
            if (event.isRightClick()) {
                newVal = Math.max(1, current - 1);
            } else {
                newVal = Math.min(27, current + 1);
            }
            mgr.setItemsPerDrop(newVal);
            event.getInventory().setItem(ITEMS_PER_DROP_SLOT, makeItemsPerDropBtn(newVal));
            event.getInventory().setItem(INFO_SLOT, makeInfo(mgr));
            player.sendMessage(Color.translate("&aItems por drop: &e" + newVal));
            return;
        }

        // Save button
        if (slot == SAVE_SLOT) {
            event.setCancelled(true);
            saveFromGui(player, event.getInventory());
            return;
        }

        // Info slot — cancel
        if (slot == INFO_SLOT) {
            event.setCancelled(true);
            return;
        }

        // Loot slots: allow placing/taking items freely
        // Right-click on an existing loot item: change weight
        if (event.isRightClick() && isLootSlot(slot)) {
            ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR && isLootItem(current)) {
                event.setCancelled(true);
                int weight = getWeight(current);
                int newWeight = (weight >= 100) ? 5 : weight + 5;
                setWeight(current, newWeight);
                event.getInventory().setItem(slot, current);
                player.sendMessage(Color.translate("&7Peso cambiado a &e" + newWeight));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openGuis.containsKey(player.getUniqueId())) return;
        if (!event.getView().getTitle().equals(TITLE)) return;

        // Cancel drag if any slot is a special/border slot
        for (int slot : event.getRawSlots()) {
            if (slot < SIZE && (isBorderSlot(slot) || slot == INFO_SLOT || slot == ITEMS_PER_DROP_SLOT || slot == SAVE_SLOT)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!openGuis.containsKey(player.getUniqueId())) return;
        if (!event.getView().getTitle().equals(TITLE)) return;

        openGuis.remove(player.getUniqueId());
        saveFromGui(player, event.getInventory());
    }

    // ── Save logic ────────────────────────────────────────────────────────

    private void saveFromGui(Player player, Inventory inv) {
        SupplyDropManager mgr = AstroCore.getInstance().getSupplyDropManager();
        List<String> entries = new ArrayList<>();

        for (int slot : LOOT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (!isLootItem(item)) {
                // Plain item placed by the admin — default weight 10
                int weight = 10;
                entries.add(item.getType().name() + ":" + item.getAmount() + ":" + weight);
            } else {
                int weight = getWeight(item);
                entries.add(item.getType().name() + ":" + item.getAmount() + ":" + weight);
            }
        }

        mgr.setLootEntries(entries);
        player.sendMessage(Color.translate("&a✔ Loot del supply drop guardado (&e" + entries.size() + " items&a)."));
    }

    // ── Item builders ─────────────────────────────────────────────────────

    private ItemStack makeLootItem(Material mat, int amount, int weight) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate("&e" + formatMaterial(mat)));
        meta.setLore(Arrays.asList(
            Color.translate("&7Cantidad: &f" + amount),
            Color.translate("&7Peso (probabilidad): &f" + weight),
            Color.translate(""),
            Color.translate("&8Clic derecho &7para cambiar el peso (+5)"),
            Color.translate("&8Doble clic / arrastrar &7para quitar")
        ));
        // Tag it as a loot item
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(AstroCore.getInstance(), "supply_loot_weight"),
            org.bukkit.persistence.PersistentDataType.INTEGER, weight
        );
        item.setItemMeta(meta);
        return item;
    }

    private boolean isLootItem(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
            new org.bukkit.NamespacedKey(AstroCore.getInstance(), "supply_loot_weight"),
            org.bukkit.persistence.PersistentDataType.INTEGER
        );
    }

    private int getWeight(ItemStack item) {
        if (!item.hasItemMeta()) return 10;
        Integer w = item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(AstroCore.getInstance(), "supply_loot_weight"),
            org.bukkit.persistence.PersistentDataType.INTEGER
        );
        return w != null ? w : 10;
    }

    private void setWeight(ItemStack item, int weight) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList(
            Color.translate("&7Cantidad: &f" + item.getAmount()),
            Color.translate("&7Peso (probabilidad): &f" + weight),
            Color.translate(""),
            Color.translate("&8Clic derecho &7para cambiar el peso (+5)"),
            Color.translate("&8Doble clic / arrastrar &7para quitar")
        ));
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(AstroCore.getInstance(), "supply_loot_weight"),
            org.bukkit.persistence.PersistentDataType.INTEGER, weight
        );
        item.setItemMeta(meta);
    }

    private ItemStack makeInfo(SupplyDropManager mgr) {
        ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate("&6&l✈ Supply Drop Loot Editor"));
        meta.setLore(Arrays.asList(
            Color.translate("&8&m──────────────────────"),
            Color.translate("&7Arrastrá items del inventario"),
            Color.translate("&7a los slots para agregarlos."),
            Color.translate(""),
            Color.translate("&8Clic derecho &7en un item = cambiar peso"),
            Color.translate("&7Mayor peso = mayor probabilidad"),
            Color.translate(""),
            Color.translate("&7Items por drop: &e" + mgr.getItemsPerDrop()),
            Color.translate("&7Loot configurado: &e" + mgr.getLootEntries().size() + " items"),
            Color.translate("&8&m──────────────────────"),
            Color.translate("&7Se guarda al cerrar el menú.")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeItemsPerDropBtn(int current) {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate("&eItems por Supply Drop: &6" + current));
        meta.setLore(Arrays.asList(
            Color.translate("&7Clic izquierdo: &a+1"),
            Color.translate("&7Clic derecho: &c-1"),
            Color.translate("&7Rango: &f1 &8— &f27")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSaveBtn() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.translate("&a&lGuardar loot"));
        meta.setLore(Arrays.asList(
            Color.translate("&7Guarda el loot actual."),
            Color.translate("&7(También se guarda al cerrar)")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack border() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        item.setItemMeta(meta);
        return item;
    }

    // ── Utils ─────────────────────────────────────────────────────────────

    private boolean isBorderSlot(int slot) {
        return slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8;
    }

    private boolean isLootSlot(int slot) {
        for (int s : LOOT_SLOTS) if (s == slot) return true;
        return false;
    }

    private String formatMaterial(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}

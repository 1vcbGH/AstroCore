package me.vcb.astrocore.shop;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Server item shop (HCF ecoshop style): a hub with category buttons, a buy menu
 * per category and a dedicated sell menu. Pink glowing border + balance head.
 * Config-driven via shop.yml.
 */
public class ShopManager implements Listener {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private static final String HUB_T  = "§dItems Shop";
    private static final String BUY_T  = "§dComprar";
    private static final String SELL_T = "§cVender";

    private static final int[] CONTENT = {
        10,11,12,13,14,15,16, 19,20,21,22,23,24,25,
        28,29,30,31,32,33,34, 37,38,39,40,41,42,43 };

    public static class ShopItem { Material material; int buy = -1, sell = -1, amount = 16; }
    public static class Category { String id, name; Material icon; List<ShopItem> items = new ArrayList<>(); }

    private static ShopManager instance;
    private final File file;
    private final List<Category> categories = new ArrayList<>();
    private final Map<UUID, Integer> activeCat = new HashMap<>();

    public ShopManager() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "shop.yml");
        load();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static ShopManager getInstance() { return instance; }

    // ── Config ──────────────────────────────────────────────────────────────

    private void load() {
        if (!this.file.exists()) writeDefaults();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(this.file);
        this.categories.clear();
        ConfigurationSection cats = y.getConfigurationSection("categories");
        if (cats == null) return;
        for (String key : cats.getKeys(false)) {
            ConfigurationSection c = cats.getConfigurationSection(key);
            if (c == null) continue;
            Category cat = new Category();
            cat.id = key;
            cat.name = c.getString("name", key);
            cat.icon = matOr(c.getString("icon"), Material.CHEST);
            for (Map<?, ?> raw : c.getMapList("items")) {
                ShopItem si = new ShopItem();
                Object mat = raw.get("material");
                si.material = matOr(mat == null ? null : mat.toString(), null);
                if (si.material == null) continue;
                si.buy = raw.get("buy") instanceof Number n ? n.intValue() : -1;
                si.sell = raw.get("sell") instanceof Number n ? n.intValue() : -1;
                si.amount = raw.get("amount") instanceof Number n ? Math.max(1, n.intValue()) : 16;
                cat.items.add(si);
            }
            this.categories.add(cat);
        }
    }

    public void reload() { load(); }

    private Material matOr(String s, Material def) {
        if (s == null) return def;
        Material m = Material.matchMaterial(s.toUpperCase());
        return m == null ? def : m;
    }

    private void writeDefaults() {
        YamlConfiguration y = new YamlConfiguration();
        cat(y, "bloques", "&aBloques", "GRASS_BLOCK", new String[][]{
            {"STONE","128","16"}, {"COBBLESTONE","80","16"}, {"OAK_LOG","192","16"},
            {"GLASS","240","16"}, {"OBSIDIAN","4000","16"}, {"SAND","96","16"}});
        cat(y, "recursos", "&bRecursos", "DIAMOND", new String[][]{
            {"IRON_INGOT","1920","16"}, {"GOLD_INGOT","1600","16"}, {"DIAMOND","6400","16"},
            {"EMERALD","5600","16"}, {"REDSTONE","320","16"}, {"COAL","240","16"}, {"NETHERITE_INGOT","8000","1"}});
        cat(y, "comida", "&6Comida", "COOKED_BEEF", new String[][]{
            {"COOKED_BEEF","320","16"}, {"GOLDEN_APPLE","500","1"}, {"GOLDEN_CARROT","960","16"},
            {"BREAD","160","16"}, {"SUGAR","128","16"}});
        cat(y, "combate", "&cCombate", "DIAMOND_SWORD", new String[][]{
            {"ARROW","128","16"}, {"ENDER_PEARL","120","-1"}, {"EXPERIENCE_BOTTLE","30","-1"},
            {"TNT","200","-1"}, {"COBWEB","150","-1"}});
        try { y.save(this.file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void cat(YamlConfiguration y, String id, String name, String icon, String[][] items) {
        String b = "categories." + id + ".";
        y.set(b + "name", name);
        y.set(b + "icon", icon);
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] it : items) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("material", it[0]);
            m.put("buy", Integer.parseInt(it[1]));
            m.put("amount", Integer.parseInt(it[2]));
            // sell defaults to 25% of buy unit price.
            int unitBuy = Integer.parseInt(it[1]) / Math.max(1, Integer.parseInt(it[2]));
            m.put("sell", Integer.parseInt(it[2]) < 0 ? -1 : Math.max(1, unitBuy / 4));
            list.add(m);
        }
        y.set(b + "items", list);
    }

    // ── Hub ───────────────────────────────────────────────────────────────────

    public void openMain(Player player) {
        if (this.categories.isEmpty()) { player.sendMessage(Color.translate("&cLa tienda está vacía.")); return; }
        int bal = bal(player);
        Inventory inv = Bukkit.createInventory(null, 45, Color.translate(HUB_T + " &7┃ &f$" + NF.format(bal)));
        border(inv, 45);
        inv.setItem(4, balanceHead(player, bal));

        int n = this.categories.size();
        int[] row = {20, 21, 22, 23, 24};
        int start = Math.max(0, (row.length - n) / 2);
        for (int i = 0; i < n && start + i < row.length; i++) {
            Category c = this.categories.get(i);
            inv.setItem(row[start + i], new ItemBuilder(c.icon)
                .setName(Color.translate("&d&l" + Color.strip(Color.translate(c.name))))
                .setLore(List.of(
                    Color.translate("&7" + c.items.size() + " ítems"),
                    "",
                    Color.translate("&d➜ Click para comprar"))).addFakeGlow().build());
        }

        inv.setItem(40, new ItemBuilder(Material.HOPPER)
            .setName(Color.translate("&c&lVender Ítems"))
            .setLore(List.of(
                Color.translate("&7Vendé tus ítems por dinero."),
                "",
                Color.translate("&c➜ Click para abrir"))).addFakeGlow().build());

        player.openInventory(inv);
    }

    // ── Buy menu ────────────────────────────────────────────────────────────

    private void openBuy(Player player, int catIndex) {
        if (catIndex < 0 || catIndex >= this.categories.size()) catIndex = 0;
        this.activeCat.put(player.getUniqueId(), catIndex);
        Category cat = this.categories.get(catIndex);
        int bal = bal(player);
        Inventory inv = Bukkit.createInventory(null, 54,
            Color.translate(BUY_T + " &8» &7" + Color.strip(Color.translate(cat.name)) + " &7┃ &f$" + NF.format(bal)));
        border(inv, 54);
        inv.setItem(4, balanceHead(player, bal));

        for (int i = 0; i < cat.items.size() && i < CONTENT.length; i++) {
            ShopItem si = cat.items.get(i);
            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8&m                    "));
            lore.add(Color.translate(" &fPrecio &8» &d$" + NF.format(si.buy) + " &7por " + si.amount));
            lore.add(Color.translate("&8&m                    "));
            lore.add(si.buy >= 0 ? Color.translate(" &aClick &7» comprar &fx" + si.amount)
                : Color.translate(" &cNo comprable"));
            if (si.buy >= 0) lore.add(Color.translate(" &aShift &7» comprar &fx" + (si.amount * 4)));
            inv.setItem(CONTENT[i], new ItemBuilder(si.material).setAmount(Math.min(64, si.amount))
                .setName(Color.translate("&d&l" + niceName(si.material))).setLore(lore).build());
        }

        inv.setItem(49, me.vcb.astrocore.utils.Heads.skull(me.vcb.astrocore.utils.Heads.ARROW_LEFT,
            "&c« Volver", List.of(Color.translate("&7Volver al menú principal"))));
        inv.setItem(48, new ItemBuilder(Material.HOPPER).setName(Color.translate("&c&lVender Ítems")).build());
        player.openInventory(inv);
    }

    // ── Sell menu ─────────────────────────────────────────────────────────────

    private List<ShopItem> sellables() {
        List<ShopItem> list = new ArrayList<>();
        java.util.Set<Material> seen = new java.util.HashSet<>();
        for (Category c : this.categories)
            for (ShopItem si : c.items)
                if (si.sell >= 0 && seen.add(si.material)) list.add(si);
        return list;
    }

    private void openSell(Player player) {
        int bal = bal(player);
        Inventory inv = Bukkit.createInventory(null, 54, Color.translate(SELL_T + " &7┃ &f$" + NF.format(bal)));
        border(inv, 54);
        inv.setItem(4, balanceHead(player, bal));

        List<ShopItem> list = sellables();
        for (int i = 0; i < list.size() && i < CONTENT.length; i++) {
            ShopItem si = list.get(i);
            int have = countItem(player, si.material);
            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8&m                    "));
            lore.add(Color.translate(" &fPrecio &8» &d$" + NF.format(si.sell) + " &7c/u"));
            lore.add(Color.translate(" &fTenés &8» &f" + have));
            lore.add(Color.translate("&8&m                    "));
            lore.add(Color.translate(" &aClick izq &7» vender &f1"));
            lore.add(Color.translate(" &aClick der &7» vender &f" + si.amount));
            lore.add(Color.translate(" &aShift der &7» vender &ftodo"));
            inv.setItem(CONTENT[i], new ItemBuilder(si.material)
                .setName(Color.translate("&c&l" + niceName(si.material))).setLore(lore).build());
        }

        inv.setItem(49, me.vcb.astrocore.utils.Heads.skull(me.vcb.astrocore.utils.Heads.ARROW_LEFT,
            "&c« Volver", List.of(Color.translate("&7Volver al menú principal"))));
        player.openInventory(inv);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private int bal(Player p) { return AstroCore.getInstance().getEconomyManager().getBalance(p); }

    private void border(Inventory inv, int size) {
        ItemStack glass = new ItemBuilder(Material.MAGENTA_STAINED_GLASS_PANE).setName("§r").addFakeGlow().build();
        int rows = size / 9;
        for (int i = 0; i < 9; i++) { inv.setItem(i, glass); inv.setItem((rows - 1) * 9 + i, glass); }
        for (int r = 1; r < rows - 1; r++) { inv.setItem(r * 9, glass); inv.setItem(r * 9 + 8, glass); }
    }

    private ItemStack balanceHead(Player player, int bal) {
        return new ItemBuilder(Material.PLAYER_HEAD).setSkullOwner(player.getName())
            .setName(Color.translate("&d&l" + player.getName()))
            .setLore(List.of(
                Color.translate("&8&m                    "),
                Color.translate(" &fBalance &8» &d$" + NF.format(bal)))).build();
    }

    private int countItem(Player player, Material mat) {
        int have = 0;
        for (ItemStack it : player.getInventory().getContents())
            if (it != null && it.getType() == mat) have += it.getAmount();
        return have;
    }

    private String niceName(Material m) {
        String s = m.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Clicks ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        int slot = event.getRawSlot();

        if (title.startsWith(Color.translate(HUB_T))) {
            event.setCancelled(true);
            if (slot == 40) { openSell(player); return; }
            int n = this.categories.size();
            int[] row = {20, 21, 22, 23, 24};
            int start = Math.max(0, (row.length - n) / 2);
            for (int i = 0; i < n && start + i < row.length; i++)
                if (row[start + i] == slot) { openBuy(player, i); return; }
            return;
        }

        if (title.startsWith(Color.translate(BUY_T))) {
            event.setCancelled(true);
            if (slot == 49) { openMain(player); return; }
            if (slot == 48) { openSell(player); return; }
            int catIndex = this.activeCat.getOrDefault(player.getUniqueId(), 0);
            Category cat = this.categories.get(catIndex);
            int idx = contentIndex(slot);
            if (idx < 0 || idx >= cat.items.size()) return;
            ShopItem si = cat.items.get(idx);
            if (si.buy < 0) return;
            int qty = si.amount * (event.isShiftClick() ? 4 : 1);
            buy(player, si, qty);
            openBuy(player, catIndex);
            return;
        }

        if (title.startsWith(Color.translate(SELL_T))) {
            event.setCancelled(true);
            if (slot == 49) { openMain(player); return; }
            List<ShopItem> list = sellables();
            int idx = contentIndex(slot);
            if (idx < 0 || idx >= list.size()) return;
            ShopItem si = list.get(idx);
            if (event.isShiftClick() && event.isRightClick()) sell(player, si, countItem(player, si.material));
            else if (event.isRightClick()) sell(player, si, si.amount);
            else sell(player, si, 1);
            openSell(player);
        }
    }

    private int contentIndex(int slot) {
        for (int i = 0; i < CONTENT.length; i++) if (CONTENT[i] == slot) return i;
        return -1;
    }

    // ── Buy / sell ────────────────────────────────────────────────────────────

    private void buy(Player player, ShopItem si, int qty) {
        if (qty <= 0) return;
        // buy price is for 'amount' items → unit price * qty.
        long total = (long) Math.ceil((double) si.buy / Math.max(1, si.amount) * qty);
        var econ = AstroCore.getInstance().getEconomyManager();
        if (econ.getBalance(player) < total) {
            player.sendMessage(Color.translate("&cNo te alcanza. Cuesta &d$" + NF.format(total) + "&c."));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        econ.removeBalance(player, (int) total);
        for (ItemStack left : player.getInventory().addItem(new ItemStack(si.material, qty)).values())
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        player.sendMessage(Color.translate("&aCompraste &f" + qty + "x " + niceName(si.material) + " &7por &d$" + NF.format(total)));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    private void sell(Player player, ShopItem si, int qty) {
        if (qty <= 0) return;
        int have = countItem(player, si.material);
        if (have <= 0) {
            player.sendMessage(Color.translate("&cNo tenés " + niceName(si.material) + " para vender."));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        int toSell = Math.min(qty, have);
        removeItems(player, si.material, toSell);
        long total = (long) si.sell * toSell;
        AstroCore.getInstance().getEconomyManager().addBalance(player, (int) total);
        player.sendMessage(Color.translate("&aVendiste &f" + toSell + "x " + niceName(si.material) + " &7por &d$" + NF.format(total)));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.9f);
    }

    private void removeItems(Player player, Material mat, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType() != mat) continue;
            int take = Math.min(amount, it.getAmount());
            it.setAmount(it.getAmount() - take);
            amount -= take;
            if (it.getAmount() <= 0) player.getInventory().setItem(i, null);
        }
    }
}

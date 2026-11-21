package me.vcb.astrocore.perks;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /perks — a money shop for premium perks (falltrap tokens, extra lives, etc.).
 * Everything is centred in the inventory for a clean look.
 */
public class PerksMenu implements Listener {

    private static final String TITLE = "§5§lᴘᴇʀᴋs §8» §7Tienda";
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

    private static PerksMenu instance;

    /** A buyable perk. */
    public enum Perk {
        FALLTRAP_TOKEN("&c&lToken de Falltrap", Material.TNT, 75_000,
            new String[]{ "&7Permite usar &f/f falltrap &7una vez.", "&7Se consume al construir." }),
        BASE_TOKEN("&e&lToken de Base", Material.OAK_FENCE_GATE, 50_000,
            new String[]{ "&7Permite usar &f/f base &7una vez.", "&7Se consume al construir." }),
        SKYBASE_TOKEN("&b&lToken de SkyBase", Material.GLASS, 100_000,
            new String[]{ "&7Permite usar &f/f skybase &7una vez.", "&7Se consume al construir." }),
        EXTRA_LIFE("&a&lVida Extra", Material.TOTEM_OF_UNDYING, 40_000,
            new String[]{ "&7Suma &f+1 vida &7a tu cuenta." }),
        SPAWN_CREDITS("&b&lCréditos de Spawn", Material.SUNFLOWER, 25_000,
            new String[]{ "&7Suma &f+16 créditos de spawn&7." });

        public final String name;
        public final Material icon;
        public final int price;
        public final String[] desc;

        Perk(String name, Material icon, int price, String[] desc) {
            this.name = name; this.icon = icon; this.price = price; this.desc = desc;
        }
    }

    // Perks disabled for the current map (persisted).
    private static final java.util.Set<String> DISABLED = new java.util.HashSet<>();
    private java.io.File disabledFile;

    public PerksMenu() {
        instance = this;
        this.disabledFile = new java.io.File(AstroCore.getInstance().getDataFolder(), "perks-disabled.yml");
        loadDisabled();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static PerksMenu getInstance() { return instance; }

    // ── Enable / disable ──────────────────────────────────────────────────────

    public static boolean isDisabled(Perk perk) {
        return perk != null && DISABLED.contains(perk.name());
    }

    public void setDisabled(Perk perk, boolean disabled) {
        if (disabled) DISABLED.add(perk.name());
        else DISABLED.remove(perk.name());
        saveDisabled();
    }

    /** Resolves an admin perk alias to the enum. */
    public static Perk resolvePerk(String s) {
        return switch (s.toLowerCase()) {
            case "falltrap", "falltrap_token" -> Perk.FALLTRAP_TOKEN;
            case "base", "base_token" -> Perk.BASE_TOKEN;
            case "skybase", "skybase_token" -> Perk.SKYBASE_TOKEN;
            case "life", "lives", "vida" -> Perk.EXTRA_LIFE;
            case "credits", "spawncredits" -> Perk.SPAWN_CREDITS;
            default -> null;
        };
    }

    private void loadDisabled() {
        if (!this.disabledFile.exists()) return;
        org.bukkit.configuration.file.YamlConfiguration y =
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(this.disabledFile);
        DISABLED.addAll(y.getStringList("disabled"));
    }

    private void saveDisabled() {
        org.bukkit.configuration.file.YamlConfiguration y = new org.bukkit.configuration.file.YamlConfiguration();
        y.set("disabled", new java.util.ArrayList<>(DISABLED));
        try { y.save(this.disabledFile); } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    // ── Open ──────────────────────────────────────────────────────────────

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, Color.translate(TITLE));

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 45; i++) inv.setItem(i, filler);

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        int balance = ud != null ? ud.getBalance() : 0;

        // Centre the perks across the middle row (slots 18-26).
        Perk[] perks = Perk.values();
        int[] slots = centeredSlots(perks.length, 22); // row 3 centre = 22
        for (int i = 0; i < perks.length; i++) {
            Perk perk = perks[i];
            boolean canAfford = balance >= perk.price;

            boolean disabled = isDisabled(perk);

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8&m                    "));
            for (String d : perk.desc) lore.add(Color.translate(d));
            lore.add(Color.translate("&8&m                    "));
            lore.add(Color.translate("&7Precio: &6$" + NF.format(perk.price)));
            lore.add("");
            lore.add(disabled
                ? Color.translate("&cDeshabilitado durante este mapa")
                : (canAfford
                    ? Color.translate("&aClick para comprar")
                    : Color.translate("&cNo tenés suficiente dinero")));

            inv.setItem(slots[i], new ItemBuilder(perk.icon)
                .setName(Color.translate(perk.name))
                .setLore(lore).build());
        }

        // Centred info item at the bottom.
        inv.setItem(40, new ItemBuilder(Material.PLAYER_HEAD)
            .setName(Color.translate("&e&l" + player.getName()))
            .setLore(List.of(
                Color.translate("&8&m                    "),
                Color.translate("&7Balance: &6$" + NF.format(balance)),
                Color.translate("&7Tokens de Falltrap: &f" + (ud != null ? ud.getFalltrapTokens() : 0)),
                Color.translate("&7Tokens de Base: &f" + (ud != null ? ud.getBaseTokens() : 0)),
                Color.translate("&7Tokens de SkyBase: &f" + (ud != null ? ud.getSkyBaseTokens() : 0)),
                Color.translate("&8&m                    ")
            )).build());

        player.openInventory(inv);
    }

    private int[] centeredSlots(int count, int centerSlot) {
        // Centre `count` items horizontally around the middle of the centre row.
        int row = centerSlot / 9;
        int start = row * 9 + Math.max(0, (9 - count) / 2);
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) slots[i] = start + i;
        return slots;
    }

    // ── Buy ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(Color.translate(TITLE))) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = clicked.getItemMeta().getDisplayName();
        Perk perk = null;
        for (Perk p : Perk.values()) {
            if (Color.translate(p.name).equals(name)) { perk = p; break; }
        }
        if (perk == null) return;

        if (isDisabled(perk)) {
            player.sendMessage(Color.translate("&cEse perk está deshabilitado durante este mapa."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        if (ud.getBalance() < perk.price) {
            player.sendMessage(Color.translate("&cNo tenés suficiente dinero. Necesitás &6$"
                + NF.format(perk.price) + "&c."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        ud.changeBalance(ud.getBalance() - perk.price);

        switch (perk) {
            case FALLTRAP_TOKEN -> ud.setFalltrapTokens(ud.getFalltrapTokens() + 1);
            case BASE_TOKEN     -> ud.setBaseTokens(ud.getBaseTokens() + 1);
            case SKYBASE_TOKEN  -> ud.setSkyBaseTokens(ud.getSkyBaseTokens() + 1);
            case EXTRA_LIFE     -> ud.setLives(ud.getLives() + 1);
            case SPAWN_CREDITS  -> ud.setSpawnCredits(ud.getSpawnCredits() + 16);
        }

        player.sendMessage(Color.translate("&aCompraste &r" + perk.name + " &apor &6$"
            + NF.format(perk.price) + "&a."));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);

        open(player); // refresh
    }
}

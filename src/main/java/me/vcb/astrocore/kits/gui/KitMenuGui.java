package me.vcb.astrocore.kits.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.kits.KitsManager;
import me.vcb.astrocore.kits.kit.KitCategory;
import me.vcb.astrocore.kits.kit.KitData;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class KitMenuGui implements Listener {

    // ── Titles ────────────────────────────────────────────────────────────────
    private static final String MAIN_TITLE = "§8sᴇʀᴠᴇʀ §fᴋɪᴛs";
    private static final String SUB_PREFIX = "§8ᴋɪᴛs §7» §f";

    // ── Background ────────────────────────────────────────────────────────────
    private static final Material BG = Material.BLACK_STAINED_GLASS_PANE;

    // ── Custom head textures (base64) from HeadDB ─────────────────────────────
    // Chest — used for GRATUITO category panel
    private static final String HEAD_CHEST =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvNmY2OGQ1MDliNWQxNjY5Yjk3MWRkMWQ0ZGYyZTQ3ZTE5YmNiMWIzM2Jm" +
        "MWE3ZmYxZGRhMjliZmM2ZjllYmYifX19";

    // Beacon — used for GKIT category panel (premium/god-kit feel)
    private static final String HEAD_BEACON =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvOWRiZGFhNzU1MDk5ZWRkN2VmYTFmMTI4ODJjN2E1MWI1ODE1ZGI1MmUw" +
        "YjE2NGFlZjZkZjlhMWY1M2VjYTIzIn19fQ==";

    // Arrow Left — used for the back button
    private static final String HEAD_ARROW_LEFT =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5" +
        "OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==";

    // Red X — used when a category has no kits
    private static final String HEAD_RED_X =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvMTJkN2E3NTFlYjA3MWUwOGRiYmM5NWJjNWQ5ZDY2ZTVmNTFkYzY3MTI2" +
        "NDBhZDJkZmEwM2RlZmJiNjhhN2YzYSJ9fX0=";

    /*
     *  MAIN MENU — 27 slots (3 rows)
     *  [ B  B  B  B  B  B  B  B  B ]
     *  [ B  B  G  B  B  B  K  B  B ]  GRATUITO@11  GKIT@15
     *  [ B  B  B  B  B  B  B  B  B ]
     *
     *  SUB-MENU — 54 slots (6 rows), same pattern as reference DeluxeMenus
     *  [ B  B  B  B  B  B  B  B  B ]
     *  [ B  K1 B  K2 B  K3 B  K4 B ]  slots 10,12,14,16
     *  [ B  B  B  B  B  B  B  B  B ]
     *  [ B  K5 B  K6 B  K7 B  K8 B ]  slots 28,30,32,34
     *  [ B  B  B  B  B  B  B  B  B ]
     *  [ B  B  B  B  ←  B  B  B  B ]  back @ 49
     */
    private static final int[] KIT_SLOTS = { 10, 12, 14, 16, 28, 30, 32, 34 };
    private static final int   BACK_SLOT  = 49;
    private static final int   SUB_SIZE   = 54;

    public KitMenuGui() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    // ── Main menu ─────────────────────────────────────────────────────────────

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_TITLE);
        KitsManager km = AstroCore.getInstance().getKitsManager();

        fillBg(inv, 27);

        inv.setItem(11, makeCategoryItem(
            KitCategory.GRATUITO,
            km.getKitsByCategory(KitCategory.GRATUITO).size(),
            HEAD_CHEST));

        inv.setItem(15, makeCategoryItem(
            KitCategory.GKIT,
            km.getKitsByCategory(KitCategory.GKIT).size(),
            HEAD_BEACON));

        player.openInventory(inv);
    }

    // ── Sub-menu ──────────────────────────────────────────────────────────────

    public void openCategory(Player player, KitCategory category) {
        KitsManager km = AstroCore.getInstance().getKitsManager();
        List<KitData> kits = km.getKitsByCategory(category);

        Inventory inv = Bukkit.createInventory(null, SUB_SIZE,
            SUB_PREFIX + stripColor(category.getDisplayName()));

        fillBg(inv, SUB_SIZE);
        inv.setItem(BACK_SLOT, makeBackButton());

        if(kits.isEmpty()) {
            inv.setItem(22, makeSkull(HEAD_RED_X, "§cNo hay kits en esta categoría.",
                Arrays.asList("", "§7Esta categoría está vacía.")));
            player.openInventory(inv);
            return;
        }

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        int limit = Math.min(kits.size(), KIT_SLOTS.length);
        for(int i = 0; i < limit; i++) {
            inv.setItem(KIT_SLOTS[i], makeKitItem(kits.get(i), player, ud));
        }

        player.openInventory(inv);
    }

    // ── Click handler ─────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // ---- Main menu ----
        if(title.equals(MAIN_TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if(slot == 11) openCategory(player, KitCategory.GRATUITO);
            else if(slot == 15) openCategory(player, KitCategory.GKIT);
            return;
        }

        // ---- Sub-menu ----
        if(title.startsWith(SUB_PREFIX)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            // Back button — detect by slot, not by material
            if(slot == BACK_SLOT) {
                openMain(player);
                return;
            }

            ItemStack item = event.getCurrentItem();
            if(item == null || item.getType() == Material.AIR) return;
            if(item.getType() == BG) return;
            if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

            // Resolve kit by display name (strip color codes)
            String rawName = net.md_5.bungee.api.ChatColor
                .stripColor(item.getItemMeta().getDisplayName()).trim();
            KitData kit = AstroCore.getInstance().getKitsManager().getKit(rawName);
            if(kit == null) return;

            player.closeInventory();
            AstroCore.getInstance().getKitsManager().giveKit(player, kit);
        }
    }

    // ── Item builders ─────────────────────────────────────────────────────────

    /** Category selector shown in the main 3-row menu — uses a custom skull head. */
    private ItemStack makeCategoryItem(KitCategory category, int count, String headTexture) {
        List<String> lore = new ArrayList<>();
        lore.add("§8" + stripColor(category.getDescription()));
        lore.add("");
        lore.add("§7§l ︳ §7ᴋɪᴛs ᴅɪsᴘᴏɴɪʙʟᴇs");
        lore.add("§7§l︳ §f" + count + " kits");
        lore.add("");
        lore.add("§a➡ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴀʙʀɪʀ");

        ItemStack skull = makeSkull(headTexture, category.getDisplayName(), lore);
        ItemMeta meta = skull.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        skull.setItemMeta(meta);
        return skull;
    }

    /** Individual kit item shown inside a category sub-menu. */
    private ItemStack makeKitItem(KitData kit, Player player, Userdata ud) {
        boolean hasPerm = player.isOp()
            || player.hasPermission(kit.getPermission())
            || player.hasPermission("astrocore.kit.*");
        boolean onCooldown = ud != null
            && ud.getKitDelays().containsKey(kit.getName())
            && (ud.getKitDelays().get(kit.getName()) == -1
                || System.currentTimeMillis() < ud.getKitDelays().get(kit.getName()));

        Material icon = AstroCore.getInstance().getKitsManager().getKitIcon(kit);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        String color = !hasPerm ? "§c" : onCooldown ? "§e" : "§f";
        meta.setDisplayName(color + kit.getName());

        List<String> lore = new ArrayList<>();
        lore.add("§8Kits");
        lore.add("");

        // Cooldown section
        lore.add("§7§l ︳ §7ᴄᴏᴏʟᴅᴏᴡɴ");
        if(kit.getDelay() == -1) {
            lore.add("§7§l︳ §cᴜɴ sᴏʟᴏ ᴜsᴏ");
        } else if(kit.getDelay() == 0) {
            lore.add("§7§l︳ §aɴɪɴɢᴜɴᴏ");
        } else {
            lore.add("§7§l︳ §f" + StringUtils.formatDurationWords(kit.getDelay() * 1000L));
        }

        lore.add("");

        // Status section
        lore.add("§7§l ︳ §7ᴇsᴛᴀᴅᴏ");
        if(!hasPerm) {
            lore.add("§7§l︳ §c✗ sɪɴ ᴘᴇʀᴍɪsᴏ");
        } else if(onCooldown) {
            long remaining = ud.getKitDelays().get(kit.getName());
            String time = remaining == -1
                ? "ᴘᴇʀᴍᴀɴᴇɴᴛᴇ"
                : StringUtils.formatDurationWords(remaining - System.currentTimeMillis());
            lore.add("§7§l︳ §e⌛ §f" + time);
        } else {
            lore.add("§7§l︳ §a✔ ᴅɪsᴘᴏɴɪʙʟᴇ");
        }

        lore.add("");

        if(hasPerm && !onCooldown) {
            lore.add("§a➡ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴜsᴀʀ ᴋɪᴛ!");
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    /** Back button using the Arrow Left custom skull. */
    private ItemStack makeBackButton() {
        return makeSkull(HEAD_ARROW_LEFT,
            "§c§lᴠᴏʟᴠᴇʀ",
            Arrays.asList("", "§7➡ ᴄʟɪᴄᴋ ᴘᴀʀᴀ ᴠᴏʟᴠᴇʀ ᴀʟ ᴍᴇɴᴜ!"));
    }

    // ── Custom skull builder ──────────────────────────────────────────────────

    /**
     * Creates a PLAYER_HEAD with a custom base64 texture from HeadDB.
     * Uses the Paper API: Bukkit.createProfile + ProfileProperty("textures", b64).
     */
    private ItemStack makeSkull(String base64, String displayName, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        // Apply texture via Paper's PlayerProfile API
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(profile);

        meta.setDisplayName(displayName);
        if(lore != null) meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void fillBg(Inventory inv, int size) {
        ItemStack pane = new ItemStack(BG);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("§r");
        pane.setItemMeta(meta);
        for(int i = 0; i < size; i++) inv.setItem(i, pane);
    }

    private String stripColor(String s) {
        return net.md_5.bungee.api.ChatColor.stripColor(s);
    }
}

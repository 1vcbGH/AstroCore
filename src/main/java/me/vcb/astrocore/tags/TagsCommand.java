package me.vcb.astrocore.tags;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagsCommand extends BaseCommand implements Listener {

    @lombok.Getter private static TagsCommand instance;

    private static final int TAGS_PER_PAGE = 28;
    private static final int[] TAG_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    // Bottom row special slots
    private static final int SLOT_BACK    = 45;
    private static final int SLOT_PREV    = 47;
    private static final int SLOT_BALANCE = 49;
    private static final int SLOT_NEXT    = 51;

    private static String pageTitle(int page) {
        return "§d§lChat Tags §8— §ePágina " + page;
    }

    public static boolean isTagsTitle(String title) {
        return title != null && title.startsWith("§d§lChat Tags");
    }

    public TagsCommand() {
        super("tags", (String) null, true);
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        openGui((Player) sender, 1);
    }

    public void openGui(Player player) {
        openGui(player, 1);
    }

    public void openGui(Player player, int page) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        ChatTag[] tags = ChatTag.values();
        int totalPages = (int) Math.ceil((double) tags.length / TAGS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));

        Inventory inv = Bukkit.createInventory(null, 54, pageTitle(page));

        // Border
        ItemStack border = makeBorder();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, border);
        }

        // Fill bottom row with border
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        // Tags for this page
        int startIndex = (page - 1) * TAGS_PER_PAGE;
        for (int i = 0; i < TAG_SLOTS.length; i++) {
            int tagIndex = startIndex + i;
            if (tagIndex >= tags.length) break;
            inv.setItem(TAG_SLOTS[i], buildTagItem(tags[tagIndex], ud));
        }

        // Back button
        inv.setItem(SLOT_BACK, makeBackBtn());

        // Balance
        inv.setItem(SLOT_BALANCE, makeBalanceItem(ud.getBalance()));

        // Prev page
        if (page > 1) inv.setItem(SLOT_PREV, makePageBtn(false, page - 1));

        // Next page
        if (page < totalPages) inv.setItem(SLOT_NEXT, makePageBtn(true, page + 1));

        player.openInventory(inv);
    }

    private ItemStack buildTagItem(ChatTag tag, Userdata ud) {
        boolean owned  = ud.ownsTag(tag);
        boolean active = !ud.isTagHidden() && ud.getActiveTagId().equals(tag.getId());

        ItemStack item = new ItemStack(tag.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tag.getDisplay() + (active ? " §a§l✔" : ""));

        List<String> lore = new ArrayList<>();
        lore.add("§8" + tag.getDescription().replace("§7", "").trim());
        lore.add("");
        lore.add(tag.isFree() ? "§7Precio: §aGratis" : "§7Precio: §e" + tag.getPrice() + " coins");
        lore.add("");
        if (active) {
            lore.add("§a§lEQUIPADO §7— Click para desequipar");
        } else if (owned) {
            lore.add("§aTenés este tag §7— §eClick para equipar");
        } else {
            lore.add("§cNo tenés este tag");
            lore.add("§7Click para comprar");
        }
        meta.setLore(lore);

        if (active) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!isTagsTitle(title)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
            || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE
            || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        int slot = event.getRawSlot();
        int currentPage = parsePageFromTitle(title);

        // Back button
        if (slot == SLOT_BACK) {
            player.closeInventory();
            // Reopen coinshop main if available
            me.vcb.astrocore.coinshop.CoinShopCommand cs = getCoinShop();
            if (cs != null) cs.openMainFor(player);
            return;
        }

        // Prev page
        if (slot == SLOT_PREV) {
            openGui(player, currentPage - 1);
            return;
        }

        // Next page
        if (slot == SLOT_NEXT) {
            openGui(player, currentPage + 1);
            return;
        }

        // Balance — no action
        if (slot == SLOT_BALANCE) return;

        // Tag slot
        int tagSlotIndex = -1;
        for (int i = 0; i < TAG_SLOTS.length; i++) {
            if (TAG_SLOTS[i] == slot) { tagSlotIndex = i; break; }
        }
        if (tagSlotIndex < 0) return;

        int tagIndex = (currentPage - 1) * TAGS_PER_PAGE + tagSlotIndex;
        ChatTag[] tags = ChatTag.values();
        if (tagIndex >= tags.length) return;

        ChatTag tag = tags[tagIndex];
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        if (!ud.isTagHidden() && ud.getActiveTagId().equals(tag.getId())) {
            ud.equipTag(null);
            player.sendMessage("§7Tag desequipado.");
            openGui(player, currentPage);
            return;
        }

        if (ud.ownsTag(tag)) {
            ud.equipTag(tag);
            player.sendMessage("§aTag equipado: " + tag.getDisplay());
            openGui(player, currentPage);
            return;
        }

        // Buy
        if (ud.getBalance() < tag.getPrice()) {
            player.sendMessage("§cNo tenés coins suficientes. Necesitás §e" + tag.getPrice()
                + " coins§c, tenés §e" + ud.getBalance() + "§c.");
            return;
        }

        AstroCore.getInstance().getEconomyManager().removeBalance(player, tag.getPrice());
        ud.giveTag(tag);
        ud.equipTag(tag);
        player.sendMessage("§a¡Compraste y equipaste el tag " + tag.getDisplay() + "§a! §e-" + tag.getPrice() + " coins.");
        openGui(player, currentPage);
    }

    private int parsePageFromTitle(String title) {
        try {
            return Integer.parseInt(title.substring(title.lastIndexOf(' ') + 1));
        } catch (Exception e) {
            return 1;
        }
    }

    private me.vcb.astrocore.coinshop.CoinShopCommand getCoinShop() {
        return me.vcb.astrocore.coinshop.CoinShopCommand.getInstance();
    }

    // ── Item builders ─────────────────────────────────────────────────────

    private ItemStack makeBackBtn() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7◀ Volver");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePageBtn(boolean next, int targetPage) {
        ItemStack item = new ItemStack(next ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(next ? "§aSiguiente página §8(Pág. " + targetPage + ")" : "§cPágina anterior §8(Pág. " + targetPage + ")");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBalanceItem(int balance) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Tu balance: §e" + balance + " coins");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBorder() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        item.setItemMeta(meta);
        return item;
    }
}

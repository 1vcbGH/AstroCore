package me.vcb.astrocore.coinshop;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.killeffects.KillEffect;
import me.vcb.astrocore.tags.TagsCommand;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoinShopCommand extends BaseCommand implements Listener {

    private static final String TITLE_MAIN      = "§6§lCoin Shop";
    private static final String TITLE_DEATH     = "§6§lCoin Shop §8— §eDeath Messages";
    private static final String TITLE_EFFECT    = "§6§lCoin Shop §8— §eKill Effects";
    private static final String TITLE_CHATCOLOR = "§6§lCoin Shop §8— §bChat Colors";
    private static final String TITLE_TRAIL     = "§6§lCoin Shop §8— §aTrails";
    private static final String TITLE_KSMSG     = "§6§lCoin Shop §8— §6Killstreak Messages";

    private static final int[] SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};

    @lombok.Getter private static CoinShopCommand instance;

    public CoinShopCommand() {
        super("coinshop", Arrays.asList(), null, true);
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void openMainFor(Player player) {
        openMain(player);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        openMain((Player) sender);
    }

    // ── Main menu ─────────────────────────────────────────────────────────

    private void openMain(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);
        ItemStack border = border();
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // Row 2 — centered at cols 2,4,6 of a 9-wide row (slots 11,13,15)
        inv.setItem(11, makeNavItem(Material.SKELETON_SKULL, "§e§lDeath Messages",
            "§7Personaliza tu mensaje de muerte.", "§8Clic para ver"));
        inv.setItem(13, makeNavItem(Material.FIREWORK_ROCKET, "§c§lKill Effects",
            "§7Efectos visuales al matar a un jugador.", "§8Clic para ver"));
        inv.setItem(15, makeNavItem(Material.NAME_TAG, "§d§lChat Tags",
            "§7Tags que se muestran en el chat.", "§8Clic para ver"));

        // Row 4 — centered at slots 29,31,33
        inv.setItem(29, makeNavItem(Material.NAME_TAG, "§b§lChat Colors",
            "§7Cambia el color de tu chat.", "§8Clic para ver"));
        inv.setItem(31, makeNavItem(Material.BLAZE_ROD, "§a§lTrails",
            "§7Deja un rastro de partículas al caminar. §7(Solo en Spawn)", "§8Clic para ver"));
        inv.setItem(33, makeNavItem(Material.BONE, "§6§lKillstreak Messages",
            "§7Personaliza el mensaje de tu racha.", "§8Clic para ver"));

        // Balance — slot 49
        inv.setItem(49, makeBalanceItem(ud.getBalance()));

        player.openInventory(inv);
    }

    // ── Death Messages menu ───────────────────────────────────────────────

    private void openDeathMessages(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_DEATH);
        fillBorder(inv);

        DeathMessageType[] types = DeathMessageType.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            DeathMessageType dm = types[i];
            boolean owned = dm.isFree() || ud.getPurchasedDeathMessages().contains(dm.name());
            boolean selected = dm.name().equals(ud.getSelectedDeathMessage());
            inv.setItem(SLOTS[i], makeDeathMsgItem(dm, owned, selected, ud.getBalance()));
        }

        inv.setItem(45, makeBackBtn());
        inv.setItem(49, makeBalanceItem(ud.getBalance()));
        player.openInventory(inv);
    }

    // ── Kill Effects menu ─────────────────────────────────────────────────

    private void openKillEffects(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_EFFECT);
        fillBorder(inv);

        KillEffect[] effects = KillEffect.values();
        String current = ud.getSelectedKillEffect() != null ? ud.getSelectedKillEffect() : KillEffect.NINGUNO.getId();

        for (int i = 0; i < effects.length && i < SLOTS.length; i++) {
            KillEffect ef = effects[i];
            boolean owned = ef.isFree() || hasEffect(ud, ef);
            boolean selected = ef.getId().equalsIgnoreCase(current);
            inv.setItem(SLOTS[i], makeEffectItem(ef, owned, selected, ud.getBalance()));
        }

        inv.setItem(45, makeBackBtn());
        inv.setItem(49, makeBalanceItem(ud.getBalance()));
        player.openInventory(inv);
    }

    private boolean hasEffect(Userdata ud, KillEffect ef) {
        if (ud.getOwnedKillEffects() == null) return false;
        return ud.getOwnedKillEffects().contains(ef.getId());
    }

    // ── Chat Colors menu ──────────────────────────────────────────────────

    private void openChatColors(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_CHATCOLOR);
        fillBorder(inv);

        ChatColorType[] colors = ChatColorType.values();
        String current = ud.getSelectedChatColor() != null ? ud.getSelectedChatColor() : "default";

        for (int i = 0; i < colors.length && i < SLOTS.length; i++) {
            ChatColorType ct = colors[i];
            boolean owned = ct.isFree() || ud.getOwnedChatColors().contains(ct.getId());
            boolean selected = ct.getId().equalsIgnoreCase(current);
            inv.setItem(SLOTS[i], makeColorItem(ct, owned, selected, ud.getBalance()));
        }

        inv.setItem(45, makeBackBtn());
        inv.setItem(49, makeBalanceItem(ud.getBalance()));
        player.openInventory(inv);
    }

    // ── Trails menu ───────────────────────────────────────────────────────

    private void openTrails(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_TRAIL);
        fillBorder(inv);

        TrailType[] trails = TrailType.values();
        String current = ud.getSelectedTrail() != null ? ud.getSelectedTrail() : "ninguno";

        for (int i = 0; i < trails.length && i < SLOTS.length; i++) {
            TrailType tt = trails[i];
            boolean owned = tt.isFree() || ud.getOwnedTrails().contains(tt.getId());
            boolean selected = tt.getId().equalsIgnoreCase(current);
            inv.setItem(SLOTS[i], makeTrailItem(tt, owned, selected, ud.getBalance()));
        }

        inv.setItem(45, makeBackBtn());
        inv.setItem(49, makeBalanceItem(ud.getBalance()));
        player.openInventory(inv);
    }

    // ── Killstreak Messages menu ──────────────────────────────────────────

    private void openKsMessages(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_KSMSG);
        fillBorder(inv);

        KillStreakMessageType[] msgs = KillStreakMessageType.values();
        String current = ud.getSelectedKillstreakMessage() != null ? ud.getSelectedKillstreakMessage() : "default";

        for (int i = 0; i < msgs.length && i < SLOTS.length; i++) {
            KillStreakMessageType km = msgs[i];
            boolean owned = km.isFree() || ud.getOwnedKillstreakMessages().contains(km.getId());
            boolean selected = km.getId().equalsIgnoreCase(current);
            inv.setItem(SLOTS[i], makeKsMsgItem(km, owned, selected, ud.getBalance()));
        }

        inv.setItem(45, makeBackBtn());
        inv.setItem(49, makeBalanceItem(ud.getBalance()));
        player.openInventory(inv);
    }

    // ── Click handler ─────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(TITLE_MAIN) && !title.equals(TITLE_DEATH) && !title.equals(TITLE_EFFECT)
            && !title.equals(TITLE_CHATCOLOR) && !title.equals(TITLE_TRAIL) && !title.equals(TITLE_KSMSG)
            && !me.vcb.astrocore.tags.TagsCommand.isTagsTitle(title)) return;
        // Tags GUI is handled by TagsCommand listener — skip here
        if (me.vcb.astrocore.tags.TagsCommand.isTagsTitle(title)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
            || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE
            || clicked.getType() == Material.GOLD_NUGGET) return;

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // Main menu navigation
        if (title.equals(TITLE_MAIN)) {
            if (name.contains("Death Messages")) openDeathMessages(player);
            else if (name.contains("Kill Effects")) openKillEffects(player);
            else if (name.contains("Chat Tags")) {
                player.closeInventory();
                if (TagsCommand.getInstance() != null) TagsCommand.getInstance().openGui(player);
            }
            else if (name.contains("Chat Colors")) openChatColors(player);
            else if (name.contains("Trails")) openTrails(player);
            else if (name.contains("Killstreak Messages")) openKsMessages(player);
            return;
        }

        // Back button
        if (name.equals("◀ Volver")) { openMain(player); return; }

        // Death messages
        if (title.equals(TITLE_DEATH)) { handleDeathMsgClick(player, name); return; }

        // Kill effects
        if (title.equals(TITLE_EFFECT)) { handleEffectClick(player, name); return; }

        // Chat colors
        if (title.equals(TITLE_CHATCOLOR)) { handleColorClick(player, name); return; }

        // Trails
        if (title.equals(TITLE_TRAIL)) { handleTrailClick(player, name); return; }

        // Killstreak messages
        if (title.equals(TITLE_KSMSG)) { handleKsMsgClick(player, name); }
    }

    // ── Click handlers ────────────────────────────────────────────────────

    private void handleDeathMsgClick(Player player, String rawName) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        DeathMessageType found = null;
        for (DeathMessageType dm : DeathMessageType.values()) {
            if (ChatColor.stripColor(dm.getDisplayName()).equals(rawName)) { found = dm; break; }
        }
        if (found == null) return;

        boolean owned = found.isFree() || ud.getPurchasedDeathMessages().contains(found.name());
        if (!owned) {
            if (ud.getBalance() < found.getPrice()) {
                player.sendMessage("§cNo tienes suficientes coins. Necesitas §e" + found.getPrice() + "§c.");
                return;
            }
            ud.changeBalance(ud.getBalance() - found.getPrice());
            ud.getPurchasedDeathMessages().add(found.name());
            player.sendMessage("§a¡Compraste §e" + found.getDisplayName() + "§a!");
        }
        ud.setSelectedDeathMessage(found.name());
        player.sendMessage("§aDeath message §e" + found.getDisplayName() + " §aequipada.");
        player.closeInventory();
        openDeathMessages(player);
    }

    private void handleEffectClick(Player player, String rawName) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        KillEffect found = null;
        for (KillEffect ef : KillEffect.values()) {
            if (ChatColor.stripColor(ef.getDisplayName()).equals(rawName)) { found = ef; break; }
        }
        if (found == null) return;

        boolean owned = found.isFree() || hasEffect(ud, found);
        if (!owned) {
            if (ud.getBalance() < found.getPrice()) {
                player.sendMessage("§cNo tienes suficientes coins. Necesitas §e" + found.getPrice() + "§c.");
                return;
            }
            ud.changeBalance(ud.getBalance() - found.getPrice());
            ud.getOrCreateOwnedKillEffects().add(found.getId());
            player.sendMessage("§a¡Compraste §e" + found.getDisplayName() + "§a!");
        }
        ud.setSelectedKillEffect(found == KillEffect.NINGUNO ? null : found.getId());
        player.sendMessage("§aKill effect §e" + found.getDisplayName() + " §aequipado.");
        player.closeInventory();
        openKillEffects(player);
    }

    private void handleColorClick(Player player, String rawName) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        ChatColorType found = null;
        for (ChatColorType ct : ChatColorType.values()) {
            if (ChatColor.stripColor(ct.getDisplayName()).equals(rawName)) { found = ct; break; }
        }
        if (found == null) return;

        boolean owned = found.isFree() || ud.getOwnedChatColors().contains(found.getId());
        if (!owned) {
            if (ud.getBalance() < found.getPrice()) {
                player.sendMessage("§cNo tienes suficientes coins. Necesitas §e" + found.getPrice() + "§c.");
                return;
            }
            ud.changeBalance(ud.getBalance() - found.getPrice());
            ud.getOwnedChatColors().add(found.getId());
            player.sendMessage("§a¡Compraste §e" + found.getDisplayName() + "§a!");
        }
        ud.setSelectedChatColor(found.getId());
        player.sendMessage("§aColor de chat §e" + found.getDisplayName() + " §aequipado.");
        player.closeInventory();
        openChatColors(player);
    }

    private void handleTrailClick(Player player, String rawName) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        TrailType found = null;
        for (TrailType tt : TrailType.values()) {
            if (ChatColor.stripColor(tt.getDisplayName()).equals(rawName)) { found = tt; break; }
        }
        if (found == null) return;

        boolean owned = found.isFree() || ud.getOwnedTrails().contains(found.getId());
        if (!owned) {
            if (ud.getBalance() < found.getPrice()) {
                player.sendMessage("§cNo tienes suficientes coins. Necesitas §e" + found.getPrice() + "§c.");
                return;
            }
            ud.changeBalance(ud.getBalance() - found.getPrice());
            ud.getOwnedTrails().add(found.getId());
            player.sendMessage("§a¡Compraste §e" + found.getDisplayName() + "§a!");
        }
        ud.setSelectedTrail(found == TrailType.NINGUNO ? null : found.getId());
        player.sendMessage("§aTrail §e" + found.getDisplayName() + " §aequipado.");
        player.closeInventory();
        openTrails(player);
    }

    private void handleKsMsgClick(Player player, String rawName) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;

        KillStreakMessageType found = null;
        for (KillStreakMessageType km : KillStreakMessageType.values()) {
            if (ChatColor.stripColor(km.getDisplayName()).equals(rawName)) { found = km; break; }
        }
        if (found == null) return;

        boolean owned = found.isFree() || ud.getOwnedKillstreakMessages().contains(found.getId());
        if (!owned) {
            if (ud.getBalance() < found.getPrice()) {
                player.sendMessage("§cNo tienes suficientes coins. Necesitas §e" + found.getPrice() + "§c.");
                return;
            }
            ud.changeBalance(ud.getBalance() - found.getPrice());
            ud.getOwnedKillstreakMessages().add(found.getId());
            player.sendMessage("§a¡Compraste §e" + found.getDisplayName() + "§a!");
        }
        ud.setSelectedKillstreakMessage(found.getId());
        player.sendMessage("§aMensaje de racha §e" + found.getDisplayName() + " §aequipado.");
        player.closeInventory();
        openKsMessages(player);
    }

    // ── Item builders ─────────────────────────────────────────────────────

    private ItemStack makeDeathMsgItem(DeathMessageType dm, boolean owned, boolean selected, int balance) {
        ItemStack item = new ItemStack(dm.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((selected ? "§a" : owned ? "§e" : "§7") + dm.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§8§m──────────────");
        lore.add("§7" + dm.getDescription());
        lore.add("");
        lore.add("§7Ejemplo: §f" + dm.build("Killer", "Victim", "Espada"));
        lore.add("");
        if (!dm.isFree()) lore.add("§7Precio: §6" + dm.getPrice() + " coins");
        lore.add(selected ? "§a§l[EQUIPADO]" : owned ? "§e[Equipar]" : "§c[Comprar]");
        if (!owned && balance < dm.getPrice()) lore.add("§c(Coins insuficientes)");
        lore.add("§8§m──────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeEffectItem(KillEffect ef, boolean owned, boolean selected, int balance) {
        ItemStack item = new ItemStack(ef.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((selected ? "§a" : owned ? "§e" : "§7") + ef.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§8§m──────────────");
        lore.add("§7" + ef.getDescription());
        lore.add("");
        if (!ef.isFree()) lore.add("§7Precio: §6" + ef.getPrice() + " coins");
        lore.add(selected ? "§a§l[EQUIPADO]" : owned ? "§e[Equipar]" : "§c[Comprar]");
        if (!owned && balance < ef.getPrice()) lore.add("§c(Coins insuficientes)");
        lore.add("§8§m──────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeColorItem(ChatColorType ct, boolean owned, boolean selected, int balance) {
        ItemStack item = new ItemStack(ct.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((selected ? "§a" : owned ? "§e" : "§7") + ct.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§8§m──────────────");
        lore.add("§7" + ct.getDescription());
        lore.add("");
        lore.add("§7Vista previa: " + ct.getColorCode() + "Hola mundo!");
        lore.add("");
        if (!ct.isFree()) lore.add("§7Precio: §6" + ct.getPrice() + " coins");
        lore.add(selected ? "§a§l[EQUIPADO]" : owned ? "§e[Equipar]" : "§c[Comprar]");
        if (!owned && balance < ct.getPrice()) lore.add("§c(Coins insuficientes)");
        lore.add("§8§m──────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeTrailItem(TrailType tt, boolean owned, boolean selected, int balance) {
        ItemStack item = new ItemStack(tt.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((selected ? "§a" : owned ? "§e" : "§7") + tt.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§8§m──────────────");
        lore.add("§7" + tt.getDescription());
        lore.add("");
        if (!tt.isFree()) lore.add("§7Precio: §6" + tt.getPrice() + " coins");
        lore.add(selected ? "§a§l[EQUIPADO]" : owned ? "§e[Equipar]" : "§c[Comprar]");
        if (!owned && balance < tt.getPrice()) lore.add("§c(Coins insuficientes)");
        lore.add("§8§m──────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeKsMsgItem(KillStreakMessageType km, boolean owned, boolean selected, int balance) {
        ItemStack item = new ItemStack(km.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((selected ? "§a" : owned ? "§e" : "§7") + km.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§8§m──────────────");
        lore.add("§7" + km.getDescription());
        lore.add("");
        lore.add("§7Vista previa:");
        lore.add("§f" + km.build("Player", 10, 300));
        lore.add("");
        if (!km.isFree()) lore.add("§7Precio: §6" + km.getPrice() + " coins");
        lore.add(selected ? "§a§l[EQUIPADO]" : owned ? "§e[Equipar]" : "§c[Comprar]");
        if (!owned && balance < km.getPrice()) lore.add("§c(Coins insuficientes)");
        lore.add("§8§m──────────────");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeNavItem(Material mat, String name, String... lorelines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        for (String l : lorelines) lore.add(l);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBackBtn() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7◀ Volver");
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

    private ItemStack border() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv) {
        ItemStack b = border();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, b);
        }
    }
}

package me.vcb.astrocore.battlepass;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Season battlepass defined in battlepass.yml. XP is earned from kills and
 * playtime; each level has a reward (money + commands). Progress is per-player.
 */
public class BattlePassManager implements Listener {

    private static final String TITLE = "§b§lBattlepass";

    private static BattlePassManager instance;
    private final File file;

    private int xpPerLevel = 1000;
    private int maxLevel = 20;
    private int xpPerKill = 100;
    private int xpPerMinute = 20;
    private final java.util.Map<Integer, int[]> rewardMoney = new java.util.HashMap<>(); // level -> money
    private final java.util.Map<Integer, List<String>> rewardCmds = new java.util.HashMap<>();

    public BattlePassManager() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "battlepass.yml");
        load();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        Tasks.syncTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) addXp(p, this.xpPerMinute);
        }, 1200L, 1200L);
    }

    public static BattlePassManager getInstance() { return instance; }

    // ── Config ──────────────────────────────────────────────────────────────

    private void load() {
        if (!this.file.exists()) writeDefaults();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(this.file);
        this.xpPerLevel = y.getInt("xp-per-level", 1000);
        this.maxLevel = y.getInt("max-level", 20);
        this.xpPerKill = y.getInt("xp-per-kill", 100);
        this.xpPerMinute = y.getInt("xp-per-minute", 20);
        this.rewardMoney.clear(); this.rewardCmds.clear();
        ConfigurationSection sec = y.getConfigurationSection("rewards");
        if (sec != null) for (String key : sec.getKeys(false)) {
            try {
                int lvl = Integer.parseInt(key);
                this.rewardMoney.put(lvl, new int[]{ sec.getInt(key + ".money", 0) });
                this.rewardCmds.put(lvl, sec.getStringList(key + ".commands"));
            } catch (NumberFormatException ignored) {}
        }
    }

    public void reload() { load(); }

    private void writeDefaults() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("xp-per-level", 1000);
        y.set("max-level", 20);
        y.set("xp-per-kill", 100);
        y.set("xp-per-minute", 20);
        for (int lvl = 1; lvl <= 20; lvl++) {
            y.set("rewards." + lvl + ".money", lvl * 1000);
            y.set("rewards." + lvl + ".commands", new ArrayList<String>());
        }
        try { y.save(this.file); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── XP / levels ───────────────────────────────────────────────────────────

    public void addXp(Player player, int amount) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        int before = levelOf(ud.getBattlepassXp());
        ud.setBattlepassXp(ud.getBattlepassXp() + amount);
        int after = levelOf(ud.getBattlepassXp());
        if (after > before)
            player.sendMessage(Color.translate("&b&lBattlepass &7» &a¡Subiste a nivel &f" + after + "&a! Reclamá en &f/battlepass"));
    }

    public int levelOf(int xp) {
        return Math.min(this.maxLevel, xp / this.xpPerLevel);
    }

    private int moneyFor(int level) {
        int[] v = this.rewardMoney.get(level);
        return v != null ? v[0] : level * 1000;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && killer != event.getEntity()) addXp(killer, this.xpPerKill);
    }

    // ── GUI (paginated reward track) ──────────────────────────────────────────

    private static final int PER_PAGE = 7;          // levels per page
    private static final int[] REWARD_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] NUMBER_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] TRACK_SLOTS  = {28, 29, 30, 31, 32, 33, 34};
    private static final int PREV = 48, NEXT = 50, INFO = 49;
    private final java.util.Map<java.util.UUID, Integer> page = new java.util.HashMap<>();

    public void open(Player player) { open(player, this.page.getOrDefault(player.getUniqueId(), 0)); }

    public void open(Player player, int pg) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        if (ud.getBattlepassClaimed() == null) ud.setBattlepassClaimed(new HashSet<>());

        int totalPages = (this.maxLevel + PER_PAGE - 1) / PER_PAGE;
        if (pg < 0) pg = 0;
        if (pg >= totalPages) pg = totalPages - 1;
        this.page.put(player.getUniqueId(), pg);

        int xp = ud.getBattlepassXp();
        int level = levelOf(xp);

        Inventory inv = Bukkit.createInventory(null, 54, Color.translate(TITLE));
        ItemStack bg = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        ItemStack edge = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);
        for (int i = 0; i < 9; i++) inv.setItem(i, edge);
        for (int i = 45; i < 54; i++) inv.setItem(i, edge);

        for (int i = 0; i < PER_PAGE; i++) {
            int lvl = pg * PER_PAGE + i + 1;
            if (lvl > this.maxLevel) {
                inv.setItem(NUMBER_SLOTS[i], bg);
                inv.setItem(REWARD_SLOTS[i], bg);
                inv.setItem(TRACK_SLOTS[i], bg);
                continue;
            }
            boolean reached = level >= lvl;
            boolean claimed = ud.getBattlepassClaimed().contains(lvl);
            boolean claimable = reached && !claimed;

            // Number tag (top).
            inv.setItem(NUMBER_SLOTS[i], new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setAmount(Math.max(1, Math.min(64, lvl)))
                .setName(Color.translate((reached ? "&b&l" : "&7&l") + "Nivel " + lvl)).build());

            // Reward (middle, clickable).
            int money = moneyFor(lvl);
            boolean hasCmd = this.rewardCmds.get(lvl) != null && !this.rewardCmds.get(lvl).isEmpty();
            Material rewardIcon = lvl % 10 == 0 ? Material.NETHER_STAR
                : lvl % 5 == 0 ? Material.DIAMOND
                : hasCmd ? Material.CHEST : Material.GOLD_INGOT;

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8&m                "));
            if (money > 0) lore.add(Color.translate("  &6▪ $" + String.format("%,d", money)));
            if (hasCmd) lore.add(Color.translate("  &d▪ Recompensa especial"));
            lore.add(Color.translate("&8&m                "));
            lore.add(claimed ? Color.translate("&a✔ Reclamado")
                : claimable ? Color.translate("&e&l➜ CLICK PARA RECLAMAR")
                : Color.translate("&c🔒 Requiere nivel " + lvl));
            ItemBuilder rb = new ItemBuilder(rewardIcon)
                .setName(Color.translate((claimable ? "&e&l" : claimed ? "&a&l" : "&7&l") + "Recompensa Nivel " + lvl))
                .setLore(lore);
            if (claimable) rb.addFakeGlow();
            inv.setItem(REWARD_SLOTS[i], rb.build());

            // Track pane (bottom) — visual progress.
            Material track = claimed ? Material.LIME_STAINED_GLASS_PANE
                : reached ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            inv.setItem(TRACK_SLOTS[i], new ItemBuilder(track)
                .setName(Color.translate(claimed ? "&aCompletado" : reached ? "&eDisponible" : "&cBloqueado")).build());
        }

        // Player info head with XP bar.
        int into = xp % this.xpPerLevel;
        inv.setItem(INFO, me.vcb.astrocore.utils.Heads.skull(me.vcb.astrocore.utils.Heads.BEACON,
            "&b&l" + player.getName(), List.of(
                Color.translate("&8&m                "),
                Color.translate("&7Nivel actual &8» &b&l" + level + " &7/ " + this.maxLevel),
                Color.translate("&7XP total &8» &f" + String.format("%,d", xp)),
                "",
                Color.translate("&7Hacia nivel " + Math.min(this.maxLevel, level + 1) + ":"),
                "  " + me.vcb.astrocore.utils.Heads.progressBar(into, this.xpPerLevel, 14, "&b", "&8")
                    + Color.translate(" &7" + into + "/" + this.xpPerLevel))));

        // Navigation.
        if (pg > 0) inv.setItem(PREV, me.vcb.astrocore.utils.Heads.skull(
            me.vcb.astrocore.utils.Heads.ARROW_LEFT, "&e« Página anterior", null));
        if (pg < totalPages - 1) inv.setItem(NEXT, new ItemBuilder(Material.ARROW)
            .setName(Color.translate("&eSiguiente página »")).build());
        inv.setItem(53, new ItemBuilder(Material.PAPER)
            .setName(Color.translate("&7Página &f" + (pg + 1) + "&7/&f" + totalPages)).build());

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(Color.translate(TITLE))) return;
        event.setCancelled(true);

        int raw = event.getRawSlot();
        int pg = this.page.getOrDefault(player.getUniqueId(), 0);

        if (raw == PREV) { open(player, pg - 1); return; }
        if (raw == NEXT) { open(player, pg + 1); return; }

        int idx = -1;
        for (int i = 0; i < REWARD_SLOTS.length; i++) if (REWARD_SLOTS[i] == raw) idx = i;
        if (idx < 0) return;
        int lvl = pg * PER_PAGE + idx + 1;
        if (lvl > this.maxLevel) return;

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        if (ud.getBattlepassClaimed() == null) ud.setBattlepassClaimed(new HashSet<>());

        if (ud.getBattlepassClaimed().contains(lvl)) return;
        if (levelOf(ud.getBattlepassXp()) < lvl) {
            player.sendMessage(Color.translate("&cTodavía no llegaste a ese nivel."));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        ud.getBattlepassClaimed().add(lvl);
        int money = moneyFor(lvl);
        if (money > 0) ud.changeBalance(ud.getBalance() + money);
        List<String> cmds = this.rewardCmds.get(lvl);
        if (cmds != null) for (String c : cmds)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", player.getName()));

        player.sendMessage(Color.translate("&b&lBattlepass &7» &aReclamaste el nivel &f" + lvl + " &7(&6$" + String.format("%,d", money) + "&7)"));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);
        open(player, pg);
    }
}

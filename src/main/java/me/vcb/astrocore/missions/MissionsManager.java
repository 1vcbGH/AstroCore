package me.vcb.astrocore.missions;

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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Daily rotating missions, defined in missions.yml. The set of active missions is
 * the same for everyone each day; progress + claims are per-player and reset
 * daily. Reward via money + console commands (flexible).
 */
public class MissionsManager implements Listener {

    public enum Type { KILLS, MINE, DEPOSIT, KOTH, PLAYTIME }

    public static class Mission {
        String id, displayName; Type type; int target; Material icon, mineMaterial;
        int rewardMoney; List<String> rewardCommands;
    }

    private static MissionsManager instance;
    private final File file;
    private final List<Mission> pool = new ArrayList<>();
    private int dailyCount = 4;

    private long cachedDay = -1;
    private List<Mission> cachedDaily = new ArrayList<>();

    public MissionsManager() {
        instance = this;
        this.file = new File(AstroCore.getInstance().getDataFolder(), "missions.yml");
        load();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        // Playtime tick every minute.
        Tasks.syncTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) addProgress(p, Type.PLAYTIME, 1, null);
        }, 1200L, 1200L);
    }

    public static MissionsManager getInstance() { return instance; }

    // ── Config ──────────────────────────────────────────────────────────────

    private void load() {
        if (!this.file.exists()) writeDefaults();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
        this.dailyCount = yaml.getInt("daily-count", 4);
        this.pool.clear();
        ConfigurationSection sec = yaml.getConfigurationSection("missions");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection m = sec.getConfigurationSection(key);
            if (m == null) continue;
            Mission mi = new Mission();
            mi.id = key;
            try { mi.type = Type.valueOf(m.getString("type", "KILLS").toUpperCase()); }
            catch (Exception e) { mi.type = Type.KILLS; }
            mi.target = m.getInt("target", 1);
            mi.displayName = m.getString("name", key);
            mi.icon = matOr(m.getString("icon"), Material.PAPER);
            mi.mineMaterial = matOr(m.getString("mine-material"), null);
            mi.rewardMoney = m.getInt("reward-money", 0);
            mi.rewardCommands = m.getStringList("reward-commands");
            this.pool.add(mi);
        }
    }

    public void reload() { load(); this.cachedDay = -1; }

    private void writeDefaults() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("daily-count", 4);
        def(y, "kills10", "KILLS", 10, "&cMata 10 jugadores", "DIAMOND_SWORD", null, 5000);
        def(y, "kills25", "KILLS", 25, "&cMata 25 jugadores", "NETHERITE_SWORD", null, 12000);
        def(y, "koth1", "KOTH", 1, "&5Captura 1 KOTH", "BEACON", null, 15000);
        def(y, "deposit50k", "DEPOSIT", 50000, "&6Depositá $50,000 en tu facción", "GOLD_INGOT", null, 8000);
        def(y, "mineDiamond", "MINE", 32, "&bMiná 32 diamantes", "DIAMOND", "DIAMOND_ORE", 6000);
        def(y, "play30", "PLAYTIME", 30, "&aJugá 30 minutos", "CLOCK", null, 4000);
        try { y.save(this.file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void def(YamlConfiguration y, String id, String type, int target, String name,
                     String icon, String mine, int money) {
        String b = "missions." + id + ".";
        y.set(b + "type", type); y.set(b + "target", target); y.set(b + "name", name);
        y.set(b + "icon", icon); if (mine != null) y.set(b + "mine-material", mine);
        y.set(b + "reward-money", money);
        y.set(b + "reward-commands", new ArrayList<String>());
    }

    private Material matOr(String s, Material def) {
        if (s == null) return def;
        Material m = Material.matchMaterial(s);
        return m == null ? def : m;
    }

    // ── Daily set ─────────────────────────────────────────────────────────────

    private long today() { return System.currentTimeMillis() / 86_400_000L; }

    public List<Mission> getDailyMissions() {
        long day = today();
        if (day != this.cachedDay) {
            this.cachedDay = day;
            List<Mission> copy = new ArrayList<>(this.pool);
            Collections.shuffle(copy, new Random(day));
            this.cachedDaily = new ArrayList<>(copy.subList(0, Math.min(this.dailyCount, copy.size())));
        }
        return this.cachedDaily;
    }

    private void ensureDay(Userdata ud) {
        long day = today();
        if (ud.getMissionsDay() != day) {
            ud.setMissionsDay(day);
            ud.setMissionProgress(new HashMap<>());
            ud.setMissionsClaimed(new HashSet<>());
        }
        if (ud.getMissionProgress() == null) ud.setMissionProgress(new HashMap<>());
        if (ud.getMissionsClaimed() == null) ud.setMissionsClaimed(new HashSet<>());
    }

    // ── Progress ────────────────────────────────────────────────────────────

    public void addProgress(Player player, Type type, int amount, Material mineMat) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        ensureDay(ud);
        for (Mission m : getDailyMissions()) {
            if (m.type != type) continue;
            if (type == Type.MINE && m.mineMaterial != null && m.mineMaterial != mineMat) continue;
            if (ud.getMissionsClaimed().contains(m.id)) continue;
            int cur = ud.getMissionProgress().getOrDefault(m.id, 0);
            if (cur >= m.target) continue;
            ud.getMissionProgress().put(m.id, Math.min(m.target, cur + amount));
        }
    }

    // ── Events ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && killer != event.getEntity()) addProgress(killer, Type.KILLS, 1, null);
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        addProgress(event.getPlayer(), Type.MINE, 1, event.getBlock().getType());
    }

    @EventHandler
    public void onKothCap(me.vcb.astrocore.games.koth.event.KothCappedEvent event) {
        if (event.getCapper() != null) addProgress(event.getCapper(), Type.KOTH, 1, null);
    }

    // Called from DepositCommand / KOTH cap.
    public void onDeposit(Player player, int amount) { addProgress(player, Type.DEPOSIT, amount, null); }
    public void onKothCapped(Player player) { addProgress(player, Type.KOTH, 1, null); }

    // ── GUI ───────────────────────────────────────────────────────────────────

    private static final String TITLE = "§a§lMisiones Diarias";

    public void open(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        ensureDay(ud);

        List<Mission> daily = getDailyMissions();
        Inventory inv = Bukkit.createInventory(null, 27, Color.translate(TITLE));
        var bg = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§r").build();
        var accent = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName("§r").build();
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);
        for (int i : new int[]{0, 8, 18, 26}) inv.setItem(i, accent);

        inv.setItem(4, me.vcb.astrocore.utils.Heads.skull(me.vcb.astrocore.utils.Heads.CHEST,
            "&a&lMisiones Diarias", List.of(
                Color.translate("&7Completá objetivos y reclamá premios."),
                Color.translate("&7Rotan cada &a24 horas&7."))));

        int completed = 0;
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < daily.size() && i < slots.length; i++) {
            Mission m = daily.get(i);
            int prog = ud.getMissionProgress().getOrDefault(m.id, 0);
            boolean done = prog >= m.target;
            boolean claimed = ud.getMissionsClaimed().contains(m.id);
            if (claimed) completed++;

            List<String> lore = new ArrayList<>();
            lore.add(Color.translate("&8&m              "));
            lore.add(Color.translate("&7Progreso &8» &f" + Math.min(prog, m.target) + "&7/&f" + m.target));
            lore.add("  " + me.vcb.astrocore.utils.Heads.progressBar(prog, m.target, 10, "&a", "&8"));
            lore.add(Color.translate("&7Recompensa &8» &6$" + m.rewardMoney));
            lore.add(Color.translate("&8&m              "));
            lore.add(claimed ? Color.translate("&a✔ Reclamada")
                : done ? Color.translate("&e➜ Click para reclamar")
                : Color.translate("&7⌛ En progreso..."));

            ItemBuilder b = new ItemBuilder(claimed ? Material.LIME_DYE : m.icon)
                .setName(Color.translate(m.displayName))
                .setLore(lore);
            if (done && !claimed) b.addFakeGlow();
            inv.setItem(slots[i], b.build());
        }

        inv.setItem(22, new ItemBuilder(Material.CLOCK)
            .setName(Color.translate("&e&lProgreso del día"))
            .setLore(List.of(
                Color.translate("&7Reclamadas &8» &a" + completed + "&7/&a" + daily.size()),
                Color.translate("&7Rotan en menos de 24h."))).build());

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(Color.translate(TITLE))) return;
        event.setCancelled(true);

        int raw = event.getRawSlot();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int idx = -1;
        for (int i = 0; i < slots.length; i++) if (slots[i] == raw) idx = i;
        if (idx < 0) return;

        List<Mission> daily = getDailyMissions();
        if (idx >= daily.size()) return;
        Mission m = daily.get(idx);

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null) return;
        ensureDay(ud);

        if (ud.getMissionsClaimed().contains(m.id)) return;
        int prog = ud.getMissionProgress().getOrDefault(m.id, 0);
        if (prog < m.target) {
            player.sendMessage(Color.translate("&cTodavía no completaste esa misión."));
            return;
        }

        ud.getMissionsClaimed().add(m.id);
        if (m.rewardMoney > 0) ud.changeBalance(ud.getBalance() + m.rewardMoney);
        for (String cmd : m.rewardCommands)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));

        player.sendMessage(Color.translate("&a&lMisiones &7» &aReclamaste &r" + Color.translate(m.displayName)
            + " &7(&6$" + m.rewardMoney + "&7)"));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);
        open(player);
    }
}

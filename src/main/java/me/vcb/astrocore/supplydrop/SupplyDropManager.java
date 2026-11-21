package me.vcb.astrocore.supplydrop;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.WarzoneFaction;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SupplyDropManager implements Listener {

    private static final int INTERVAL_TICKS = 36000; // 30 min
    private static final int MAX_SURFACE_ATTEMPTS = 100;
    private static final int MIN_SPAWN_AMOUNT = 1;
    private static final int MAX_SPAWN_AMOUNT = 3;

    private final File configFile;
    private YamlConfiguration config;

    /** chestLocation key → SupplyDrop */
    private final Map<String, SupplyDrop> activeDrops = new HashMap<>();

    private BukkitTask autoTask;
    private SupplyDropLootGui lootGui;

    public SupplyDropManager() {
        this.configFile = new File(AstroCore.getInstance().getDataFolder(), "supplys.yml");
        this.loadOrCreateConfig();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        this.lootGui = new SupplyDropLootGui();
        this.startAutoTask();
    }

    // ── Config ────────────────────────────────────────────────────────────

    private void loadOrCreateConfig() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            YamlConfiguration def = new YamlConfiguration();
            def.set("items-per-drop", 5);
            List<String> defaultLoot = new ArrayList<>();
            defaultLoot.add("DIAMOND_SWORD:1:20");
            defaultLoot.add("BOW:1:40");
            defaultLoot.add("ARROW:64:50");
            defaultLoot.add("GOLDEN_APPLE:3:30");
            defaultLoot.add("DIAMOND_CHESTPLATE:1:15");
            defaultLoot.add("DIAMOND_HELMET:1:15");
            defaultLoot.add("DIAMOND_LEGGINGS:1:15");
            defaultLoot.add("DIAMOND_BOOTS:1:15");
            defaultLoot.add("COOKED_BEEF:10:60");
            defaultLoot.add("OBSIDIAN:8:35");
            def.set("loot", defaultLoot);
            try { def.save(configFile); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try { this.config.save(configFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getLootEntries() {
        return new ArrayList<>(config.getStringList("loot"));
    }

    public int getItemsPerDrop() {
        return config.getInt("items-per-drop", 5);
    }

    public void addLootEntry(String entry) {
        List<String> list = getLootEntries();
        list.add(entry);
        config.set("loot", list);
        saveConfig();
    }

    public boolean removeLootEntry(int index) {
        List<String> list = getLootEntries();
        if (index < 0 || index >= list.size()) return false;
        list.remove(index);
        config.set("loot", list);
        saveConfig();
        return true;
    }

    public void setLootEntries(List<String> entries) {
        config.set("loot", entries);
        saveConfig();
    }

    public void setItemsPerDrop(int amount) {
        config.set("items-per-drop", amount);
        saveConfig();
    }

    // ── Loot generation ───────────────────────────────────────────────────

    public List<ItemStack> generateLoot() {
        List<String> entries = getLootEntries();
        int count = getItemsPerDrop();
        List<ItemStack> result = new ArrayList<>();
        Random rng = ThreadLocalRandom.current();

        List<ItemStack> pool = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length < 2) continue;
            try {
                Material mat = Material.matchMaterial(parts[0]);
                if (mat == null) continue;
                int amount = Integer.parseInt(parts[1]);
                int weight = parts.length >= 3 ? Integer.parseInt(parts[2]) : 10;
                pool.add(new ItemStack(mat, amount));
                weights.add(weight);
                totalWeight += weight;
            } catch (Exception ignored) {}
        }

        if (pool.isEmpty()) return result;

        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            int roll = rng.nextInt(totalWeight);
            int cumulative = 0;
            for (int j = 0; j < pool.size(); j++) {
                cumulative += weights.get(j);
                if (roll < cumulative) {
                    result.add(pool.get(j).clone());
                    totalWeight -= weights.get(j);
                    pool.remove(j);
                    weights.remove(j);
                    break;
                }
            }
        }
        return result;
    }

    // ── Spawn ─────────────────────────────────────────────────────────────

    public int spawnDrops(int amount) {
        World world = getOverworld();
        if (world == null) return 0;
        int spawned = 0;
        List<Location> spawnedLocs = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Location loc = findSurfaceInWarzone(world);
            if (loc == null) continue;
            List<ItemStack> loot = generateLoot();
            final Location finalLoc = loc;
            SupplyDrop drop = new SupplyDrop(loc, loot, () -> handleExpiry(finalLoc));
            activeDrops.put(locKey(loc), drop);
            spawnedLocs.add(loc);
            spawned++;
        }

        if (spawned > 0) broadcastSpawn(spawnedLocs);
        return spawned;
    }

    private void broadcastSpawn(List<Location> locs) {
        // Build coord list string
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < locs.size(); i++) {
            Location loc = locs.get(i);
            coords.append("&cX:").append(loc.getBlockX()).append(" Z:").append(loc.getBlockZ());
            if (i < locs.size() - 1) coords.append(" &8| ");
        }

        List<String> lines = Arrays.asList(
            "&8&m                                                                      ",
            "&8█&7████████&8█",
            "&7██&6█████&7████",
            "&7██&6█&7███&6█&7███   &6&l✈ SUPPLY DROP &6&l✈",
            "&7██&6█&7████&6█&7██   &7Han caído &e" + locs.size() + " &7supply drop(s) en la warzone",
            "&7██&6█&7████&6█&7██   &7Coords: &c" + coords,
            "&7██&6█&7███&6█&7███",
            "&7██&6█████&7████",
            "&8█&7████████&8█",
            "&8&m                                                                      "
        );

        Messages.sendMessage(lines.stream()
            .map(Color::translate)
            .collect(java.util.stream.Collectors.toList()));
    }

    /** Find a random surface location inside the warzone. */
    private Location findSurfaceInWarzone(World world) {
        int radius = me.vcb.astrocore.config.Config.WARZONE_RADIUS.getOrDefault(Environment.NORMAL, 2000);
        int usableRadius = Math.max(10, radius - 20);
        Random rng = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < MAX_SURFACE_ATTEMPTS; attempt++) {
            int x = rng.nextInt(usableRadius * 2 + 1) - usableRadius;
            int z = rng.nextInt(usableRadius * 2 + 1) - usableRadius;

            Location checkLoc = new Location(world, x, 64, z);
            if (!(ClaimManager.getInstance().getFactionAt(checkLoc) instanceof WarzoneFaction)) continue;

            Block surface = world.getHighestBlockAt(x, z);
            if (surface.getY() < 10 || surface.getY() > 250) continue;

            Material mat = surface.getType();
            if (mat == Material.WATER || mat == Material.LAVA || mat == Material.AIR) continue;
            if (!mat.isSolid()) continue;

            Location chestLoc = surface.getLocation().add(0, 1, 0);
            if (chestLoc.getBlock().getType() != Material.AIR) continue;

            return chestLoc;
        }
        return null;
    }

    private World getOverworld() {
        Location spawn = me.vcb.astrocore.config.Config.WORLD_SPAWNS.get(Environment.NORMAL);
        if (spawn != null && spawn.getWorld() != null) return spawn.getWorld();
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == Environment.NORMAL)
            .findFirst().orElse(null);
    }

    // ── Expiry ────────────────────────────────────────────────────────────

    private void handleExpiry(Location loc) {
        SupplyDrop drop = activeDrops.remove(locKey(loc));
        if (drop == null) return;
        drop.destroy();

        List<String> lines = Arrays.asList(
            "&8&m                                                                      ",
            "&8█&7████████&8█",
            "&7██&6█████&7████",
            "&7██&6█&7███&6█&7███   &6&l✈ SUPPLY DROP EXPIRADO",
            "&7██&6█&7████&6█&7██   &7Nadie reclamó el supply drop",
            "&7██&6█&7████&6█&7██   &7Coords: &cX:" + loc.getBlockX() + " Z:" + loc.getBlockZ(),
            "&7██&6█&7███&6█&7███   &7Drops restantes: &e" + activeDrops.size(),
            "&7██&6█████&7████",
            "&8█&7████████&8█",
            "&8&m                                                                      "
        );
        Messages.sendMessage(lines.stream()
            .map(Color::translate)
            .collect(java.util.stream.Collectors.toList()));
    }

    // ── Auto task ─────────────────────────────────────────────────────────

    private void startAutoTask() {
        this.autoTask = Tasks.syncTimer(() -> {
            int amount = ThreadLocalRandom.current().nextInt(MAX_SPAWN_AMOUNT - MIN_SPAWN_AMOUNT + 1) + MIN_SPAWN_AMOUNT;
            spawnDrops(amount);
        }, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    public void cancelAutoTask() {
        if (autoTask != null) autoTask.cancel();
        activeDrops.values().forEach(drop -> {
            drop.removeHolograms();
            Block b = drop.getChestLocation().getBlock();
            if (b.getType() == Material.CHEST) b.setType(Material.AIR);
        });
        activeDrops.clear();
    }

    // ── Events ────────────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Tasks.syncLater(() -> activeDrops.values().forEach(drop ->
            drop.addPlayerToBossBar(player)), 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Chest chestHolder)) return;

        Location loc = chestHolder.getLocation();
        SupplyDrop drop = activeDrops.get(locKey(loc));
        if (drop == null) return;

        event.setCancelled(true);
        if (drop.isOpened()) return;
        drop.markOpened();

        // Drop loot on the ground
        for (ItemStack item : drop.getLoot()) {
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 1, 0.5), item);
        }

        activeDrops.remove(locKey(loc));
        drop.destroy();

        // Announce opened — D shape
        List<String> lines = Arrays.asList(
            "&8&m                                                                      ",
            "&8█&7████████&8█",
            "&7██&6█████&7████",
            "&7██&6█&7███&6█&7███   &6&l✈ SUPPLY DROP ABIERTO",
            "&7██&6█&7████&6█&7██   &a" + player.getName() + " &7reclamó un supply drop",
            "&7██&6█&7████&6█&7██   &7Coordenadas: &cX:" + loc.getBlockX() + " Z:" + loc.getBlockZ(),
            "&7██&6█&7███&6█&7███   &7Drops restantes: &e" + activeDrops.size(),
            "&7██&6█████&7████",
            "&8█&7████████&8█",
            "&8&m                                                                      "
        );

        Messages.sendMessage(lines.stream()
            .map(Color::translate)
            .collect(java.util.stream.Collectors.toList()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (activeDrops.containsKey(locKey(event.getBlock().getLocation()))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.translate("&cNo puedes romper un supply drop. ¡Ábrelo!"));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    public String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public int getActiveDropCount() { return activeDrops.size(); }

    /** True if the given block location is an active supply drop chest. */
    public boolean isSupplyDrop(Location loc) {
        return loc != null && activeDrops.containsKey(locKey(loc));
    }

    /** Remove the drop nearest to the given location, or any drop if loc is null. Returns true if removed. */
    public boolean stopNearest(Location from, String adminName) {
        if (activeDrops.isEmpty()) return false;
        String key;
        if (from == null) {
            key = activeDrops.keySet().iterator().next();
        } else {
            key = activeDrops.entrySet().stream()
                .min(java.util.Comparator.comparingDouble(e ->
                    e.getValue().getChestLocation().distanceSquared(from)))
                .map(java.util.Map.Entry::getKey).orElse(null);
        }
        if (key == null) return false;
        SupplyDrop drop = activeDrops.remove(key);
        if (drop == null) return false;
        Location loc = drop.getChestLocation();
        drop.destroy();

        List<String> lines = Arrays.asList(
            "&8&m                                                                      ",
            "&8█&7████████&8█",
            "&7██&6█████&7████",
            "&7██&6█&7███&6█&7███   &6&l✈ SUPPLY DROP CANCELADO",
            "&7██&6█&7████&6█&7██   &cUn admin eliminó el supply drop",
            "&7██&6█&7████&6█&7██   &7Admin: &e" + adminName + " &8| &7Coords: &cX:" + loc.getBlockX() + " Z:" + loc.getBlockZ(),
            "&7██&6█&7███&6█&7███   &7Drops restantes: &e" + activeDrops.size(),
            "&7██&6█████&7████",
            "&8█&7████████&8█",
            "&8&m                                                                      "
        );
        Messages.sendMessage(lines.stream().map(Color::translate).collect(java.util.stream.Collectors.toList()));
        return true;
    }

    public int stopAll(String adminName) {
        int count = activeDrops.size();
        activeDrops.values().forEach(SupplyDrop::destroy);
        activeDrops.clear();

        if (count > 0) {
            List<String> lines = Arrays.asList(
                "&8&m                                                                      ",
                "&8█&7████████&8█",
                "&7██&6█████&7████",
                "&7██&6█&7███&6█&7███   &6&l✈ SUPPLY DROPS CANCELADOS",
                "&7██&6█&7████&6█&7██   &cUn admin eliminó todos los supply drops",
                "&7██&6█&7████&6█&7██   &7Admin: &e" + adminName + " &8| &7Eliminados: &c" + count,
                "&7██&6█&7███&6█&7███",
                "&7██&6█████&7████",
                "&8█&7████████&8█",
                "&8&m                                                                      "
            );
            Messages.sendMessage(lines.stream().map(Color::translate).collect(java.util.stream.Collectors.toList()));
        }
        return count;
    }

    public java.util.Collection<SupplyDrop> getActiveDrops() { return activeDrops.values(); }

    public void openLootGui(Player player) { lootGui.openFor(player); }
}

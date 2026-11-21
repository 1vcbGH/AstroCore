package me.vcb.astrocore.elevator;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.hologram.HologramEntry;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Quartz-block elevators. A special (lore + PDC tagged) quartz block, given via
 * /elevators give. Stand on it and jump to go up, sneak to go down — teleporting
 * to the next elevator block in the column. Each block shows a hologram.
 */
public class ElevatorManager implements Listener {

    private static final String HOLO_TAG = "kc_elevator_holo";
    private static final long COOLDOWN_MS = 500;

    private static ElevatorManager instance;

    private final NamespacedKey itemKey;
    private final File file;
    private final Set<String> elevators = new HashSet<>();   // "worldName;x;y;z"
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ElevatorManager() {
        instance = this;
        this.itemKey = new NamespacedKey(AstroCore.getInstance(), "elevator_block");
        this.file = new File(AstroCore.getInstance().getDataFolder(), "elevators.yml");
        load();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static ElevatorManager getInstance() { return instance; }

    // ── Item ──────────────────────────────────────────────────────────────

    public ItemStack createItem(int amount) {
        ItemStack item = new ItemBuilder(Material.QUARTZ_BLOCK, amount)
            .setName(Color.translate("&b&lElevador"))
            .setLore(List.of(
                Color.translate("&7Saltá para &asubir"),
                Color.translate("&7Agáchate para &cbajar"),
                Color.translate("&8Bloque de elevador")
            )).build();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(this.itemKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** Registers an elevator block at a location (used by builders like SkyBase). */
    public void registerElevator(Location loc) {
        loc.getBlock().setType(Material.QUARTZ_BLOCK, false);
        this.elevators.add(key(loc));
        save();
        spawnHologram(loc.getBlock());
    }

    private boolean isElevatorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte b = item.getItemMeta().getPersistentDataContainer().get(this.itemKey, PersistentDataType.BYTE);
        return b != null && b == 1;
    }

    // ── Place / break ─────────────────────────────────────────────────────

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!isElevatorItem(event.getItemInHand())) return;
        Block block = event.getBlockPlaced();

        // Only allowed inside your own faction's claim.
        me.vcb.astrocore.factions.type.PlayerFaction faction =
            me.vcb.astrocore.factions.FactionsManager.getInstance().getPlayerFaction(event.getPlayer());
        me.vcb.astrocore.factions.Faction at =
            me.vcb.astrocore.factions.claim.ClaimManager.getInstance().getFactionAt(block.getLocation());
        if (faction == null || at == null || !at.equals(faction)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.translate("&cSolo podés poner elevadores dentro del claim de tu facción."));
            return;
        }

        this.elevators.add(key(block.getLocation()));
        save();
        spawnHologram(block);
        event.getPlayer().sendMessage(Color.translate("&aElevador colocado."));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String k = key(block.getLocation());
        if (!this.elevators.contains(k)) return;

        this.elevators.remove(k);
        save();
        removeHologram(block.getLocation());

        // Give back the elevator item (to the inventory) instead of plain quartz.
        event.setDropItems(false);
        Player player = event.getPlayer();
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            ItemStack give = createItem(1);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(give);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(it ->
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), it));
            }
        }
    }

    // ── Movement triggers ─────────────────────────────────────────────────

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getTo().getY() <= event.getFrom().getY()) return; // only upward = jump

        Player player = event.getPlayer();
        Block standing = blockUnder(event.getFrom());
        if (!this.elevators.contains(key(standing.getLocation()))) return;

        if (onCooldown(player)) return;
        teleport(player, standing.getLocation(), true);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        Block standing = blockUnder(player.getLocation());
        if (!this.elevators.contains(key(standing.getLocation()))) return;

        if (onCooldown(player)) return;
        teleport(player, standing.getLocation(), false);
    }

    private Block blockUnder(Location loc) {
        return loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
    }

    private boolean onCooldown(Player player) {
        long now = System.currentTimeMillis();
        Long last = this.cooldowns.get(player.getUniqueId());
        if (last != null && now - last < COOLDOWN_MS) return true;
        this.cooldowns.put(player.getUniqueId(), now);
        return false;
    }

    private void teleport(Player player, Location from, boolean up) {
        World world = from.getWorld();
        int x = from.getBlockX(), z = from.getBlockZ();
        int min = world.getMinHeight(), max = world.getMaxHeight() - 1;

        if (up) {
            for (int y = from.getBlockY() + 1; y <= max; y++) {
                if (this.elevators.contains(key(world, x, y, z))) { doTeleport(player, world, x, y, z); return; }
            }
        } else {
            for (int y = from.getBlockY() - 1; y >= min; y--) {
                if (this.elevators.contains(key(world, x, y, z))) { doTeleport(player, world, x, y, z); return; }
            }
        }
        player.sendMessage(Color.translate("&cNo hay otro elevador " + (up ? "arriba" : "abajo") + "."));
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
    }

    private void doTeleport(Player player, World world, int x, int y, int z) {
        Location dest = new Location(world, x + 0.5, y + 1, z + 0.5,
            player.getLocation().getYaw(), player.getLocation().getPitch());
        player.teleport(dest);
        player.setFallDistance(0f);
        player.playSound(dest, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f);
    }

    // ── Hologram ────────────────────────────────────────────────────────────

    private void spawnHologram(Block block) {
        Location loc = block.getLocation().add(0.5, 1.4, 0.5);
        TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, e -> {
            e.text(HologramEntry.buildComponent("&b&lJumpea para subir\n&b&lAgáchate para bajar"));
            e.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            e.setSeeThrough(true);
            e.setPersistent(true);
            e.setShadowed(true);
            e.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            e.setViewRange(0.0625f); // ~4 blocks (1.0 ≈ 64 blocks)
            e.addScoreboardTag(HOLO_TAG);
            e.addScoreboardTag(HOLO_TAG + ":" + key(block.getLocation()).replace(';', '_'));
        });
    }

    private void removeHologram(Location blockLoc) {
        String tag = HOLO_TAG + ":" + key(blockLoc).replace(';', '_');
        World world = blockLoc.getWorld();
        Location near = blockLoc.clone().add(0.5, 1.4, 0.5);
        world.getNearbyEntities(near, 1.0, 2.0, 1.0).forEach(en -> {
            if (en instanceof TextDisplay && en.getScoreboardTags().contains(tag)) en.remove();
        });
    }

    // ── Persistence ─────────────────────────────────────────────────────────

    private void load() {
        if (!this.file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);
        this.elevators.addAll(yaml.getStringList("elevators"));
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("elevators", new java.util.ArrayList<>(this.elevators));
        try { yaml.save(this.file); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Keys ──────────────────────────────────────────────────────────────

    private String key(Location loc) {
        return key(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String key(World world, int x, int y, int z) {
        return world.getName() + ";" + x + ";" + y + ";" + z;
    }
}

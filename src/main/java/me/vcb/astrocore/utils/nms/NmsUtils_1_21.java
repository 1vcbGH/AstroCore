package me.vcb.astrocore.utils.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.viaversion.viaversion.api.Via;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.abilities.AbilitiesManager;
import me.vcb.astrocore.abilities.AbilityType;
import me.vcb.astrocore.abilities.type.DecoyAbility;
import me.vcb.astrocore.abilities.type.InvisibilityAbility;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.games.dragon.EnderDragon;
import me.vcb.astrocore.games.dragon.nms.EnderDragon_1_21;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.glass.GlassInfo;
import me.vcb.astrocore.handlers.logger.CombatLogger;
import me.vcb.astrocore.handlers.logger.CombatLoggerType;
import me.vcb.astrocore.handlers.logger.nms.CombatLogger_1_21;
import me.vcb.astrocore.scoreboard.PlayerScoreboard;
import me.vcb.astrocore.scoreboard.nms.PlayerScoreboard_1_21;
import me.vcb.astrocore.tab.PlayerTab;
import me.vcb.astrocore.tab.nms.PlayerTab_1_21;
import me.vcb.astrocore.utils.Tasks;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class NmsUtils_1_21 extends NmsUtils implements Listener {

    public static final NamespacedKey COMBAT_LOGGER_KEY =
            new NamespacedKey(AstroCore.getInstance(), "combat_logger");

    private final Set<Material> clickableItems;
    private final Set<Material> purgeClickableItems;
    private final Set<Material> kitmapClickables;
    private final Set<Material> exoticBoneClickables;

    private ProtocolManager protocolManager;

    public NmsUtils_1_21() {
        Plugin viaPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
        boolean viaEnabled = viaPlugin != null && viaPlugin.isEnabled();

        this.clickableItems = EnumSet.of(
            Material.ANVIL, Material.BEACON, Material.FIRE,
            Material.OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.MANGROVE_FENCE_GATE, Material.CHERRY_FENCE_GATE,
            Material.OAK_BUTTON, Material.STONE_BUTTON, Material.BIRCH_BUTTON,
            Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.OAK_DOOR, Material.IRON_DOOR, Material.BIRCH_DOOR, Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
            Material.TRAPPED_CHEST, Material.FURNACE, Material.BREWING_STAND,
            Material.HOPPER, Material.DROPPER, Material.DISPENSER,
            Material.RED_BED, Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED, Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED,
            Material.GRAY_BED, Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED,
            Material.BLUE_BED, Material.BROWN_BED, Material.GREEN_BED, Material.BLACK_BED,
            Material.ENCHANTING_TABLE, Material.LEVER,
            Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.ACACIA_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.SPRUCE_TRAPDOOR,
            Material.CHEST, Material.REPEATER, Material.COMPARATOR,
            Material.JUKEBOX, Material.CRAFTING_TABLE, Material.ENDER_CHEST
        );

        this.purgeClickableItems = EnumSet.of(
            Material.ANVIL, Material.BEACON,
            Material.OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.OAK_BUTTON, Material.STONE_BUTTON,
            Material.OAK_DOOR, Material.IRON_DOOR, Material.BIRCH_DOOR, Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
            Material.TRAPPED_CHEST, Material.FURNACE, Material.BREWING_STAND,
            Material.HOPPER, Material.DROPPER, Material.DISPENSER,
            Material.ENCHANTING_TABLE, Material.LEVER,
            Material.OAK_TRAPDOOR, Material.CHEST, Material.JUKEBOX,
            Material.CRAFTING_TABLE, Material.ENDER_CHEST
        );

        this.kitmapClickables = EnumSet.of(
            Material.LEVER, Material.OAK_BUTTON, Material.STONE_BUTTON,
            Material.OAK_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.CRAFTING_TABLE, Material.FURNACE,
            Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.ACACIA_FENCE_GATE
        );

        this.exoticBoneClickables = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.HOPPER,
            Material.DROPPER, Material.DISPENSER,
            Material.OAK_TRAPDOOR, Material.CRAFTING_TABLE, Material.ENDER_CHEST,
            Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR,
            Material.JUNGLE_DOOR, Material.SPRUCE_DOOR, Material.OAK_DOOR,
            Material.OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE
        );

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.registerPacketListeners();

        this.bukkitExecutor = r -> Bukkit.getScheduler().runTask(AstroCore.getInstance(), r);

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        Tasks.asyncLater(() -> Bukkit.getOnlinePlayers().forEach(this::injectPacketInterceptor), 20L);
    }

    private void registerPacketListeners() {
        // Intercept outgoing ENTITY_EQUIPMENT packets for armor visibility (abilities)
        this.protocolManager.addPacketListener(new PacketAdapter(
            AstroCore.getInstance(),
            ListenerPriority.NORMAL,
            PacketType.Play.Server.ENTITY_EQUIPMENT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled()) return;

                int entityId = event.getPacket().getIntegers().read(0);
                Player recipient = event.getPlayer();

                // Find the entity whose equipment is being sent
                Entity sender = null;
                for (Entity e : recipient.getWorld().getEntities()) {
                    if (e.getEntityId() == entityId) {
                        sender = e;
                        break;
                    }
                }

                if (!(sender instanceof Player senderPlayer)) return;

                AbilitiesManager manager = AbilitiesManager.getInstance();

                InvisibilityAbility invisibilityAbility = (InvisibilityAbility) manager.getAbilityItemByType(AbilityType.INVISIBILITY);
                DecoyAbility decoyAbility = (DecoyAbility) manager.getAbilityItemByType(AbilityType.DECOY);

                boolean shouldCancel = (invisibilityAbility != null && invisibilityAbility.getPlayers().contains(senderPlayer.getUniqueId()))
                    || (decoyAbility != null && decoyAbility.getPlayers().contains(senderPlayer.getUniqueId()));

                if (shouldCancel) {
                    // Check the slot - only cancel armor slots (head, chest, legs, feet)
                    // In 1.21, equipment slots are sent as a list of (slot, item) pairs
                    // Cancel armor equipment packets entirely
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public void disable() {
        // ProtocolLib handles cleanup automatically
    }

    @Override
    public boolean isSpigot18() {
        return false;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public java.util.concurrent.Executor getBukkitExecutor() {
        return this.bukkitExecutor;
    }

    @Override
    public boolean isCustomSpigot() {
        return false;
    }

    @Override
    public CommandMap getCommandMap() {
        return Bukkit.getCommandMap();
    }

    @Override
    public Set<Material> getClickableItems() {
        return this.clickableItems;
    }

    @Override
    public Set<Material> getPurgeClickableItems() {
        return this.purgeClickableItems;
    }

    @Override
    public Set<Material> getKitmapClickables() {
        return this.kitmapClickables;
    }

    @Override
    public Set<Material> getExoticBoneClickables() {
        return this.exoticBoneClickables;
    }

    @Override
    public void registerCombatLogger(CombatLoggerType loggerType) {
        // No entity class registration needed in 1.21 - composition pattern
    }

    @Override
    public void registerEnderDragon() {
        // No entity class registration needed in 1.21 - composition pattern
    }

    @Override
    public boolean isCombatLogger(Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity living)) return false;
        return living.getPersistentDataContainer().has(COMBAT_LOGGER_KEY, PersistentDataType.STRING);
    }

    @Override
    public void strikeLightningEffect(Player player, Location loc) {
        float pitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.2F;

        Bukkit.getOnlinePlayers().forEach(online -> {
            if (player.getWorld() != online.getWorld()) return;
            me.vcb.astrocore.userdata.Userdata onlineData = AstroCore.getInstance().getUserdataManager().getUserdata(online);
            if (onlineData == null || !onlineData.getSettings().isLightning()) return;

            double dx = loc.getX() - online.getLocation().getX();
            double dy = loc.getY() - online.getLocation().getY();
            double dz = loc.getZ() - online.getLocation().getZ();

            if (dx * dx + dy * dy + dz * dz < 16384) {
                online.getWorld().strikeLightningEffect(loc);
                online.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10000.0F, pitch);
            }
        });
    }

    @Override
    public int getClientVersion(Player player) {
        Plugin viaPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
        if (viaPlugin != null && viaPlugin.isEnabled()) {
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        }
        return 767; // 1.21 protocol version
    }

    @Override
    public int getPing(Player player) {
        return player.getPing();
    }

    @Override
    public PlayerScoreboard getNewPlayerScoreboard(Player player) {
        return new PlayerScoreboard_1_21(player);
    }

    @Override
    public PlayerTab getNewPlayerTab(Player player) {
        return new PlayerTab_1_21(player);
    }

    @Override
    public void increaseStatistic(Player player, Statistic statistic, Material material) {
        try {
            if (material != null) {
                player.incrementStatistic(statistic, material);
            } else {
                player.incrementStatistic(statistic);
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void decreaseStatistic(Player player, Statistic statistic, Material material) {
        try {
            if (material != null) {
                player.decrementStatistic(statistic, material);
            } else {
                player.decrementStatistic(statistic);
            }
        } catch (Exception ignored) { }
    }

    @Override
    public boolean isInvulnerable(Player player) {
        return player.isInvulnerable();
    }

    @Override
    public void toggleInvulnerable(Player player) {
        player.setInvulnerable(!player.isInvulnerable());
    }

    @Override
    public ItemStack createMobSpawnerItemStack(EntityType spawnerType, String name) {
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta blockStateMeta = (BlockStateMeta) spawner.getItemMeta();

        if (name != null && blockStateMeta != null) {
            blockStateMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        }

        if (blockStateMeta != null) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
            creatureSpawner.setSpawnedType(spawnerType);
            blockStateMeta.setBlockState(creatureSpawner);
            spawner.setItemMeta(blockStateMeta);
        }

        return spawner;
    }

    @Override
    public int getPotionEffectDuration(Player player, PotionEffectType type) {
        PotionEffect effect = player.getPotionEffect(type);
        return effect != null ? effect.getDuration() : -1;
    }

    @Override
    public PotionEffect getPotionEffect(Player player, PotionEffectType type) {
        return player.getPotionEffect(type);
    }

    @Override
    public void addPotionEffect(Player player, PotionEffect effect) {
        player.removePotionEffect(effect.getType());

        if (this.isMainThread()) {
            player.addPotionEffect(effect);
        } else {
            Tasks.sync(() -> player.addPotionEffect(effect));
        }
    }

    @Override
    public void removeInfinitePotionEffect(Player player, PotionEffect toRemove) {
        PotionEffect current = player.getPotionEffect(toRemove.getType());

        if (current == null) return;
        if (toRemove.getAmplifier() != current.getAmplifier() || current.getDuration() < 12000) return;

        AstroCore.getInstance().getPvpClassManager().getPotionEffectRestorer()
                .removePlayerEffect(player, toRemove.getType());
    }

    @Override
    public Scoreboard getPlayerScoreboard(Player player) {
        return player.getScoreboard() == Bukkit.getScoreboardManager()
                .getMainScoreboard() ? Bukkit.getScoreboardManager().getNewScoreboard() : player.getScoreboard();
    }

    @Override
    public String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ");
    }

    @Override
    public List<ItemStack> getBlockDrops(ItemStack itemInHand, Block block) {
        if (block.getType() == Material.AIR) return new ArrayList<>();

        Collection<ItemStack> drops;
        if (itemInHand != null && itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            // Silk touch - return block itself
            drops = List.of(new ItemStack(block.getType()));
        } else {
            drops = block.getDrops(itemInHand);
        }

        return new ArrayList<>(drops);
    }

    @Override
    public void damageItemInHand(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int newDamage = damageable.getDamage() + 1;
            if (newDamage >= item.getType().getMaxDurability()) {
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
            } else {
                damageable.setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }
    }

    @Override
    public void changeServerSlots(int amount) {
        try {
            Field maxPlayersField = Bukkit.getServer().getClass().getDeclaredField("maxPlayers");
            maxPlayersField.setAccessible(true);
            maxPlayersField.set(Bukkit.getServer(), amount);
        } catch (Exception e) {
            try {
                // Fallback: try on the underlying server object
                Object nmsServer = Bukkit.getServer().getClass().getMethod("getHandle").invoke(Bukkit.getServer());
                Class<?> cls = nmsServer.getClass();
                while (cls != null) {
                    try {
                        Field f = cls.getDeclaredField("maxPlayers");
                        f.setAccessible(true);
                        f.set(nmsServer, amount);
                        return;
                    } catch (NoSuchFieldException ignored) {
                        cls = cls.getSuperclass();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void setViewDistance(int amount) {
        Bukkit.getWorlds().forEach(world -> world.setViewDistance(amount));
    }

    @Override
    public CombatLogger spawnCombatLogger(World world, Player player, CombatLoggerType loggerType) {
        return new CombatLogger_1_21(world, player, loggerType);
    }

    @Override
    public EnderDragon spawnEnderDragon(Location location, LootData loot) {
        return new EnderDragon_1_21(location, loot);
    }

    @Override
    public void sendHeaderAndFooter(Player player) {
        player.sendPlayerListHeaderAndFooter(
            LegacyComponentSerializer.legacySection().deserialize(Config.TAB_HEADER),
            LegacyComponentSerializer.legacySection().deserialize(Config.TAB_FOOTER)
        );
    }

    @Override
    public void injectPacketInterceptor(Player player) {
        if (!player.isOnline()) return;

        AbilitiesManager manager = AbilitiesManager.getInstance();

        InvisibilityAbility invisibilityAbility = (InvisibilityAbility) manager.getAbilityItemByType(AbilityType.INVISIBILITY);
        if (invisibilityAbility != null) {
            invisibilityAbility.hidePlayers(player);
        }

        DecoyAbility decoyAbility = (DecoyAbility) manager.getAbilityItemByType(AbilityType.DECOY);
        if (decoyAbility != null) {
            decoyAbility.hidePlayers(player);
        }
    }

    @Override
    public void deinjectPacketInterceptor(Player player) {
        // ProtocolLib manages this globally — no per-player cleanup needed
    }

    @Override
    public void updateArmor(Player player, boolean remove) {
        for (Player other : player.getWorld().getPlayers()) {
            if (other == player) continue;
            this.updateArmorFor(other, player, remove);
        }
        player.updateInventory();
    }

    @Override
    public void updateArmorFor(Player recipient, Player target, boolean remove) {
        try {
            PacketContainer packet = this.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
            packet.getIntegers().write(0, target.getEntityId());

            // Build equipment list
            List<com.comphenix.protocol.wrappers.Pair<com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot, ItemStack>> equipment = new ArrayList<>();

            ItemStack[] armorContents = target.getInventory().getArmorContents();
            ItemStack mainHand = target.getInventory().getItemInHand();

            equipment.add(new com.comphenix.protocol.wrappers.Pair<>(
                com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.MAINHAND,
                remove ? new ItemStack(Material.AIR) : mainHand));

            com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot[] armorSlots = {
                com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.FEET,
                com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.LEGS,
                com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.CHEST,
                com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.HEAD
            };

            for (int i = 0; i < 4; i++) {
                equipment.add(new com.comphenix.protocol.wrappers.Pair<>(
                    armorSlots[i],
                    remove ? new ItemStack(Material.AIR) : (armorContents[i] != null ? armorContents[i] : new ItemStack(Material.AIR))));
            }

            packet.getSlotStackPairLists().write(0, equipment);
            this.protocolManager.sendServerPacket(recipient, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        if (packet instanceof PacketContainer pc) {
            try {
                this.protocolManager.sendServerPacket(player, pc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendPackets(Player player, Object... packets) {
        for (Object packet : packets) {
            this.sendPacket(player, packet);
        }
    }

    @Override
    public void sendHologramTeleportPacket(Player player, int entityId, Location location) {
        // Handled via TextDisplay entity teleport in Hologram class
    }

    @Override
    public void sendHologramSpawnPacket(Player player, int entityId, Location location, String message) {
        // Handled via TextDisplay entity show in Hologram class
    }

    @Override
    public void sendHologramMessagePacket(Player player, int entityId, String message) {
        // Handled via TextDisplay entity text update in Hologram class
    }

    @Override
    public void sendHologramDestroyPacket(Player player, int entityId) {
        // Handled via TextDisplay entity hide in Hologram class
    }

    // Glass block event handling (replaces packet interception)
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        GlassInfo glassInfo = AstroCore.getInstance().getGlassManager().getGlassAt(player, location);
        if (glassInfo == null) return;

        event.setCancelled(true);
        player.sendBlockChange(location, glassInfo.getMaterial().createBlockData());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        GlassInfo glassInfo = AstroCore.getInstance().getGlassManager().getGlassAt(player, location);
        if (glassInfo != null) {
            player.sendBlockChange(location, glassInfo.getMaterial().createBlockData());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        GlassInfo glassInfo = AstroCore.getInstance().getGlassManager().getGlassAt(player, location);
        if (glassInfo == null) return;

        event.setCancelled(true);
        player.sendBlockChange(location, glassInfo.getMaterial().createBlockData());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.injectPacketInterceptor(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // No cleanup needed
    }
}

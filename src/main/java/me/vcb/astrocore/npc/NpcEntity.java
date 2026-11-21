package me.vcb.astrocore.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Packet-based NPC using ProtocolLib + NMS reflection.
 * Spawns as a fake player entity visible only via packets.
 */
public class NpcEntity {

    private static final ProtocolManager PM = ProtocolLibrary.getProtocolManager();

    // ── NMS reflection cache (initialised once) ───────────────────────────────

    private static Class<?> CLS_SERVER_PLAYER;
    private static Class<?> CLS_MINECRAFT_SERVER;
    private static Class<?> CLS_SERVER_LEVEL;
    private static Class<?> CLS_SERVER_ENTITY;
    private static Class<?> CLS_ENTITY;
    private static Class<?> CLS_GAME_PROFILE;
    private static Class<?> CLS_CLIENT_INFO;

    private static Constructor<?> CTOR_SERVER_PLAYER;
    private static Constructor<?> CTOR_SERVER_ENTITY;
    private static Constructor<?> CTOR_META_PACKET;
    private static Method         MTH_CLIENT_INFO_DEFAULT;
    private static Method         MTH_GET_HANDLE_SERVER;
    private static Method         MTH_ADD_ENTITY_PACKET;
    private static Method         MTH_ABS_MOVE_TO;
    private static Method         MTH_GET_ENTITY_DATA;
    private static Method         MTH_SET_DATA;
    private static Method         MTH_PACK_ALL;
    private static Method         MTH_PLAYER_INFO_INIT;  // ClientboundPlayerInfoUpdatePacket.createPlayerInitializing
    private static Field          FLD_ENTITY_ID;
    private static Field          FLD_CONN;              // ServerPlayer.connection
    private static Method         MTH_CONN_SEND;         // ServerGamePacketListenerImpl.send(Packet)
    private static Object         DATA_SKIN_PARTS;       // EntityDataAccessor<Byte> for skin layers
    private static final String NAMETAG_TEAM = "kc_npc_hide";

    private static boolean NMS_READY = false;

    /** Called by NpcManager after the server is fully started. */
    public static void initNms() {
        try {
            CLS_SERVER_PLAYER    = Class.forName("net.minecraft.server.level.ServerPlayer");
            CLS_MINECRAFT_SERVER = Class.forName("net.minecraft.server.MinecraftServer");
            CLS_SERVER_LEVEL     = Class.forName("net.minecraft.server.level.ServerLevel");
            CLS_SERVER_ENTITY    = Class.forName("net.minecraft.server.level.ServerEntity");
            CLS_ENTITY           = Class.forName("net.minecraft.world.entity.Entity");
            CLS_GAME_PROFILE     = Class.forName("com.mojang.authlib.GameProfile");
            CLS_CLIENT_INFO      = Class.forName("net.minecraft.server.level.ClientInformation");

            // ClientInformation.createDefault()
            MTH_CLIENT_INFO_DEFAULT = CLS_CLIENT_INFO.getDeclaredMethod("createDefault");

            // MinecraftServer static instance — CraftServer.getHandle() returns PlayerList, not MinecraftServer
            MTH_GET_HANDLE_SERVER = CLS_MINECRAFT_SERVER.getDeclaredMethod("getServer");

            // ServerPlayer constructor: (MinecraftServer, ServerLevel, GameProfile, ClientInformation)
            CTOR_SERVER_PLAYER = CLS_SERVER_PLAYER.getDeclaredConstructor(
                    CLS_MINECRAFT_SERVER, CLS_SERVER_LEVEL, CLS_GAME_PROFILE, CLS_CLIENT_INFO);
            CTOR_SERVER_PLAYER.setAccessible(true);

            // ServerEntity constructor — discover dynamically (param signature differs by build)
            for (Constructor<?> ctor : CLS_SERVER_ENTITY.getDeclaredConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                // Find the ctor whose 2nd param is Entity, 3rd int, 4th boolean
                if (params.length >= 4
                        && CLS_ENTITY.isAssignableFrom(params[1])
                        && params[2] == int.class && params[3] == boolean.class) {
                    CTOR_SERVER_ENTITY = ctor;
                    CTOR_SERVER_ENTITY.setAccessible(true);
                    AstroCore.getInstance().log("- &a[NPC] ServerEntity ctor: " + java.util.Arrays.toString(params));
                    break;
                }
            }
            if (CTOR_SERVER_ENTITY == null) {
                AstroCore.getInstance().log("&c[NPC] ServerEntity ctors available:");
                for (Constructor<?> ctor : CLS_SERVER_ENTITY.getDeclaredConstructors())
                    AstroCore.getInstance().log("  " + java.util.Arrays.toString(ctor.getParameterTypes()));
            }

            // Entity.getAddEntityPacket(ServerEntity)
            MTH_ADD_ENTITY_PACKET = CLS_ENTITY.getDeclaredMethod("getAddEntityPacket", CLS_SERVER_ENTITY);

            // Entity position+rotation setter: find method(double, double, double, float, float)
            for (Method m : CLS_ENTITY.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 5
                        && p[0] == double.class && p[1] == double.class && p[2] == double.class
                        && p[3] == float.class  && p[4] == float.class) {
                    MTH_ABS_MOVE_TO = m;
                    MTH_ABS_MOVE_TO.setAccessible(true);
                    break;
                }
            }

            // Entity.id private field
            FLD_ENTITY_ID = CLS_ENTITY.getDeclaredField("id");
            FLD_ENTITY_ID.setAccessible(true);

            // ServerPlayer.connection field — search hierarchy
            {
                Class<?> cls = CLS_SERVER_PLAYER;
                while (cls != null && FLD_CONN == null) {
                    for (Field f : cls.getDeclaredFields()) {
                        if (f.getName().equals("connection")) {
                            FLD_CONN = f;
                            FLD_CONN.setAccessible(true);
                            break;
                        }
                    }
                    cls = cls.getSuperclass();
                }
            }

            // ServerGamePacketListenerImpl.send(Packet<?>) — find single-arg send
            if (FLD_CONN != null) {
                Class<?> connCls = FLD_CONN.getType();
                // Walk hierarchy
                Class<?> probe = connCls;
                while (probe != null && MTH_CONN_SEND == null) {
                    for (Method m : probe.getDeclaredMethods()) {
                        if (m.getName().equals("send") && m.getParameterCount() == 1) {
                            Class<?> pt = m.getParameterTypes()[0];
                            // Prefer Packet param (not Object)
                            if (!pt.equals(Object.class)) {
                                MTH_CONN_SEND = m;
                                MTH_CONN_SEND.setAccessible(true);
                                break;
                            }
                        }
                    }
                    probe = probe.getSuperclass();
                }
            }

            // ── Entity-data + metadata-packet plumbing (optional, for skin layers) ──
            try {
                Class<?> playerCls = Class.forName("net.minecraft.world.entity.player.Player");
                Class<?> dataAccessorCls = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
                Class<?> synchedDataCls  = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
                Class<?> metaPacketCls   = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");

                // Find the EntityDataAccessor<Byte> static field for skin layers (DATA_PLAYER_MODE_CUSTOMISATION)
                for (Field f : playerCls.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                            && dataAccessorCls.isAssignableFrom(f.getType())
                            && f.getName().toLowerCase().contains("mode")) {
                        f.setAccessible(true);
                        DATA_SKIN_PARTS = f.get(null);
                        break;
                    }
                }

                MTH_GET_ENTITY_DATA = CLS_ENTITY.getDeclaredMethod("getEntityData");

                // SynchedEntityData.set(EntityDataAccessor, Object)
                for (Method m : synchedDataCls.getDeclaredMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (m.getName().equals("set") && p.length == 2
                            && dataAccessorCls.isAssignableFrom(p[0])) {
                        MTH_SET_DATA = m;
                        MTH_SET_DATA.setAccessible(true);
                        break;
                    }
                }

                // SynchedEntityData.packAll() returns List<DataValue<?>>
                for (Method m : synchedDataCls.getDeclaredMethods()) {
                    if (m.getName().equals("packAll") && m.getParameterCount() == 0) {
                        MTH_PACK_ALL = m;
                        MTH_PACK_ALL.setAccessible(true);
                        break;
                    }
                }

                // ClientboundSetEntityDataPacket(int id, List<DataValue<?>> values)
                for (Constructor<?> ctor : metaPacketCls.getDeclaredConstructors()) {
                    Class<?>[] p = ctor.getParameterTypes();
                    if (p.length == 2 && p[0] == int.class && List.class.isAssignableFrom(p[1])) {
                        CTOR_META_PACKET = ctor;
                        CTOR_META_PACKET.setAccessible(true);
                        break;
                    }
                }
            } catch (Throwable t) {
                AstroCore.getInstance().log("&e[NPC] Optional metadata reflection failed (skin layers won't show): " + t.getMessage());
            }

            // ── PLAYER_INFO via NMS (works around ProtocolLib texture stripping) ──
            try {
                Class<?> infoPacketCls = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
                for (Method m : infoPacketCls.getDeclaredMethods()) {
                    if (m.getName().equals("createPlayerInitializing")
                            && java.lang.reflect.Modifier.isStatic(m.getModifiers())
                            && m.getParameterCount() == 1) {
                        MTH_PLAYER_INFO_INIT = m;
                        MTH_PLAYER_INFO_INIT.setAccessible(true);
                        break;
                    }
                }
            } catch (Throwable t) {
                AstroCore.getInstance().log("&e[NPC] Optional PLAYER_INFO reflection failed (skin textures may not show): " + t.getMessage());
            }

            NMS_READY = (CTOR_SERVER_PLAYER != null && CTOR_SERVER_ENTITY != null
                    && MTH_ADD_ENTITY_PACKET != null && MTH_ABS_MOVE_TO != null
                    && FLD_ENTITY_ID != null);

            AstroCore.getInstance().log(NMS_READY
                    ? "- &a[NPC] NMS reflection OK."
                    : "&c[NPC] NMS reflection incomplete — NPC spawn may fail.");
        } catch (Exception e) {
            AstroCore.getInstance().log("&c[NPC] NMS init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Instance ──────────────────────────────────────────────────────────────

    private final NpcData data;
    private final int entityId;
    private final UUID uuid;
    /** Profile name used in the GameProfile — random so rank plugins don't match
     *  the skin owner's real name and slap a prefix on the nametag. */
    private final String profileName;
    private final Set<UUID> viewers = new HashSet<>();
    /** Pending tab-remove task per viewer, so a respawn can cancel a stale one. */
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> pendingTabRemoves = new HashMap<>();

    private NpcHologram hologram;

    public NpcEntity(NpcData data) {
        this.data        = data;
        this.entityId    = (int) (Math.random() * Integer.MAX_VALUE / 2) + 1_000_000;
        this.uuid        = UUID.randomUUID();
        // 16-char max name (Minecraft limit). Random hex from uuid — never matches a real player.
        this.profileName = "NPC_" + uuid.toString().replace("-", "").substring(0, 12);
    }

    // ── Spawn / Despawn ───────────────────────────────────────────────────────

    public void spawnFor(Player p) {
        if (viewers.contains(p.getUniqueId())) return;
        if (data.getLocation() == null) return;
        if (!data.getLocation().getWorld().equals(p.getWorld())) return;

        viewers.add(p.getUniqueId());

        Object rawProfile = buildRawProfile();
        Location loc = data.getLocation();

        // Tab-add + spawn + metadata as one NMS-driven flow (textures end-to-end).
        // Falls back to ProtocolLib path if NMS reflection is unavailable.
        if (NMS_READY && rawProfile != null) {
            sendNmsSpawnPacket(p, loc, rawProfile);
        } else {
            WrappedGameProfile wp = rawProfile != null
                    ? WrappedGameProfile.fromHandle(rawProfile)
                    : new WrappedGameProfile(uuid, profileName);
            sendTabAdd(p, wp);
            sendFallbackSpawnPacket(p, loc, wp);
        }

        // Equipment (skips silently if NPC has none)
        sendEquipment(p);

        // 4. Remove from tab list after short delay (so skin loads on the client).
        //    Track the task so a respawn can cancel a pending stale one (otherwise
        //    the old timer fires after the new spawn and hides the entity).
        org.bukkit.scheduler.BukkitTask task = Bukkit.getScheduler().runTaskLater(
                AstroCore.getInstance(),
                () -> {
                    pendingTabRemoves.remove(p.getUniqueId());
                    if (p.isOnline() && viewers.contains(p.getUniqueId())) sendTabRemove(p);
                },
                200L);
        pendingTabRemoves.put(p.getUniqueId(), task);

        if (hologram != null) hologram.showTo(p);
    }

    /**
     * Creates a real NMS ServerPlayer, sets its position + entity ID, then calls
     * getAddEntityPacket(ServerEntity) to get a fully-formed spawn packet.
     */
    private void sendNmsSpawnPacket(Player viewer, Location loc, Object gameProfile) {
        try {
            Object ms = MTH_GET_HANDLE_SERVER.invoke(null); // static method
            Object sl = loc.getWorld().getClass().getMethod("getHandle").invoke(loc.getWorld());
            Object ci = MTH_CLIENT_INFO_DEFAULT.invoke(null);

            // Create fake NMS ServerPlayer (not added to world)
            Object nmsPlayer = CTOR_SERVER_PLAYER.newInstance(ms, sl, gameProfile, ci);

            // Stamp our entity ID onto the NMS entity
            FLD_ENTITY_ID.setInt(nmsPlayer, entityId);

            // Give the fake player a connection so createPlayerInitializing() can
            // read connection.latency() without NPEing (NpcApi pattern, 1.20.2+).
            if (FLD_CONN != null) {
                try {
                    Object conn = buildFakeConnection(ms, nmsPlayer, gameProfile, ci);
                    if (conn != null) FLD_CONN.set(nmsPlayer, conn);
                } catch (Throwable t) { t.printStackTrace(); }
            }

            // Set position and rotation
            MTH_ABS_MOVE_TO.invoke(nmsPlayer,
                    loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            // Create ServerEntity tracker (just for generating the packet).
            // Build args by inspecting actual ctor parameter types — Paper uses
            // ServerEntity.Synchronizer (interface) instead of Consumer in newer builds.
            Class<?>[] paramTypes = CTOR_SERVER_ENTITY.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            args[0] = sl;
            args[1] = nmsPlayer;
            args[2] = 0;
            args[3] = false;
            for (int i = 4; i < paramTypes.length; i++) {
                Class<?> pt = paramTypes[i];
                if (pt.isInterface()) {
                    // Proxy any interface (Consumer, BiConsumer, Synchronizer, etc.) — no-op all methods
                    args[i] = java.lang.reflect.Proxy.newProxyInstance(
                            pt.getClassLoader(), new Class<?>[]{pt}, (proxy, method, methodArgs) -> null);
                } else if (Set.class.isAssignableFrom(pt)) {
                    args[i] = Set.of();
                } else {
                    args[i] = null;
                }
            }
            Object serverEntity = CTOR_SERVER_ENTITY.newInstance(args);

            // Set skin layers byte (cape, jacket, sleeves, pants, hat) on the entity data
            // BEFORE generating the spawn packet, so the value is set when we pack metadata.
            if (DATA_SKIN_PARTS != null && MTH_SET_DATA != null && MTH_GET_ENTITY_DATA != null) {
                Object entityData = MTH_GET_ENTITY_DATA.invoke(nmsPlayer);
                MTH_SET_DATA.invoke(entityData, DATA_SKIN_PARTS, (byte) 0x7F);
            }

            // Decide send path: prefer direct NMS connection.send() to bypass ProtocolLib
            // interceptors (rank/tab plugins strip textures and rewrite display names there).
            boolean directOk = (FLD_CONN != null && MTH_CONN_SEND != null);

            // ── 1. PLAYER_INFO ADD ──
            Object infoPacket = null;
            if (MTH_PLAYER_INFO_INIT != null) {
                try {
                    infoPacket = MTH_PLAYER_INFO_INIT.invoke(null, List.of(nmsPlayer));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            if (infoPacket != null && directOk) {
                sendDirect(viewer, infoPacket);
            } else if (infoPacket != null) {
                PacketContainer wrappedInfo = new PacketContainer(PacketType.Play.Server.PLAYER_INFO, infoPacket);
                PM.sendServerPacket(viewer, wrappedInfo);
            } else {
                sendTabAdd(viewer, WrappedGameProfile.fromHandle(gameProfile));
            }

            // ── 2. Scoreboard team to hide the entity nametag ──
            //    Must happen before the entity is spawned/rendered on the client.
            hideNametagFor(viewer);

            // ── 3. SPAWN_ENTITY ──
            Object addPacket = MTH_ADD_ENTITY_PACKET.invoke(nmsPlayer, serverEntity);
            if (directOk) {
                sendDirect(viewer, addPacket);
            } else {
                PacketContainer wrapped = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY, addPacket);
                PM.sendServerPacket(viewer, wrapped);
            }

            // ── 4. ENTITY_METADATA (skin layers etc.) ──
            if (MTH_PACK_ALL != null && CTOR_META_PACKET != null && MTH_GET_ENTITY_DATA != null) {
                Object entityData = MTH_GET_ENTITY_DATA.invoke(nmsPlayer);
                Object packed = MTH_PACK_ALL.invoke(entityData);
                if (packed instanceof List<?> list && !list.isEmpty()) {
                    Object metaPacket = CTOR_META_PACKET.newInstance(entityId, list);
                    if (directOk) {
                        sendDirect(viewer, metaPacket);
                    } else {
                        PacketContainer wrappedMeta = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA, metaPacket);
                        PM.sendServerPacket(viewer, wrappedMeta);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Fallback when NMS reflection is unavailable — builds packet manually (may not work on all 1.21 builds). */
    private void sendFallbackSpawnPacket(Player p, Location loc, WrappedGameProfile profile) {
        try {
            PacketContainer spawn = PM.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            spawn.getIntegers().write(0, entityId);
            spawn.getUUIDs().write(0, uuid);
            spawn.getEntityTypeModifier().write(0, org.bukkit.entity.EntityType.PLAYER);
            spawn.getDoubles().write(0, loc.getX());
            spawn.getDoubles().write(1, loc.getY());
            spawn.getDoubles().write(2, loc.getZ());
            spawn.getBytes().write(0, angleToByte(loc.getPitch()));
            spawn.getBytes().write(1, angleToByte(loc.getYaw()));
            spawn.getBytes().write(2, angleToByte(loc.getYaw()));
            spawn.getIntegers().write(1, 0);
            send(p, spawn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void despawnFor(Player p) {
        if (!viewers.remove(p.getUniqueId())) return;
        // Cancel any pending tab-remove so it can't fire after a respawn
        org.bukkit.scheduler.BukkitTask pending = pendingTabRemoves.remove(p.getUniqueId());
        if (pending != null) pending.cancel();
        sendDestroyPacket(p, entityId);
        // Remove the entry from the nametag-hide team
        try {
            org.bukkit.scoreboard.Team team = p.getScoreboard().getTeam(NAMETAG_TEAM);
            if (team != null && team.hasEntry(profileName)) team.removeEntry(profileName);
        } catch (Exception ignored) {}
        if (hologram != null) hologram.hideFrom(p);
    }

    public void despawnForAll() {
        for (UUID uid : new ArrayList<>(viewers)) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) despawnFor(p);
        }
        viewers.clear();
    }

    public boolean isViewing(Player p) {
        return viewers.contains(p.getUniqueId());
    }

    // ── Head/body look ────────────────────────────────────────────────────────

    public void lookAt(Player viewer, Location target) {
        Location npcLoc = data.getLocation();
        if (npcLoc == null) return;

        // target is already the player's eye location; subtract NPC eye height (1.62)
        double dx = target.getX() - npcLoc.getX();
        double dy = target.getY() - (npcLoc.getY() + 1.62);
        double dz = target.getZ() - npcLoc.getZ();

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, dist));

        PacketContainer look = PM.createPacket(PacketType.Play.Server.ENTITY_LOOK);
        look.getIntegers().write(0, entityId);
        look.getBytes().write(0, angleToByte(yaw));
        look.getBytes().write(1, angleToByte(pitch));
        look.getBooleans().write(0, false);
        send(viewer, look);

        sendHeadRotation(viewer, yaw);
    }

    // ── Equipment ─────────────────────────────────────────────────────────────

    public void sendEquipment(Player viewer) {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots = new ArrayList<>();
        addSlot(slots, EnumWrappers.ItemSlot.HEAD,     data.getHelmet());
        addSlot(slots, EnumWrappers.ItemSlot.CHEST,    data.getChestplate());
        addSlot(slots, EnumWrappers.ItemSlot.LEGS,     data.getLeggings());
        addSlot(slots, EnumWrappers.ItemSlot.FEET,     data.getBoots());
        addSlot(slots, EnumWrappers.ItemSlot.MAINHAND, data.getMainHand());
        addSlot(slots, EnumWrappers.ItemSlot.OFFHAND,  data.getOffHand());
        if (slots.isEmpty()) return;

        PacketContainer eq = PM.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        eq.getIntegers().write(0, entityId);
        eq.getSlotStackPairLists().write(0, slots);
        send(viewer, eq);
    }

    private void addSlot(List<Pair<EnumWrappers.ItemSlot, ItemStack>> list,
                         EnumWrappers.ItemSlot slot, ItemStack item) {
        if (item != null && !item.getType().isAir())
            list.add(new Pair<>(slot, item));
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /** Returns the raw authlib GameProfile (with skin textures injected). Returned as
     *  Object and consumed directly by the NMS ServerPlayer ctor — we DON'T round-trip
     *  it through ProtocolLib's WrappedGameProfile, because on Paper 1.21.10 that wrapper
     *  drops the texture property (getProperties() is broken / returns null there). */
    private Object buildRawProfile() {
        // Use the random profileName (not data.getSkinName()) so rank/tab plugins can't
        // match it to a real player and attach a rank prefix to the entity's nametag.
        try {
            Class<?> gpClass = Class.forName("com.mojang.authlib.GameProfile");

            if (data.getSkinTexture() == null || data.getSkinSignature() == null) {
                return gpClass.getConstructor(UUID.class, String.class).newInstance(uuid, profileName);
            }

            // On Paper 1.21.9+ the GameProfile's PropertyMap is IMMUTABLE — getProperties().put()
            // throws UnsupportedOperationException. Mirror NpcApi: build a fresh PropertyMap from a
            // Guava Multimap containing the texture Property, then pass it to the 3-arg
            // GameProfile(UUID, String, PropertyMap) constructor so the textures stick.
            Class<?> propClass = Class.forName("com.mojang.authlib.properties.Property");
            Object property = propClass.getConstructor(String.class, String.class, String.class)
                    .newInstance("textures", data.getSkinTexture(), data.getSkinSignature());

            com.google.common.collect.Multimap<String, Object> backing =
                    com.google.common.collect.LinkedHashMultimap.create();
            backing.put("textures", property);

            Class<?> pmClass = Class.forName("com.mojang.authlib.properties.PropertyMap");
            // PropertyMap ctor takes a Guava Multimap (ForwardingMultimap delegate).
            Object propertyMap = null;
            for (java.lang.reflect.Constructor<?> ct : pmClass.getConstructors()) {
                Class<?>[] pts = ct.getParameterTypes();
                if (pts.length == 1 && pts[0].isAssignableFrom(backing.getClass())) {
                    propertyMap = ct.newInstance(backing);
                    break;
                }
            }
            // Fallback: no Multimap ctor — try no-arg then put (older authlib).
            if (propertyMap == null) {
                propertyMap = pmClass.getDeclaredConstructor().newInstance();
                for (java.lang.reflect.Method m : propertyMap.getClass().getMethods()) {
                    if (m.getName().equals("put") && m.getParameterCount() == 2) {
                        m.invoke(propertyMap, "textures", property);
                        break;
                    }
                }
            }

            Object rawProfile = null;
            for (java.lang.reflect.Constructor<?> ct : gpClass.getConstructors()) {
                Class<?>[] pts = ct.getParameterTypes();
                if (pts.length == 3 && pts[0] == UUID.class && pts[1] == String.class
                        && pts[2].isAssignableFrom(pmClass)) {
                    rawProfile = ct.newInstance(uuid, profileName, propertyMap);
                    break;
                }
            }
            if (rawProfile == null) {
                // No 3-arg ctor — build 2-arg and inject the PropertyMap into the field.
                rawProfile = gpClass.getConstructor(UUID.class, String.class).newInstance(uuid, profileName);
                for (java.lang.reflect.Field f : gpClass.getDeclaredFields()) {
                    if (pmClass.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        f.set(rawProfile, propertyMap);
                        break;
                    }
                }
            }

            return rawProfile;
        } catch (Throwable e) {
            // Throwable, not Exception — runtime authlib mismatches throw NoSuchMethodError,
            // which must not abort the spawn (that made the NPC vanish entirely).
            e.printStackTrace();
            try {
                return Class.forName("com.mojang.authlib.GameProfile")
                        .getConstructor(UUID.class, String.class).newInstance(uuid, profileName);
            } catch (Throwable t) { return null; }
        }
    }

    /**
     * Builds a fake ServerGamePacketListenerImpl connection for the fake player so
     * ClientboundPlayerInfoUpdatePacket.createPlayerInitializing() can read
     * connection.latency() without NPEing. Mirrors NpcApi 1.20.2+ behaviour.
     */
    private static Object buildFakeConnection(Object ms, Object nmsPlayer, Object gameProfile, Object ci) {
        try {
            // PacketFlow.SERVERBOUND
            Class<?> flowClass = Class.forName("net.minecraft.network.protocol.PacketFlow");
            Object serverbound = null;
            for (Object c : flowClass.getEnumConstants()) {
                if (((Enum<?>) c).name().equals("SERVERBOUND")) { serverbound = c; break; }
            }

            // Connection(PacketFlow)
            Class<?> connClass = Class.forName("net.minecraft.network.Connection");
            Object connection = connClass.getConstructor(flowClass).newInstance(serverbound);

            // CommonListenerCookie ctor: first param is GameProfile
            Class<?> cookieClass = Class.forName("net.minecraft.server.network.CommonListenerCookie");
            Class<?> gpClass = Class.forName("com.mojang.authlib.GameProfile");
            Object cookie = null;
            for (java.lang.reflect.Constructor<?> ct : cookieClass.getConstructors()) {
                Class<?>[] pts = ct.getParameterTypes();
                if (pts.length == 0 || pts[0] != gpClass) continue;
                Object[] cargs = new Object[pts.length];
                for (int i = 0; i < pts.length; i++) {
                    Class<?> pt = pts[i];
                    if (pt == gpClass) cargs[i] = gameProfile;
                    else if (pt == int.class) cargs[i] = 0;
                    else if (pt == boolean.class) cargs[i] = true;
                    else if (pt.isInstance(ci)) cargs[i] = ci;
                    else cargs[i] = null;
                }
                cookie = ct.newInstance(cargs);
                break;
            }
            if (cookie == null) return null;

            // ServerGamePacketListenerImpl(MinecraftServer, Connection, ServerPlayer, CommonListenerCookie)
            Class<?> listenerClass = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
            for (java.lang.reflect.Constructor<?> ct : listenerClass.getConstructors()) {
                if (ct.getParameterCount() == 4) {
                    return ct.newInstance(ms, connection, nmsPlayer, cookie);
                }
            }
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /** Send an NMS packet directly via the viewer's connection (bypasses ProtocolLib interceptors). */
    private static void sendDirect(Player viewer, Object packet) throws Exception {
        Object nmsViewer = viewer.getClass().getMethod("getHandle").invoke(viewer);
        Object conn = FLD_CONN.get(nmsViewer);
        if (conn != null && MTH_CONN_SEND != null) {
            MTH_CONN_SEND.invoke(conn, packet);
        }
    }

    /** Hide the entity nametag by adding the profile name to a scoreboard team with
     *  nameTagVisibility=NEVER on the viewer's scoreboard. */
    private void hideNametagFor(Player viewer) {
        try {
            org.bukkit.scoreboard.Scoreboard board = viewer.getScoreboard();
            org.bukkit.scoreboard.Team team = board.getTeam(NAMETAG_TEAM);
            if (team == null) {
                team = board.registerNewTeam(NAMETAG_TEAM);
                team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                        org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            }
            if (!team.hasEntry(profileName)) team.addEntry(profileName);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendTabAdd(Player p, WrappedGameProfile profile) {
        try {
            PacketContainer tab = PM.createPacket(PacketType.Play.Server.PLAYER_INFO);
            Class nmsActionClass = EnumWrappers.getPlayerInfoActionClass();
            Object[] nmsConstants = nmsActionClass.getEnumConstants();
            EnumSet nmsActions = EnumSet.noneOf(nmsActionClass);
            for (EnumWrappers.PlayerInfoAction a : EnumSet.of(
                    EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                    EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                    EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                    EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)) {
                nmsActions.add(nmsConstants[a.ordinal()]);
            }
            tab.getModifier().write(0, nmsActions);
            tab.getLists(PlayerInfoData.getConverter()).write(0, Collections.singletonList(
                new PlayerInfoData(uuid, 1, true, NativeGameMode.SURVIVAL,
                    profile, WrappedChatComponent.fromText(data.getSkinName()))
            ));
            send(p, tab);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendTabRemove(Player p) {
        try {
            PacketContainer remove = PM.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            remove.getUUIDLists().write(0, Collections.singletonList(uuid));
            send(p, remove);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendHeadRotation(Player p, float yaw) {
        PacketContainer head = PM.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        head.getIntegers().write(0, entityId);
        head.getBytes().write(0, angleToByte(yaw));
        send(p, head);
    }

    private void sendSkinMeta(Player p) {
        PacketContainer meta = PM.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, entityId);
        var watchableList = new ArrayList<com.comphenix.protocol.wrappers.WrappedDataValue>();
        watchableList.add(new com.comphenix.protocol.wrappers.WrappedDataValue(
            17,
            com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry.get(Byte.class),
            (byte) 0x7F
        ));
        meta.getDataValueCollectionModifier().write(0, watchableList);
        send(p, meta);
    }

    private void sendDestroyPacket(Player p, int id) {
        PacketContainer destroy = PM.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, Collections.singletonList(id));
        send(p, destroy);
    }

    private static void send(Player p, PacketContainer packet) {
        try { PM.sendServerPacket(p, packet); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private static byte angleToByte(float angle) {
        return (byte) Math.floor(angle * 256f / 360f);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public NpcData getData()      { return data; }
    public int getEntityId()      { return entityId; }
    public Set<UUID> getViewers() { return Collections.unmodifiableSet(viewers); }

    public void setHologram(NpcHologram h) { this.hologram = h; }
    public NpcHologram getHologram()       { return hologram; }
}

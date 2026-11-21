package me.vcb.astrocore.disguise;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisguiseManager {

    private static DisguiseManager instance;
    private final Map<UUID, DisguiseData> disguises = new HashMap<>();
    private final ProtocolManager protocolManager;
    private final boolean srEnabled;

    // Reflexión SR
    private Object srApi;
    private Method getPlayerStorage;
    private Method setSkinIdOfPlayer;
    private Method removeSkinIdOfPlayer;
    private Method getSkinApplier;
    private Method applySkin;
    private Method skinIdentifierOfCustom;

    public DisguiseManager() {
        instance = this;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        srEnabled = initSkinRestorer();
        registerPacketListener();
    }

    public static DisguiseManager getInstance() { return instance; }

    // ── SkinRestorer reflexión ────────────────────────────────────────────────

    private boolean initSkinRestorer() {
        try {
            if (!Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) return false;
            Class<?> providerClass = Class.forName("net.skinsrestorer.api.SkinRestorerProvider");
            srApi = providerClass.getMethod("getSkinRestorer").invoke(null);
            Class<?> apiClass = srApi.getClass();
            Class<?> playerStorageClass = Class.forName("net.skinsrestorer.api.storage.PlayerStorage");
            Class<?> skinIdentifierClass = Class.forName("net.skinsrestorer.api.property.SkinIdentifier");
            getPlayerStorage     = apiClass.getMethod("getPlayerStorage");
            setSkinIdOfPlayer    = playerStorageClass.getMethod("setSkinIdOfPlayer", UUID.class, skinIdentifierClass);
            removeSkinIdOfPlayer = playerStorageClass.getMethod("removeSkinIdOfPlayer", UUID.class);
            skinIdentifierOfCustom = skinIdentifierClass.getMethod("ofCustom", String.class);
            getSkinApplier = apiClass.getMethod("getSkinApplier", Class.class);
            Object applierInstance = getSkinApplier.invoke(srApi, Player.class);
            applySkin = applierInstance.getClass().getMethod("applySkin", Object.class);
            AstroCore.getInstance().log("- &aDisguise: SkinsRestorer listo.");
            return true;
        } catch (Exception e) {
            AstroCore.getInstance().log("&eDisguise: SkinsRestorer no disponible.");
            return false;
        }
    }

    private void applySRSkin(Player player, String skinName) {
        Tasks.async(() -> {
            try {
                Object storage = getPlayerStorage.invoke(srApi);
                Object identifier = skinIdentifierOfCustom.invoke(null, skinName);
                setSkinIdOfPlayer.invoke(storage, player.getUniqueId(), identifier);
                Tasks.sync(() -> {
                    try {
                        Object applier = getSkinApplier.invoke(srApi, Player.class);
                        applySkin.invoke(applier, player);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
            } catch (Exception e) {
                Tasks.sync(() -> player.sendMessage(Color.translate(
                    "&e[Disguise] No se pudo obtener la skin de &b" + skinName + "&e.")));
            }
        });
    }

    private void clearSRSkin(Player player) {
        Tasks.async(() -> {
            try {
                Object storage = getPlayerStorage.invoke(srApi);
                removeSkinIdOfPlayer.invoke(storage, player.getUniqueId());
                Tasks.sync(() -> {
                    try {
                        Object applier = getSkinApplier.invoke(srApi, Player.class);
                        applySkin.invoke(applier, player);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public void disguise(Player player, String fakeName) {
        disguises.put(player.getUniqueId(), new DisguiseData(fakeName));

        // 1) Enviar PLAYER_INFO con nombre falso a todos los jugadores
        sendFakeProfileToAll(player, fakeName);

        // 2) Cambiar skin con SR (async, luego refreshForAll)
        if (srEnabled) {
            applySRSkin(player, fakeName);
            // Dar tiempo a SR para aplicar la skin, luego respawnear entidad
            Tasks.syncLater(() -> respawnEntityForAll(player), 20L);
        } else {
            // Sin SR: solo respawnear para que el cliente vea el nuevo nombre
            respawnEntityForAll(player);
        }

        player.setPlayerListName(Color.translate("&7" + fakeName));
        Tasks.syncLater(() -> AstroCore.getInstance().getScoreboardManager().updateTabRelations(player, false), 6L);
        player.sendMessage(Color.translate("&6[Disguise] &aDisfrazado como &b" + fakeName + "&a."));
    }

    public void undisguise(Player player) {
        if (!disguises.containsKey(player.getUniqueId())) {
            player.sendMessage(Color.translate("&c[Disguise] No estás disfrazado."));
            return;
        }
        disguises.remove(player.getUniqueId());

        // Enviar PLAYER_INFO con nombre real a todos
        sendRealProfileToAll(player);
        respawnEntityForAll(player);

        if (srEnabled) clearSRSkin(player);

        player.setPlayerListName(null);
        Tasks.syncLater(() -> AstroCore.getInstance().getScoreboardManager().updateTabRelations(player, false), 6L);
        player.sendMessage(Color.translate("&6[Disguise] &cDisfraz removido."));
    }

    public boolean isDisguised(Player player) {
        return disguises.containsKey(player.getUniqueId());
    }

    public String getFakeName(Player player) {
        DisguiseData d = disguises.get(player.getUniqueId());
        return d != null ? d.fakeName : player.getName();
    }

    public void disable() {
        new ArrayList<>(disguises.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) undisguise(p);
        });
        disguises.clear();
    }

    // ── Packets ───────────────────────────────────────────────────────────────

    /** Envía PLAYER_INFO ADD_PLAYER con perfil falso (nombre cambiado) a todos los jugadores online */
    private void sendFakeProfileToAll(Player player, String fakeName) {
        WrappedGameProfile fakeProfile = new WrappedGameProfile(player.getUniqueId(), fakeName);
        // Copiar propiedades del perfil real (skin actual) — envuelto en try por bug de ProtocolLib con GET_PROPERTIES
        try {
            WrappedGameProfile realProfile = WrappedGameProfile.fromPlayer(player);
            if (realProfile != null) fakeProfile.getProperties().putAll(realProfile.getProperties());
        } catch (Exception ignored) {}

        PlayerInfoData fakeEntry = new PlayerInfoData(
            player.getUniqueId(), 0, true,
            EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode()),
            fakeProfile,
            WrappedChatComponent.fromLegacyText(fakeName)
        );

        // Primero remover la entrada real
        sendPlayerInfoRemove(player);

        // Luego agregar con nombre falso
        Tasks.syncLater(() -> sendPlayerInfoAdd(Collections.singletonList(fakeEntry)), 1L);
    }

    /** Restaura el perfil real a todos los jugadores */
    private void sendRealProfileToAll(Player player) {
        WrappedGameProfile realProfile = null;
        try { realProfile = WrappedGameProfile.fromPlayer(player); } catch (Exception ignored) {}
        if (realProfile == null) return;

        PlayerInfoData realEntry = new PlayerInfoData(
            player.getUniqueId(), 0, true,
            EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode()),
            realProfile,
            WrappedChatComponent.fromLegacyText(player.getName())
        );

        sendPlayerInfoRemove(player);
        Tasks.syncLater(() -> sendPlayerInfoAdd(Collections.singletonList(realEntry)), 1L);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendPlayerInfoRemove(Player player) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
            packet.getUUIDLists().write(0, Collections.singletonList(player.getUniqueId()));
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online == player) continue;
                protocolManager.sendServerPacket(online, packet);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendPlayerInfoAdd(List<PlayerInfoData> entries) {
        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);

            Class nmsActionClass = EnumWrappers.getPlayerInfoActionClass();
            Object[] nmsConstants = nmsActionClass.getEnumConstants();
            EnumSet nmsActions = EnumSet.noneOf(nmsActionClass);
            for (EnumWrappers.PlayerInfoAction action : EnumSet.of(
                    EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                    EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                    EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                    EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)) {
                nmsActions.add(nmsConstants[action.ordinal()]);
            }
            packet.getModifier().write(0, nmsActions);
            packet.getLists(PlayerInfoData.getConverter()).write(0, entries);

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (entries.stream().anyMatch(e -> e.getProfileId().equals(online.getUniqueId()))) continue;
                protocolManager.sendServerPacket(online, packet);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Hide + show para forzar re-spawn del entity (el cliente cargará el nuevo perfil) */
    private void respawnEntityForAll(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online == player) return;
            online.hidePlayer(AstroCore.getInstance(), player);
        });
        Tasks.syncLater(() -> Bukkit.getOnlinePlayers().forEach(online -> {
            if (online == player) return;
            online.showPlayer(AstroCore.getInstance(), player);
        }), 3L);
    }

    /** Intercepta PLAYER_INFO ADD_PLAYER para cualquier otro send que no pasó por nosotros */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(
            AstroCore.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled() || disguises.isEmpty()) return;
                PacketContainer packet = event.getPacket();
                Object actionsRaw = packet.getModifier().read(0);
                if (actionsRaw == null || !actionsRaw.toString().contains("ADD_PLAYER")) return;

                List<PlayerInfoData> entries = packet.getLists(PlayerInfoData.getConverter()).read(0);
                if (entries == null || entries.isEmpty()) return;

                List<PlayerInfoData> modified = new ArrayList<>();
                boolean changed = false;

                for (PlayerInfoData entry : entries) {
                    WrappedGameProfile profile = entry.getProfile();
                    if (profile == null) { modified.add(entry); continue; }

                    Player disguisedPlayer = Bukkit.getPlayer(profile.getUUID());
                    DisguiseData ddata = disguisedPlayer != null ? disguises.get(disguisedPlayer.getUniqueId()) : null;

                    if (ddata != null) {
                        WrappedGameProfile fakeProfile = new WrappedGameProfile(profile.getUUID(), ddata.fakeName);
                        try { fakeProfile.getProperties().putAll(profile.getProperties()); } catch (Exception ignored) {}
                        modified.add(new PlayerInfoData(
                            profile.getUUID(), entry.getLatency(), entry.isListed(),
                            entry.getGameMode(), fakeProfile, entry.getDisplayName()
                        ));
                        changed = true;
                    } else {
                        modified.add(entry);
                    }
                }
                if (changed) packet.getLists(PlayerInfoData.getConverter()).write(0, modified);
            }
        });
    }

    static class DisguiseData {
        final String fakeName;
        DisguiseData(String fakeName) { this.fakeName = fakeName; }
    }
}

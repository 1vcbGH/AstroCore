package me.vcb.astrocore.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NpcListener implements Listener {

    // Debounce: a single right-click sends multiple USE_ENTITY packets
    // (INTERACT + INTERACT_AT, plus off-hand). Key = playerId|entityId.
    private final java.util.Map<String, Long> lastInteract = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long INTERACT_COOLDOWN_MS = 250L;

    public NpcListener() {
        // Listen for USE_ENTITY packets to detect NPC clicks
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(AstroCore.getInstance(),
                ListenerPriority.NORMAL,
                PacketType.Play.Client.USE_ENTITY) {

                @Override
                public void onPacketReceiving(PacketEvent event) {
                    PacketContainer packet = event.getPacket();
                    int entityId = packet.getIntegers().read(0);

                    NpcEntity npc = NpcManager.getInstance().getByEntityId(entityId);
                    if (npc == null) return;

                    // Only trigger on interact (action 0) or attack (action 1)
                    // EnumWrappers.EntityUseAction: 0=INTERACT, 1=ATTACK, 2=INTERACT_AT
                    int action = packet.getEnumEntityUseActions().read(0).getAction().ordinal();
                    if (action != 0 && action != 2) return; // ignore attack

                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    NpcData data  = npc.getData();

                    // Debounce duplicate packets from a single click
                    String key = player.getUniqueId() + "|" + entityId;
                    long now = System.currentTimeMillis();
                    Long last = lastInteract.get(key);
                    if (last != null && now - last < INTERACT_COOLDOWN_MS) return;
                    lastInteract.put(key, now);

                    Tasks.sync(() -> {
                        if (!player.isOnline()) return;
                        for (String cmd : data.getCommands()) {
                            String resolved = cmd.replace("%player%", player.getName());
                            if (resolved.startsWith("/")) resolved = resolved.substring(1);
                            player.performCommand(resolved);
                        }
                        if (data.getCommands().isEmpty()) {
                            player.sendMessage(Color.translate(
                                "§e[NPC] §f" + Color.translate(data.getDisplayName())));
                        }
                    });
                }
            }
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Tasks.syncLater(() -> NpcManager.getInstance().getAllNpcs()
            .forEach(d -> {
                NpcEntity entity = NpcManager.getInstance().getEntity(d.getId());
                if (entity != null) entity.spawnFor(event.getPlayer());
            }), 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        NpcManager.getInstance().getAllNpcs().forEach(d -> {
            NpcEntity entity = NpcManager.getInstance().getEntity(d.getId());
            if (entity != null) entity.despawnFor(p);
        });
    }
}

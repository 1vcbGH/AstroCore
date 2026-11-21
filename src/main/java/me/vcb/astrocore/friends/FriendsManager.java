package me.vcb.astrocore.friends;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.userdata.UserdataManager;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsManager implements Listener {

    private static FriendsManager instance;

    public FriendsManager() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public static FriendsManager getInstance() { return instance; }

    // ── helpers ───────────────────────────────────────────────────────────

    private UserdataManager udm() {
        return AstroCore.getInstance().getUserdataManager();
    }

    private void loadOfflineByName(String name, java.util.function.Consumer<Userdata> callback) {
        Tasks.async(() -> {
            Userdata ud = udm().getOfflineUserdataByName(name);
            Tasks.sync(() -> callback.accept(ud));
        });
    }

    // ── API pública ───────────────────────────────────────────────────────

    public void sendRequest(Player sender, String targetName) {
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            sendRequestTo(sender, onlineTarget.getUniqueId(), onlineTarget.getName(), true);
        } else {
            loadOfflineByName(targetName, ud -> {
                if (ud == null) {
                    sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado."));
                    return;
                }
                sendRequestTo(sender, ud.getUuid(), ud.getName(), false);
            });
        }
    }

    private void sendRequestTo(Player sender, UUID targetUuid, String targetName, boolean targetOnline) {
        if (sender.getUniqueId().equals(targetUuid)) {
            sender.sendMessage(Color.translate("&cNo puedes agregarte a ti mismo."));
            return;
        }

        Userdata senderUd = udm().getUserdata(sender);
        if (senderUd == null) return;

        if (senderUd.isFriendWith(targetUuid)) {
            sender.sendMessage(Color.translate("&eYa eres amigo de &c" + targetName + "&e."));
            return;
        }

        // Cargar userdata del target (online o offline) para leer/escribir pending requests
        Userdata targetUd = targetOnline
            ? udm().getUserdata(targetUuid)
            : udm().getOfflineUserdata(targetUuid);

        if (targetUd == null) {
            sender.sendMessage(Color.translate("&cNo se pudo procesar la solicitud."));
            return;
        }

        if (targetUd.getPendingFriendRequests().contains(sender.getUniqueId())) {
            sender.sendMessage(Color.translate("&eYa le enviaste una solicitud a &c" + targetName + "&e."));
            return;
        }

        // Si el target ya me mandó request → auto aceptar
        if (senderUd.getPendingFriendRequests().contains(targetUuid)) {
            senderUd.removePendingFriendRequest(targetUuid);
            Tasks.async(() -> udm().saveUserdata(senderUd));
            addFriendBoth(sender.getUniqueId(), targetUuid, sender.getName(), targetName, targetOnline);
            return;
        }

        // Agregar request en el userdata del target
        targetUd.addPendingFriendRequest(sender.getUniqueId());
        Tasks.async(() -> udm().saveUserdata(targetUd));

        sender.sendMessage(Color.translate("&eSolicitud de amistad enviada a &c" + targetName + "&e."));

        if (targetOnline) {
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null) {
                target.sendMessage(Color.translate("&e" + sender.getName()
                    + " &ete envió una solicitud de amistad. Usa &b/friend accept " + sender.getName() + "&e."));
            }
        }
        // Si es offline se le notificará al entrar (ver onJoin)
    }

    public void acceptRequest(Player player, String senderName) {
        Userdata playerUd = udm().getUserdata(player);
        if (playerUd == null) return;

        // Encontrar UUID del sender en las pending requests
        UUID senderUuid = null;
        String resolvedName = senderName;

        Player onlineSender = Bukkit.getPlayer(senderName);
        if (onlineSender != null) {
            senderUuid = onlineSender.getUniqueId();
            resolvedName = onlineSender.getName();
        } else {
            for (UUID uuid : playerUd.getPendingFriendRequests()) {
                Userdata ud = udm().getOfflineUserdata(uuid);
                if (ud != null && ud.getName().equalsIgnoreCase(senderName)) {
                    senderUuid = uuid;
                    resolvedName = ud.getName();
                    break;
                }
            }
        }

        if (senderUuid == null || !playerUd.getPendingFriendRequests().contains(senderUuid)) {
            player.sendMessage(Color.translate("&cNo tienes solicitud de &e" + senderName + "&c."));
            return;
        }

        playerUd.removePendingFriendRequest(senderUuid);
        Tasks.async(() -> udm().saveUserdata(playerUd));

        boolean senderIsOnline = onlineSender != null;
        addFriendBoth(player.getUniqueId(), senderUuid, player.getName(), resolvedName, senderIsOnline);
    }

    public void declineRequest(Player player, String senderName) {
        Userdata playerUd = udm().getUserdata(player);
        if (playerUd == null) return;

        UUID senderUuid = null;
        String resolvedName = senderName;

        Player onlineSender = Bukkit.getPlayer(senderName);
        if (onlineSender != null) {
            senderUuid = onlineSender.getUniqueId();
            resolvedName = onlineSender.getName();
        } else {
            for (UUID uuid : playerUd.getPendingFriendRequests()) {
                Userdata ud = udm().getOfflineUserdata(uuid);
                if (ud != null && ud.getName().equalsIgnoreCase(senderName)) {
                    senderUuid = uuid;
                    resolvedName = ud.getName();
                    break;
                }
            }
        }

        if (senderUuid == null || !playerUd.getPendingFriendRequests().contains(senderUuid)) {
            player.sendMessage(Color.translate("&cNo tienes solicitud de &e" + senderName + "&c."));
            return;
        }

        playerUd.removePendingFriendRequest(senderUuid);
        Tasks.async(() -> udm().saveUserdata(playerUd));

        player.sendMessage(Color.translate("&cRechazaste la solicitud de &e" + resolvedName + "&c."));
        if (onlineSender != null) {
            onlineSender.sendMessage(Color.translate("&c" + player.getName() + " &crechazó tu solicitud de amistad."));
        }
    }

    public void removeFriend(Player player, String targetName) {
        Userdata playerUd = udm().getUserdata(player);
        if (playerUd == null) return;

        UUID targetUuid = null;
        String resolvedName = targetName;

        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            targetUuid = onlineTarget.getUniqueId();
            resolvedName = onlineTarget.getName();
        } else {
            for (UUID uuid : playerUd.getFriends()) {
                Userdata ud = udm().getOfflineUserdata(uuid);
                if (ud != null && ud.getName().equalsIgnoreCase(targetName)) {
                    targetUuid = uuid;
                    resolvedName = ud.getName();
                    break;
                }
            }
        }

        if (targetUuid == null || !playerUd.isFriendWith(targetUuid)) {
            player.sendMessage(Color.translate("&eNo eres amigo de &c" + targetName + "&e."));
            return;
        }

        playerUd.removeFriend(targetUuid);
        Tasks.async(() -> udm().saveUserdata(playerUd));

        UUID finalTargetUuid = targetUuid;
        Tasks.async(() -> {
            Userdata targetUd = udm().getOfflineUserdata(finalTargetUuid);
            if (targetUd != null) {
                targetUd.removeFriend(player.getUniqueId());
                udm().saveUserdata(targetUd);
            }
        });

        player.sendMessage(Color.translate("&eEliminaste a &c" + resolvedName + " &ede tu lista de amigos."));
        if (onlineTarget != null) {
            onlineTarget.sendMessage(Color.translate("&c" + player.getName() + " &ete eliminó de su lista de amigos."));
        }
    }

    public void listFriends(Player player) {
        Userdata ud = udm().getUserdata(player);
        if (ud == null || ud.getFriends().isEmpty()) {
            player.sendMessage(Color.translate("&eNo tienes amigos agregados. Usa &b/friend add <jugador>&e."));
            return;
        }

        player.sendMessage(Color.translate("&6&lTus amigos &8(&e" + ud.getFriends().size() + "&8)&6:"));
        for (UUID uuid : ud.getFriends()) {
            Player friend = Bukkit.getPlayer(uuid);
            if (friend != null) {
                player.sendMessage(Color.translate("  &7- &a[En línea] &f" + friend.getName()));
            } else {
                Userdata fud = udm().getOfflineUserdata(uuid);
                String name = fud != null ? fud.getName() : uuid.toString();
                player.sendMessage(Color.translate("  &7- &c[Desconectado] &7" + name));
            }
        }
    }

    public void listRequests(Player player) {
        Userdata ud = udm().getUserdata(player);
        if (ud == null || ud.getPendingFriendRequests().isEmpty()) {
            player.sendMessage(Color.translate("&eNo tienes solicitudes de amistad pendientes."));
            return;
        }
        player.sendMessage(Color.translate("&6&lSolicitudes pendientes &8(&e"
            + ud.getPendingFriendRequests().size() + "&8)&6:"));
        for (UUID uuid : ud.getPendingFriendRequests()) {
            Player p = Bukkit.getPlayer(uuid);
            String name;
            if (p != null) {
                name = p.getName();
            } else {
                Userdata fud = udm().getOfflineUserdata(uuid);
                name = fud != null ? fud.getName() : uuid.toString();
            }
            player.sendMessage(Color.translate("  &7- &e" + name + " &7— &b/friend accept " + name));
        }
    }

    public boolean areFriends(UUID a, UUID b) {
        Userdata ud = udm().getOfflineUserdata(a);
        return ud != null && ud.isFriendWith(b);
    }

    // ── privado ───────────────────────────────────────────────────────────

    private void addFriendBoth(UUID a, UUID b, String nameA, String nameB, boolean bIsOnline) {
        Userdata udA = udm().getUserdata(a);
        if (udA != null) {
            udA.addFriend(b);
            Tasks.async(() -> udm().saveUserdata(udA));
        }

        Tasks.async(() -> {
            Userdata udB = udm().getOfflineUserdata(b);
            if (udB != null) {
                udB.addFriend(a);
                udm().saveUserdata(udB);
            }
        });

        Player playerA = Bukkit.getPlayer(a);
        if (playerA != null) playerA.sendMessage(Color.translate("&aAhora eres amigo de &e" + nameB + "&a."));
        if (bIsOnline) {
            Player playerB = Bukkit.getPlayer(b);
            if (playerB != null) playerB.sendMessage(Color.translate("&aAhora eres amigo de &e" + nameA + "&a."));
        }
    }

    // ── eventos ───────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Userdata ud = udm().getUserdata(player);
        if (ud == null) return;

        // Notificar a amigos online
        for (UUID uuid : ud.getFriends()) {
            Player friend = Bukkit.getPlayer(uuid);
            if (friend != null) {
                friend.sendMessage(Color.translate("&6[Amigos] &e" + player.getName() + " &ase conectó."));
            }
        }

        // Notificar solicitudes pendientes al entrar
        List<UUID> pending = new ArrayList<>(ud.getPendingFriendRequests());
        if (!pending.isEmpty()) {
            Tasks.syncLater(() -> {
                for (UUID senderUuid : pending) {
                    Player senderOnline = Bukkit.getPlayer(senderUuid);
                    String senderName;
                    if (senderOnline != null) {
                        senderName = senderOnline.getName();
                    } else {
                        Userdata senderUd = udm().getOfflineUserdata(senderUuid);
                        senderName = senderUd != null ? senderUd.getName() : senderUuid.toString();
                    }
                    player.sendMessage(Color.translate("&6[Amigos] &e" + senderName
                        + " &ete envió una solicitud de amistad. Usa &b/friend accept " + senderName + "&e."));
                }
            }, 40L); // 2 segundos después de entrar para que cargue bien
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Userdata ud = udm().getUserdata(player);
        if (ud == null) return;

        for (UUID uuid : ud.getFriends()) {
            Player friend = Bukkit.getPlayer(uuid);
            if (friend != null) {
                friend.sendMessage(Color.translate("&6[Amigos] &c" + player.getName() + " &7se desconectó."));
            }
        }
    }
}

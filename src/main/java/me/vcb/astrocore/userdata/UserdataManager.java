package me.vcb.astrocore.userdata;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.event.PlayerUsernameChangeEvent;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserdataManager implements Listener {

    protected final Map<UUID, Userdata> userdata;

    public UserdataManager() {
        this.userdata = new ConcurrentHashMap<>();

        Bukkit.getOnlinePlayers().forEach(this::loadUserdata);
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        this.userdata.keySet().forEach(uuid -> this.saveUserdata(uuid, false));
        this.userdata.clear();
    }

    private void loadUserdata(Player player) {
        this.loadUserdata(player.getUniqueId(), player.getName());
    }

    protected void loadUserdata(UUID uuid, String name) {
        if(this.userdata.containsKey(uuid)) return;

        File file = new File(Config.USERDATA_DIR, uuid + ".json");

        if(!file.exists()) {
            this.userdata.put(uuid, new Userdata(uuid, name));
            return;
        }

        String content = FileUtils.readWholeFile(file);
        if(content == null) return;

        Userdata userdata = AstroCore.getInstance().getGson().fromJson(content, Userdata.class);

        this.userdata.put(uuid, userdata);
        this.checkUsernameChange(userdata, name);
    }

    private void saveUserdata(Player player) {
        this.saveUserdata(player.getUniqueId(), true);
    }

    public void saveUserdata(UUID uuid, boolean remove) {
        Userdata userdata = this.getUserdata(uuid);
        if(userdata == null) return;

        File file = FileUtils.getOrCreateFile(Config.USERDATA_DIR, uuid + ".json");

        FileUtils.writeString(file, AstroCore.getInstance().getGson().toJson(userdata, Userdata.class));
        if(remove) this.userdata.remove(uuid);
    }

    public void saveUserdata(Userdata userdata) {
        if(userdata == null) return;

        File file = FileUtils.getOrCreateFile(Config.USERDATA_DIR, userdata.getUuid() + ".json");
        FileUtils.writeString(file, AstroCore.getInstance().getGson().toJson(userdata, Userdata.class));
    }

    public Userdata getUserdata(Player player) {
        return this.getUserdata(player.getUniqueId());
    }

    public Userdata getUserdata(UUID uuid) {
        return this.userdata.get(uuid);
    }

    public Userdata getOfflineUserdata(UUID uuid) {
        if (this.userdata.containsKey(uuid)) return this.userdata.get(uuid);
        File file = new File(Config.USERDATA_DIR, uuid + ".json");
        if (!file.exists()) return null;
        String content = FileUtils.readWholeFile(file);
        if (content == null) return null;
        return AstroCore.getInstance().getGson().fromJson(content, Userdata.class);
    }

    /** Returns all currently loaded userdatas (online players + recently online). */
    public java.util.Collection<Userdata> getAllLoadedUserdata() {
        return this.userdata.values();
    }

    public Userdata getOfflineUserdataByName(String name) {
        // Check loaded userdata first
        for (Userdata ud : this.userdata.values()) {
            if (ud.getName().equalsIgnoreCase(name)) return ud;
        }
        // Scan files
        File[] files = Config.USERDATA_DIR.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return null;
        for (File file : files) {
            String content = FileUtils.readWholeFile(file);
            if (content == null) continue;
            Userdata ud = AstroCore.getInstance().getGson().fromJson(content, Userdata.class);
            if (ud != null && ud.getName().equalsIgnoreCase(name)) return ud;
        }
        return null;
    }

    public Userdata getUserdata(OfflinePlayer player) {
        if(this.userdata.containsKey(player.getUniqueId())) return this.getUserdata(player.getUniqueId());

        File file = new File(Config.USERDATA_DIR, player.getUniqueId() + ".json");
        if(!file.exists()) return null;

        String content = FileUtils.readWholeFile(file);
        if(content == null) return null;

        Userdata userdata = AstroCore.getInstance().getGson().fromJson(content, Userdata.class);
        this.userdata.put(player.getUniqueId(), userdata);

        return userdata;
    }

    protected void checkUsernameChange(Userdata userdata, String name) {
        if(userdata.getName().equals(name)) return;

        userdata.setName(name);
        new PlayerUsernameChangeEvent(userdata, name);
    }

    public void deleteAllUserdata() {
        int deleted = this.userdata.size();

        Config.USERDATA_DIR.delete();
        this.userdata.clear();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            this.userdata.put(uuid, new Userdata(uuid, player.getName()));
        });

        AstroCore.getInstance().log("- &cDeleted &e" + deleted + " &cuserdata files.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        this.loadUserdata(event.getUniqueId(), event.getName());

        Userdata ud = this.getUserdata(event.getUniqueId());
        if(ud == null) {
            event.disallow(Result.KICK_OTHER, Language.USERDATA_FAILED_TO_LOAD);
            return;
        }

        // Guardar última IP
        String ip = event.getAddress().getHostAddress();
        ud.setLastIp(ip);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Userdata data = this.getUserdata(event.getPlayer());
        if(data == null) return;

        data.getKitDelays().values().removeIf(delay -> delay != -1 && delay < System.currentTimeMillis());

        Tasks.async(() -> this.saveUserdata(event.getPlayer()));
    }
}

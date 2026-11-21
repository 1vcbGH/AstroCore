package me.vcb.astrocore.partner;

import com.google.gson.reflect.TypeToken;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.partner.menu.PartnerRewardMenu;
import me.vcb.astrocore.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartnerManager implements Listener {

    private static final long COOLDOWN_DURATION = 24L * 60L * 60L * 1000L;

    private final File dataFile;
    private List<Partner> partners;

    private final Map<UUID, Map<String, Long>> claimCooldowns = new HashMap<>();
    private final Map<UUID, String> pendingChatInput = new HashMap<>();

    public PartnerManager() {
        this.dataFile = FileUtils.getOrCreateFile(AstroCore.getInstance().getDataFolder(), "partners.json");
        this.load();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    private void load() {
        String content = FileUtils.readWholeFile(this.dataFile);
        if (content == null || content.isEmpty()) {
            this.partners = new ArrayList<>();
            return;
        }
        Type type = new TypeToken<List<Partner>>() {}.getType();
        List<Partner> loaded = AstroCore.getInstance().getGson().fromJson(content, type);
        this.partners = loaded != null ? loaded : new ArrayList<>();
    }

    public void save() {
        Type type = new TypeToken<List<Partner>>() {}.getType();
        String json = AstroCore.getInstance().getGson().toJson(this.partners, type);
        FileUtils.writeString(this.dataFile, json);
    }

    public Partner getPartner(String nick) {
        for (Partner partner : this.partners) {
            if (partner.getNick().equalsIgnoreCase(nick)) {
                return partner;
            }
        }
        return null;
    }

    public List<Partner> getVisiblePartnersSorted() {
        return this.partners.stream()
            .filter(p -> !p.isHidden())
            .sorted(Comparator.comparingInt(Partner::getClaimCount).reversed())
            .collect(Collectors.toList());
    }

    public List<Partner> getAllPartners() {
        return new ArrayList<>(this.partners);
    }

    public boolean createPartner(String nick, String displayName) {
        if (this.getPartner(nick) != null) return false;
        this.partners.add(new Partner(nick, displayName));
        this.save();
        return true;
    }

    public boolean deletePartner(String nick) {
        Partner partner = this.getPartner(nick);
        if (partner == null) return false;
        this.partners.remove(partner);
        this.save();
        return true;
    }

    public boolean hidePartner(String nick) {
        Partner partner = this.getPartner(nick);
        if (partner == null) return false;
        partner.setHidden(true);
        this.save();
        return true;
    }

    public boolean showPartner(String nick) {
        Partner partner = this.getPartner(nick);
        if (partner == null) return false;
        partner.setHidden(false);
        this.save();
        return true;
    }

    public boolean resetStats(String nick) {
        Partner partner = this.getPartner(nick);
        if (partner == null) return false;
        partner.setClaimCount(0);
        this.save();
        return true;
    }

    public boolean isOnCooldown(UUID uuid, String nick) {
        Map<String, Long> map = this.claimCooldowns.get(uuid);
        if (map == null) return false;
        Long time = map.get(nick.toLowerCase());
        if (time == null) return false;
        return (System.currentTimeMillis() - time) < COOLDOWN_DURATION;
    }

    public void claimPartner(Player player, Partner partner) {
        String name = player.getName();
        for (String cmd : partner.getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", name));
        }
        this.claimCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(partner.getNick().toLowerCase(), System.currentTimeMillis());
        partner.setClaimCount(partner.getClaimCount() + 1);
        this.save();
    }

    public void startChatInput(UUID uuid, String partnerNick) {
        this.pendingChatInput.put(uuid, partnerNick);
    }

    public void cancelChatInput(UUID uuid) {
        this.pendingChatInput.remove(uuid);
    }

    public boolean hasPendingChatInput(UUID uuid) {
        return this.pendingChatInput.containsKey(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!this.pendingChatInput.containsKey(uuid)) return;

        event.setCancelled(true);
        String partnerNick = this.pendingChatInput.remove(uuid);
        String command = event.getMessage();

        Bukkit.getScheduler().runTask(AstroCore.getInstance(), () -> {
            Partner partner = this.getPartner(partnerNick);
            if (partner == null) {
                player.sendMessage("§cPartner no encontrado.");
                return;
            }
            partner.getRewardCommands().add(command);
            this.save();
            player.sendMessage("§aComando añadido: §f" + command);
            new PartnerRewardMenu(partner).open(player);
        });
    }
}

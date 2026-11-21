package me.vcb.astrocore.lunarclient.cooldown;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.timer.Timer;
import me.vcb.astrocore.timer.event.TimerActivateEvent;
import me.vcb.astrocore.timer.event.TimerCancelEvent;
import me.vcb.astrocore.timer.event.TimerExpireEvent;
import me.vcb.astrocore.timer.type.PlayerTimer;
import me.vcb.astrocore.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager implements Listener {

    private final Map<CooldownType, LunarClientCooldown> cooldowns;

    public CooldownManager() {
        this.cooldowns = new HashMap<>();

        this.setupCooldowns();

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        this.cooldowns.clear();
    }

    private void setupCooldowns() {
        ConfigurationSection section = AstroCore.getInstance().getConfig().getSection("COOLDOWNS");

        section.getKeys(false).forEach(cooldown -> {
            LunarClientCooldown lunarClientCooldown = new LunarClientCooldown();
            lunarClientCooldown.setName(section.getString(cooldown + ".NAME"));

            ItemStack itemStack = ItemUtils.parseItem(section.getString(cooldown + ".MATERIAL_ID"));
            if(itemStack == null) return;

            lunarClientCooldown.setMaterial(itemStack.getType());

            CooldownType type = CooldownType.valueOf(cooldown);
            this.cooldowns.put(type, lunarClientCooldown);
        });
    }

    private void addCooldown(UUID uuid, CooldownType type, int duration) {
        if(!AstroCore.getInstance().getLunarClientManager().isOnLunarClient(uuid)) return;

        LunarClientCooldown cooldown = this.cooldowns.get(type);
        if(cooldown == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if(player != null) cooldown.createCooldown(duration).send(player);
    }

    private void removeCooldown(UUID uuid, CooldownType type) {
        if(!AstroCore.getInstance().getLunarClientManager().isOnLunarClient(uuid)) return;

        LunarClientCooldown cooldown = this.cooldowns.get(type);
        if(cooldown == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if(player != null) cooldown.clearCooldown().clear(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTimerActivate(TimerActivateEvent event) {
        CooldownType type = this.getCooldown(event.getTimer());
        if(type == null) return;

        this.addCooldown(event.getUuid(), type, event.getDelay());
    }

    @EventHandler
    public void onTimerCancel(TimerCancelEvent event) {
        CooldownType type = this.getCooldown(event.getTimer());
        if(type == null) return;

        this.removeCooldown(event.getUuid(), type);
    }

    @EventHandler
    public void onTimerExpire(TimerExpireEvent event) {
        CooldownType type = this.getCooldown(event.getTimer());
        if(type == null) return;

        this.removeCooldown(event.getUuid(), type);
    }

    private CooldownType getCooldown(Timer timer) {
        return !(timer instanceof PlayerTimer) ? null : timer.getLunarCooldownType();
    }
}

package me.vcb.astrocore.killeffects;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class KillEffectHandler implements Listener {

    public KillEffectHandler() {
        org.bukkit.Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer == victim) return;

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(killer);
        if (ud == null || ud.getSelectedKillEffect() == null) return;

        KillEffect effect = KillEffect.fromId(ud.getSelectedKillEffect());
        if (effect == KillEffect.NINGUNO) return;

        Location loc = victim.getLocation();
        playEffect(effect, loc, killer);
    }

    private void playEffect(KillEffect effect, Location loc, Player killer) {
        World world = loc.getWorld();
        if (world == null) return;

        switch (effect) {
            case RAYO:
                world.strikeLightningEffect(loc);
                world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                break;

            case FUEGOS:
                me.vcb.astrocore.utils.Tasks.syncLater(() -> spawnFirework(loc, killer), 1L);
                break;

            case EXPLOSION:
                world.spawnParticle(Particle.EXPLOSION, loc, 3, 0.3, 0.3, 0.3, 0);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
                break;

            case LLAMAS:
                world.spawnParticle(Particle.FLAME, loc.clone().add(0, 0.5, 0), 80, 0.4, 1.2, 0.4, 0.05);
                world.playSound(loc, Sound.ENTITY_BLAZE_HURT, 1f, 0.8f);
                break;

            case DRAGON:
                // Polvo morado oscuro — columna dracónica
                DustOptions purple = new DustOptions(Color.fromRGB(80, 0, 120), 2.0f);
                DustOptions darkPurple = new DustOptions(Color.fromRGB(40, 0, 80), 1.5f);
                world.spawnParticle(Particle.DUST, loc.clone().add(0, 0.5, 0), 60, 0.4, 1.2, 0.4, 0, purple);
                world.spawnParticle(Particle.DUST, loc.clone().add(0, 0.5, 0), 40, 0.5, 1.5, 0.5, 0, darkPurple);
                world.spawnParticle(Particle.WITCH, loc.clone().add(0, 1, 0), 25, 0.4, 0.8, 0.4, 0.08);
                world.spawnParticle(Particle.SMOKE, loc.clone().add(0, 0.2, 0), 15, 0.3, 0.3, 0.3, 0.02);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
                break;

            case CORAZON:
                world.spawnParticle(Particle.HEART, loc.clone().add(0, 1, 0), 20, 0.6, 0.8, 0.6, 0.1);
                world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.8f);
                break;

            default:
                break;
        }
    }

    private void spawnFirework(Location loc, Player killer) {
        World world = loc.getWorld();
        if (world == null) return;

        Firework fw = world.spawn(loc.clone().add(0, 1, 0), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        // Color basado en nombre del killer (determinista pero variado)
        int hash = killer.getName().hashCode();
        Color c1 = Color.fromRGB(Math.abs(hash)         % 0xFFFFFF);
        Color c2 = Color.fromRGB(Math.abs(hash * 31)    % 0xFFFFFF);

        meta.addEffect(FireworkEffect.builder()
            .withColor(c1, c2)
            .withFade(Color.WHITE)
            .with(FireworkEffect.Type.BURST)
            .trail(true)
            .flicker(true)
            .build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);

        // Detonar inmediatamente (1 tick)
        me.vcb.astrocore.utils.Tasks.syncLater(fw::detonate, 2L);
    }
}

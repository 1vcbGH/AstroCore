package me.vcb.astrocore.customenchants;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class CustomEnchantHandler implements Listener {

    private static final int EFFECT_DURATION  = 12;   // ticks (refresh every 8, give 12 = overlap)
    private static final int REFRESH_INTERVAL = 8;    // ticks

    private static final int POISON_TICKS  = 60;  // 3s
    private static final int SLOW_TICKS    = 60;
    private static final int FIRE_TICKS    = 30;  // 1.5s level I, 60 level II
    private static final int COMBO_TICKS   = 60;  // 3s
    private static final int RECOVER_REGEN_TICKS = 60; // 3s regen burst
    private static final long RECOVER_COOLDOWN_MS = 15_000L; // 15s
    private static final double RECOVER_HP_THRESHOLD = 8.0; // 4 hearts (each heart = 2hp)
    private BukkitTask armorTask;
    private final Map<UUID, Long> recoverCooldown = new HashMap<>();

    public CustomEnchantHandler() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        this.armorTask = Tasks.syncTimer(this::tickAll, REFRESH_INTERVAL, REFRESH_INTERVAL);
    }

    public void disable() {
        if (this.armorTask != null) { this.armorTask.cancel(); this.armorTask = null; }
        this.recoverCooldown.clear();
    }

    // ── Armor tick ───────────────────────────────────────────────────────────

    private void tickAll() {
        for (Player p : Bukkit.getOnlinePlayers()) tickArmor(p);
    }

    private void tickArmor(Player player) {
        Map<CustomEnchant, Integer> totals = getArmorEnchants(player);

        // VELOZ → always Speed II (amplifier 1)
        if (totals.containsKey(CustomEnchant.VELOZ)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, EFFECT_DURATION, 1, true, false));
        }

        // SPRING → Jump Boost I or II
        if (totals.containsKey(CustomEnchant.SPRING)) {
            int lvl = Math.min(totals.get(CustomEnchant.SPRING), 2) - 1; // 0 or 1
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST, EFFECT_DURATION, lvl, true, false));
        }

        // REGENERATIVO → Regen I
        if (totals.containsKey(CustomEnchant.REGENERATIVO)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION, EFFECT_DURATION, 0, true, false));
        }

        // NINJA → Invisibilidad permanente (pantalón)
        if (totals.containsKey(CustomEnchant.NINJA)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, EFFECT_DURATION, 0, true, false));
        }

        // IGNIFUGO → Fire Resistance I permanente (pantalón)
        if (totals.containsKey(CustomEnchant.IGNIFUGO)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, EFFECT_DURATION, 0, true, false));
        }

        // IMPLANTES → Absorption I/II permanente (pechera)
        if (totals.containsKey(CustomEnchant.IMPLANTES)) {
            int lvl = Math.min(totals.get(CustomEnchant.IMPLANTES), 2) - 1;
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION, EFFECT_DURATION, lvl, true, false));
        }

        // FURIA → Strength I permanente (pantalón)
        if (totals.containsKey(CustomEnchant.FURIA)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, EFFECT_DURATION, 0, true, false));
        }

        // RECUPERAR → Regen II por 3s al caer de 4 corazones (cd 15s)
        if (totals.containsKey(CustomEnchant.RECUPERAR)) {
            if (player.getHealth() <= RECOVER_HP_THRESHOLD) {
                long now = System.currentTimeMillis();
                Long last = recoverCooldown.get(player.getUniqueId());
                if (last == null || now - last >= RECOVER_COOLDOWN_MS) {
                    recoverCooldown.put(player.getUniqueId(), now);
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, RECOVER_REGEN_TICKS, 1, false, true));
                }
            }
        }

    }

    // ── Damage events ────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))     return;
        if (!(event.getEntity()  instanceof LivingEntity)) return;

        Player      attacker = (Player) event.getDamager();
        LivingEntity victim  = (LivingEntity) event.getEntity();
        double      damage   = event.getFinalDamage();

        CustomEnchantManager cem = CustomEnchantManager.getInstance();

        // ── Weapon enchants ──────────────────────────────────────────────────
        ItemStack hand = attacker.getInventory().getItemInMainHand();
        Map<CustomEnchant, Integer> weapon = cem.getEnchants(hand);

        // VENENO_X — Poison I/II
        if (weapon.containsKey(CustomEnchant.VENENO_X)) {
            int lvl = weapon.get(CustomEnchant.VENENO_X);
            victim.addPotionEffect(new PotionEffect(
                PotionEffectType.POISON, POISON_TICKS, lvl - 1, false, true));
        }

        // LLAMAS_X — fire 1.5s/3s
        if (weapon.containsKey(CustomEnchant.LLAMAS_X)) {
            int lvl = weapon.get(CustomEnchant.LLAMAS_X);
            victim.setFireTicks(FIRE_TICKS * lvl);
        }

        // RALENTIZAR — Slowness I/II
        if (weapon.containsKey(CustomEnchant.RALENTIZAR)) {
            int lvl = weapon.get(CustomEnchant.RALENTIZAR);
            victim.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, SLOW_TICKS, lvl - 1, false, true));
        }

        // HOLY — Weakness I
        if (weapon.containsKey(CustomEnchant.HOLY)) {
            victim.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS, 60, 0, false, true));
        }

        // ── Armor enchants on attacker ───────────────────────────────────────
        Map<CustomEnchant, Integer> atkArmor = getArmorEnchants(attacker);

        // VAMPIRISMO — 5%/8%/12% lifesteal
        if (atkArmor.containsKey(CustomEnchant.VAMPIRISMO)) {
            int lvl = atkArmor.get(CustomEnchant.VAMPIRISMO);
            double[] pcts = { 0.05, 0.08, 0.12 };
            double heal = damage * pcts[Math.min(lvl - 1, 2)];
            double maxHp = attacker.getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            attacker.setHealth(Math.min(attacker.getHealth() + heal, maxHp));
        }

        // ── Armor enchants on victim (defender) ──────────────────────────────
        if (victim instanceof Player) {
            Player defender = (Player) victim;
            Map<CustomEnchant, Integer> defArmor = getArmorEnchants(defender);

            // ENDURECIDO — 0.3/0.6/0.9 flat reduction
            if (defArmor.containsKey(CustomEnchant.ENDURECIDO)) {
                int lvl = defArmor.get(CustomEnchant.ENDURECIDO);
                double reduction = lvl * 0.3;
                event.setDamage(Math.max(0, event.getDamage() - reduction));
            }

            // AURA — 4%/8% reflect (1 tick delay to avoid infinite loop)
            if (defArmor.containsKey(CustomEnchant.AURA)) {
                int lvl = defArmor.get(CustomEnchant.AURA);
                double[] pcts = { 0.04, 0.08 };
                double reflect = damage * pcts[Math.min(lvl - 1, 1)];
                Tasks.syncLater(() -> {
                    if (attacker.isOnline()) attacker.damage(reflect, defender);
                }, 1L);
            }

            // NINJA — invis permanente via tick, nada extra en hit
        }
    }

    // COMBO — Speed I (amplifier 0) por 3s tras matar
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack hand = killer.getInventory().getItemInMainHand();
        if (hand == null) return;
        if (CustomEnchantManager.getInstance().getLevel(hand, CustomEnchant.COMBO) <= 0) return;
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, COMBO_TICKS, 0, false, true));
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    private Map<CustomEnchant, Integer> getArmorEnchants(Player player) {
        Map<CustomEnchant, Integer> total = new EnumMap<>(CustomEnchant.class);
        EntityEquipment eq = player.getEquipment();
        if (eq == null) return total;
        for (ItemStack piece : new ItemStack[]{ eq.getHelmet(), eq.getChestplate(),
                                                eq.getLeggings(), eq.getBoots() }) {
            if (piece == null) continue;
            CustomEnchantManager.getInstance().getEnchants(piece)
                .forEach((ce, lvl) -> total.merge(ce, lvl, Integer::sum));
        }
        return total;
    }
}

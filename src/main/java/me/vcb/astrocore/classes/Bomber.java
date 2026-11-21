package me.vcb.astrocore.classes;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.classes.items.BomberTntGun;
import me.vcb.astrocore.classes.manager.PvpClass;
import me.vcb.astrocore.classes.manager.PvpClassManager;
import me.vcb.astrocore.classes.utils.PvpClassUtils;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.scoreboard.PlayerScoreboard;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.cooldown.CooldownTimer;
import me.vcb.astrocore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Bomber extends PvpClass implements Listener {

    public static final String COOLDOWN_KEY = "BOMBER_TNT_GUN";
    public static final String TNT_METADATA = "BOMBER_TNT";

    private final BomberTntGun tntGun;

    public Bomber(PvpClassManager manager) {
        super(manager, "Bomber",
            Material.GOLDEN_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.GOLDEN_BOOTS
        );

        this.warmup = Config.BOMBER_WARMUP;
        this.tntGun = PvpClassUtils.loadBomberTntGun();
    }

    public boolean isTntGun(ItemStack item) {
        return this.tntGun != null && this.tntGun.getItem().getType() == item.getType()
            && this.tntGun.getItem().getDurability() == item.getDurability();
    }

    private boolean canShootTnt(Player player) {
        if(AstroCore.getInstance().getStaffModeManager().isInStaffModeOrVanished(player)) {
            player.sendMessage(Language.PREFIX + Language.BOMBER_VANISHED_OR_IN_STAFFMODE);
            return false;
        }

        if(TimerManager.getInstance().getPvpProtTimer().isActive(player)) {
            player.sendMessage(Language.PREFIX + Language.BOMBER_CAN_NOT_SHOOT_TNT_WITH_PVP_TIMER);
            return false;
        }

        if(ClaimManager.getInstance().getFactionAt(player).isSafezone()) {
            player.sendMessage(Language.PREFIX + Language.BOMBER_CAN_NOT_SHOOT_TNT_IN_SAFEZONE);
            return false;
        }

        if(AstroCore.getInstance().getSotwHandler().isUnderSotwProtection(player)) {
            player.sendMessage(Language.PREFIX + Language.BOMBER_CAN_NOT_SHOOT_TNT_WHEN_SOTW_NOT_ENABLED);
            return false;
        }

        return true;
    }

    private void shootTntGun(Player player) {
        TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);

        tnt.setFuseTicks(this.tntGun.getFuseTicks());
        tnt.setVelocity(player.getLocation().getDirection().multiply(this.tntGun.getTntVelocity()));
        tnt.setMetadata(TNT_METADATA, PlayerUtils.TRUE_METADATA_VALUE);

        if(Config.BOMBER_COMBAT_TAG_ON_TNT_USE) {
            TimerManager.getInstance().getCombatTagTimer().activate(player.getUniqueId());
        }
    }

    @Override
    public void applyActiveScoreboardLines(Player player, PlayerScoreboard scoreboard) {
        super.applyActiveScoreboardLines(player, scoreboard);

        CooldownTimer timer = TimerManager.getInstance().getCooldownTimer();

        if(timer.isActive(player, COOLDOWN_KEY)) {
            scoreboard.add(Config.COOLDOWN_PLACEHOLDER , timer.getTimeLeft(player, COOLDOWN_KEY) + 's');
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY) return;

        if(!this.isActive(event.getPlayer()) || !event.hasItem()) return;
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if(!this.isTntGun(event.getItem()) || !this.canShootTnt(player)) return;

        CooldownTimer timer = TimerManager.getInstance().getCooldownTimer();

        if(timer.isActive(player, COOLDOWN_KEY)) {
            player.sendMessage(Language.PREFIX + Language.BOMBER_TNT_GUN_ACTIVE_COOLDOWN
                .replace("<seconds>", timer.getTimeLeft(player, COOLDOWN_KEY)));
            return;
        }

        this.shootTntGun(player);

        timer.activate(player, COOLDOWN_KEY, this.tntGun.getCooldown(),
            Language.PREFIX + Language.BOMBER_TNT_GUN_COOLDOWN_EXPIRED);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getDamager();
        if(!tnt.hasMetadata(TNT_METADATA)) return;

        Player player = (Player) event.getEntity();
        player.setMetadata(TNT_METADATA, PlayerUtils.TRUE_METADATA_VALUE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        if(!player.hasMetadata(TNT_METADATA)) return;

        Vector velocity = event.getVelocity().normalize();

        Vector newVelocity = velocity.setY(Math.min(Math.pow(velocity.getY()
            + this.tntGun.getKbYMultiplier(), 1.2), this.tntGun.getKbMaxY()));

        player.removeMetadata(TNT_METADATA, AstroCore.getInstance());
        player.setVelocity(newVelocity);

        event.setCancelled(true);
    }
}

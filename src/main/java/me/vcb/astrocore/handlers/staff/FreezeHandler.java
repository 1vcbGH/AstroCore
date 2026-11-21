package me.vcb.astrocore.handlers.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.event.freeze.FreezePlayerEvent;
import me.vcb.astrocore.handlers.event.freeze.FreezeType;
import me.vcb.astrocore.handlers.event.freeze.UnfreezePlayerEvent;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FreezeHandler extends Handler implements Listener {

    private static final String TIMER_FORMAT = "&c&l⏱ <time>";
    private static final String PLAYER_CHAT_FORMAT = "&b[Freeze] &7<player>&8: &f<message>";
    private static final String STAFF_CHAT_FORMAT = "&a[Staff] &7<player>&8: &f<message>";

    private final Map<UUID, FrozenTask> frozen = new HashMap<>();
    private final Map<UUID, UUID> frozenBy = new HashMap<>();
    private final Map<UUID, ArmorStand> timers = new HashMap<>();
    private final Map<UUID, BukkitTask> timerTasks = new HashMap<>();
    private boolean freezeAll;

    @Override
    public void disable() {
        this.frozen.values().stream().filter(task -> task != null).forEach(FrozenTask::cancel);
        new ArrayList<>(this.timers.keySet()).forEach(this::stopTimer);
        this.frozen.clear();
        this.frozenBy.clear();
        this.freezeAll = false;
    }

    public boolean isFrozen(Player player) {
        return this.frozen.containsKey(player.getUniqueId());
    }

    public void toggleFreeze(CommandSender sender, Player target) {
        if(this.isFrozen(target)) {
            this.unfreezePlayer(sender, target);
        } else {
            this.freezePlayer(sender, target);
        }
    }

    private void freezePlayer(CommandSender sender, Player target) {
        FreezePlayerEvent event = new FreezePlayerEvent(sender, target, FreezeType.PLAYER);
        if(event.isCancelled()) return;

        FrozenTask oldTask = this.frozen.put(target.getUniqueId(), new FrozenTask(target));
        if(oldTask != null) oldTask.cancel();
        this.frozenBy.put(target.getUniqueId(), sender instanceof Player ? ((Player) sender).getUniqueId() : null);

        target.setWalkSpeed(0F);
        target.setFoodLevel(0);
        target.setSprinting(false);
        target.setAllowFlight(false);
        target.setFlying(false);
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 6, false, false));

        Messages.sendMessage(Language.PREFIX + Language.FREEZE_FREEZED_STAFF_MESSAGE
            .replace("<target>", target.getName())
            .replace("<player>", sender.getName()), "astrocore.staff");
        this.startTimer(target);
    }

    private void startTimer(Player target) {
        UUID uuid = target.getUniqueId();
        this.stopTimer(uuid);
        long start = System.currentTimeMillis();

        Location location = target.getLocation().clone().add(0, 2.35, 0);
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setCustomNameVisible(true);
        this.timers.put(uuid, stand);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(AstroCore.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null || !player.isOnline() || stand.isDead() || !this.isFrozen(player)) {
                this.stopTimer(uuid);
                return;
            }

            stand.teleport(player.getLocation().add(0, 2.35, 0));
            long seconds = (System.currentTimeMillis() - start) / 1000;
            stand.setCustomName(Color.translate(TIMER_FORMAT.replace("<time>",
                String.format("%02d:%02d", seconds / 60, seconds % 60))));
        }, 0L, 5L);
        this.timerTasks.put(uuid, task);
    }

    private void stopTimer(UUID uuid) {
        BukkitTask task = this.timerTasks.remove(uuid);
        if(task != null) task.cancel();

        ArmorStand stand = this.timers.remove(uuid);
        if(stand != null && !stand.isDead()) stand.remove();
    }

    private void unfreezePlayer(CommandSender sender, Player target) {
        if(this.freezeAll) return;

        UnfreezePlayerEvent event = new UnfreezePlayerEvent(sender, target, FreezeType.PLAYER);
        if(event.isCancelled()) return;

        FrozenTask task = this.frozen.remove(target.getUniqueId());
        if(task != null) task.cancel();
        this.frozenBy.remove(target.getUniqueId());
        this.stopTimer(target.getUniqueId());
        this.restore(target);

        Messages.sendMessage(Language.PREFIX + Language.FREEZE_UNFREEZED_STAFF_MESSAGE
            .replace("<target>", target.getName())
            .replace("<player>", sender.getName()), "astrocore.staff");
        target.sendMessage(Language.PREFIX + Language.FREEZE_UNFREEZED_MESSAGE_TARGET
            .replace("<player>", sender.getName()));
    }

    private void restore(Player player) {
        player.setWalkSpeed(0.2F);
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    public void toggleFreezeAll(CommandSender sender) {
        if(this.freezeAll) {
            this.unfreezeAll(sender);
        } else {
            this.freezeAll(sender);
        }
    }

    private void freezeAllPlayer(Player player) {
        if(player.hasPermission("astrocore.staff")) return;

        FreezePlayerEvent event = new FreezePlayerEvent(Bukkit.getConsoleSender(), player, FreezeType.SERVER);
        if(event.isCancelled()) return;

        this.frozen.put(player.getUniqueId(), null);
        player.setWalkSpeed(0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        NmsUtils.getInstance().addPotionEffect(player,
            new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false));
    }

    private void freezeAll(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(this::freezeAllPlayer);
        this.freezeAll = true;
        Messages.sendMessage(Language.FREEZE_FREEZED_ALL.replace("<player>", sender.getName()));
    }

    private void unfreezeAll(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(!this.isFrozen(player)) return;

            UnfreezePlayerEvent event = new UnfreezePlayerEvent(Bukkit.getConsoleSender(), player, FreezeType.SERVER);
            if(event.isCancelled()) return;

            FrozenTask task = this.frozen.remove(player.getUniqueId());
            if(task != null) task.cancel();
            this.frozenBy.remove(player.getUniqueId());
            this.stopTimer(player.getUniqueId());
            this.restore(player);
        });

        this.freezeAll = false;
        Messages.sendMessage(Language.FREEZE_UNFREEZED_ALL.replace("<player>", sender.getName()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = PlayerUtils.getAttacker(event);
        if(attacker == null) return;

        if(this.isFrozen(attacker)) {
            event.setCancelled(true);
            attacker.sendMessage(Language.PREFIX + Language.FREEZE_PVP_DENY_MESSAGE_DAMAGER);
            return;
        }

        if(this.isFrozen(victim)) {
            event.setCancelled(true);
            attacker.sendMessage(Language.PREFIX + Language.FREEZE_PVP_DENY_MESSAGE_VICTIM
                .replace("<player>", victim.getName()));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(this.isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && this.isFrozen((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(!this.isFrozen(event.getPlayer())) return;

        String message = event.getMessage().toLowerCase();
        if(Config.FREEZE_DISABLED_COMMANDS.stream()
            .noneMatch(command -> message.startsWith(command.toLowerCase()))) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Language.PREFIX + Language.FREEZE_DISABLED_COMMAND_MESSAGE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(this.isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(this.isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player && this.isFrozen((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent event) {
        if(this.isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(this.isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(this.freezeAll) this.freezeAllPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!this.isFrozen(player)) return;

        this.restore(player);
        FrozenTask task = this.frozen.remove(player.getUniqueId());
        if(task != null) task.cancel();
        this.frozenBy.remove(player.getUniqueId());
        this.stopTimer(player.getUniqueId());

        Messages.sendMessage(Language.PREFIX + Language.FREEZE_QUIT_WHEN_FROZEN
            .replace("<player>", player.getName()), "astrocore.staff");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderId = sender.getUniqueId();

        if(this.isFrozen(sender)) {
            event.setCancelled(true);
            Set<Player> recipients = new HashSet<>();
            recipients.add(sender);

            UUID staffUuid = this.frozenBy.get(senderId);
            if(staffUuid != null) {
                Player staff = Bukkit.getPlayer(staffUuid);
                if(staff != null) recipients.add(staff);
            } else {
                Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("astrocore.staff"))
                    .forEach(recipients::add);
            }

            this.sendTo(recipients, PLAYER_CHAT_FORMAT, sender.getName(), event.getMessage());
            return;
        }

        Set<Player> frozenTargets = this.frozenTargetsOf(senderId);
        if(frozenTargets.isEmpty()) return;

        event.setCancelled(true);
        Set<Player> recipients = new HashSet<>(frozenTargets);
        recipients.add(sender);
        this.sendTo(recipients, STAFF_CHAT_FORMAT, sender.getName(), event.getMessage());
    }

    private void sendTo(Set<Player> recipients, String format, String name, String message) {
        String formatted = Color.translate(format.replace("<player>", name).replace("<message>", message));
        recipients.forEach(player -> player.sendMessage(formatted));
    }

    private Set<Player> frozenTargetsOf(UUID staffId) {
        Set<Player> targets = new HashSet<>();
        this.frozenBy.forEach((targetId, freezerId) -> {
            if(!staffId.equals(freezerId)) return;
            Player target = Bukkit.getPlayer(targetId);
            if(target != null && this.isFrozen(target)) targets.add(target);
        });
        return targets;
    }

    private static final class FrozenTask extends BukkitRunnable {

        private final UUID uuid;

        private FrozenTask(Player player) {
            this.uuid = player.getUniqueId();
            this.runTaskTimerAsynchronously(AstroCore.getInstance(), 0L,
                Config.FREEZE_MESSAGE_INTERVAL * 20L);
        }

        @Override
        public void run() {
            Player player = Bukkit.getPlayer(this.uuid);
            if(player != null) Language.FROZEN_MESSAGE.forEach(player::sendMessage);
        }
    }
}

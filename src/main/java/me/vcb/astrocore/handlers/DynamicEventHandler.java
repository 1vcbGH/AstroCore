package me.vcb.astrocore.handlers;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.ConquestFaction;
import me.vcb.astrocore.factions.type.KothFaction;
import me.vcb.astrocore.handlers.chat.ChatHandler;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.integration.spigot.DefaultSpigotListener;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.timer.cooldown.CooldownTimer;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.userdata.UserdataManager;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.PlayerUtils;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.item.ItemUtils;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DynamicEventHandler extends Handler implements Listener {

    public DynamicEventHandler() {
        Bukkit.getWorlds().forEach(world -> world.setWeatherDuration(0));

        new DefaultSpigotListener();

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if(!Config.REDUCED_DURABILITY_LOSS_ENABLED) return;
        if(!Config.REDUCED_DURABILITY_LOSS_MATERIALS.contains(event.getItem().getType())) return;
        if(ThreadLocalRandom.current().nextInt(100) > Config.REDUCED_DURABILITY_LOSS_PERCENTAGE) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if(Config.REMOVE_EMPTY_BOTTLE_ON_POTION_USE && event.getItem().getType() == Material.POTION) {
            // In 1.21, drinkable potions are Material.POTION; splash/lingering are separate materials.
            // getDurability() always returns 0 in 1.21, so we remove the bottle for all POTION items.
            Tasks.sync(() -> ItemUtils.removeOneItem(event.getPlayer()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractGlitch(PlayerInteractEvent event) {
        if(!event.hasItem() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Material blockType = event.getClickedBlock().getType();
        if(blockType != Material.OAK_FENCE && blockType != Material.NETHER_BRICK_FENCE && blockType != Material.CAULDRON) return;

        ItemStack item = event.getItem();

        if(item.getType() != Material.POTION && item.getType() != Material.BOW
        && !item.getType().isEdible() && !item.getType().name().endsWith("SWORD")) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(event.isCancelled() && !player.getAllowFlight()) {
            if(player.getLocation().getBlockY() <= block.getLocation().getBlockY()) return;

            Faction factionAt = ClaimManager.getInstance().getFactionAt(block);
            if(factionAt instanceof KothFaction || factionAt instanceof ConquestFaction) return;

            player.setVelocity(new Vector(0, -0.5, 0));
            return;
        }

        if(!event.isCancelled() && block.getType() == Material.SPAWNER) {
            switch(block.getWorld().getEnvironment()) {
                case NETHER: {
                    if(!Config.DENY_SPAWNER_PLACE_IN_NETHER || player.isOp()) return;

                    event.setCancelled(true);
                    player.sendMessage(Language.PREFIX + Language.SPAWNERS_DISABLE_PLACE_NETHER);
                    return;
                }
                case THE_END: {
                    if(!Config.DENY_SPAWNER_PLACE_IN_END || player.isOp()) return;

                    event.setCancelled(true);
                    player.sendMessage(Language.PREFIX + Language.SPAWNERS_DISABLE_PLACE_END);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if(item == null || item.getType() != Material.SPAWNER) return;

        CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();

        BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
        if(blockStateMeta == null) return;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

        spawner.setSpawnedType(creatureSpawner.getSpawnedType());
        spawner.update();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();

        switch(event.getBlock().getWorld().getEnvironment()) {
            case NETHER: {
                if(!Config.DENY_SPAWNER_BREAK_IN_NETHER || player.isOp()) return;

                event.setCancelled(true);
                player.sendMessage(Language.PREFIX + Language.SPAWNERS_DISABLE_BREAK_NETHER);
                break;
            }
            case THE_END: {
                if(!Config.DENY_SPAWNER_BREAK_IN_END || player.isOp()) return;

                event.setCancelled(true);
                player.sendMessage(Language.PREFIX + Language.SPAWNERS_DISABLE_BREAK_END);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedBombing(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !Config.DISABLE_BED_BOMBING) return;
        if(event.getClickedBlock().getType() != Material.RED_BED || event.getPlayer().isOp()) return;
        if(event.getClickedBlock().getWorld().getEnvironment() == Environment.NORMAL) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if(event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHungerChange(FoodLevelChangeEvent event) {
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        player.setSaturation(1000.0f);
        player.setSaturation(10.0f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if(!(event.getEntity() instanceof Creeper)) return;
        if(!Config.DISABLE_CREEPER_PLAYER_TARGETING) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == TeleportCause.ENDER_PEARL) return;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(event.getPlayer());

        if(data != null) {
            data.setLastLocation(event.getPlayer().getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(Config.REMOVE_STRENGTH_ON_END_ENTER && event.getTo().getWorld().getEnvironment() == Environment.THE_END) {
            event.getPlayer().removePotionEffect(PotionEffectType.STRENGTH);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(Config.DISABLE_EXPLOSIONS_BLOCK_DAMAGE) {
            event.blockList().clear();
        }

        if(ClaimManager.getInstance().getFactionAt(event.getLocation()).isSafezone()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if(!event.getPlayer().hasPermission("astrocore.sign.color")) return;
        IntStream.range(0, 4).forEach(i -> event.setLine(i, Color.translate(event.getLine(i))));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UserdataManager manager = AstroCore.getInstance().getUserdataManager();

        Player player = event.getEntity();
        Userdata userdata = manager.getUserdata(player);

        if(userdata != null) {
            userdata.setLastLocation(player.getLocation());
            userdata.updateDeathStats(event.getDeathMessage());
        }

        Player killer = player.getKiller();

        if(killer != null && player != killer && killer.isOnline()) {
            Userdata killerData = manager.getUserdata(killer);
            if(killerData != null) killerData.updateKillStats(event.getDeathMessage());
        }

        if(Config.LIGHTNING_EFFECT_ON_DEATH) {
            NmsUtils.getInstance().strikeLightningEffect(player, player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.setFireTicks(0);

        Location spawn = Config.WORLD_SPAWNS.get(Environment.NORMAL);
        event.setRespawnLocation(spawn == null ? player.getWorld().getSpawnLocation() : spawn);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(Config.COMMANDS_COOLDOWN == 0) return;
        if(event.getPlayer().hasPermission("astrocore.command.cooldown.bypass")) return;

        CooldownTimer timer = TimerManager.getInstance().getCooldownTimer();

        if(timer.isActive(event.getPlayer(), "COMMAND")) {
            event.getPlayer().sendMessage(Language.COMMANDS_COOLDOWN);
            event.setCancelled(true);
            return;
        }

        timer.activate(event.getPlayer(), "COMMAND", Config.COMMANDS_COOLDOWN, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAstroCoreKick(AstroCoreKickEvent event) {
        boolean bungeecord = Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false);
        if(!bungeecord || !Config.SEND_TO_HUB_ON_KICK_ENABLED) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(event.getKickMessage());

        int randomHub = ThreadLocalRandom.current().nextInt(Config.SEND_TO_HUB_ON_KICK_HUBS.size());

        String serverName = Config.SEND_TO_HUB_ON_KICK_HUBS.get(randomHub);
        PlayerUtils.sendToServer(event.getPlayer(), serverName);
    }

    @EventHandler
    public void onFullServerJoin(PlayerLoginEvent event) {
        if(event.getResult() != PlayerLoginEvent.Result.KICK_FULL) return;

        if(!event.getPlayer().hasPermission("astrocore.joinfullserver")) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, Language.JOIN_FULL_SERVER_MESSAGE);
            return;
        }

        event.allow();
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if(Bukkit.getPlayerExact(event.getName()) != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Language.PLAYER_ALREADY_ONLINE);
        }
    }

    @EventHandler
    public void onVersionCheck(PlayerJoinEvent event) {
        if(!Config.VERSION_WHITELIST_ENABLED || Config.VERSION_WHITELIST_ALLOWED.isEmpty()) return;

        Player player = event.getPlayer();
        int version = NmsUtils.getInstance().getClientVersion(player);

        if(!Config.VERSION_WHITELIST_ALLOWED.contains(version)) {
            Bukkit.getScheduler().runTask(AstroCore.getInstance(), () ->
                player.kickPlayer(Color.translate(Config.VERSION_WHITELIST_KICK_MESSAGE)));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        String rankPrefix = Color.translate(ChatHandler.getInstance().getPrefix(player));

        if(Config.JOIN_WELCOME_MESSAGE_ENABLED) {
            Language.JOIN_WELCOME_MESSAGE.forEach(line -> player.sendMessage(line
                .replace("<player>", player.getName())
                .replace("<rankPrefix>", rankPrefix)));
        }

        if(Language.STAFF_JOIN_MESSAGE.isEmpty()) return;
        if(!player.hasPermission("astrocore.staff") || (Config.VANISH_ON_JOIN_ENABLED && player.hasPermission("astrocore.vanish.onjoin"))) return;

        Messages.sendMessage(Language.STAFF_JOIN_MESSAGE.replace("<player>", player.getName()), "astrocore.staff");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        if(Language.STAFF_QUIT_MESSAGE.isEmpty()) return;

        Player player = event.getPlayer();
        if(!event.getPlayer().hasPermission("astrocore.staff")) return;

        Messages.sendMessage(Language.STAFF_QUIT_MESSAGE.replace("<player>", player.getName()), "astrocore.staff");
    }
}

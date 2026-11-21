package me.vcb.astrocore.events.hunterprey;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HunterPreyManager implements Listener {

    @Getter private static HunterPreyManager instance;

    // Coins rewards
    private static final int KILLER_REWARD  = 2000;
    private static final int PREY_REWARD    = 5000;
    private static final int PREY_ELO_BONUS = 200;

    // Commands the prey cannot use
    private static final Set<String> BLOCKED_COMMANDS = new HashSet<>(Arrays.asList(
        "spawn", "home", "homes", "warp", "warps", "tp", "tpa", "tpaccept",
        "back", "rtp", "randomteleport", "exit", "hub", "lobby",
        "logout", "stuck", "safe"
    ));

    @Getter private boolean active;
    @Getter private UUID preyUuid;
    @Getter private String preyName;

    private int timeRemaining;
    private BossBar bossBar;
    private BukkitTask timerTask;
    private BukkitTask coordsTask;

    public HunterPreyManager() {
        instance = this;
    }

    public void disable() {
        if(this.active) this.end(false, null);
        instance = null;
    }

    // -------------------------------------------------------------------------
    // Start / End
    // -------------------------------------------------------------------------

    public boolean start(int seconds) {
        if(this.active) return false;

        List<Player> online = List.copyOf(Bukkit.getOnlinePlayers());
        if(online.isEmpty()) return false;

        Player prey = online.get((int) (Math.random() * online.size()));
        return this.startWithPlayer(prey, seconds);
    }

    public boolean startWithPlayer(Player prey, int seconds) {
        if(this.active) return false;

        this.active       = true;
        this.preyUuid     = prey.getUniqueId();
        this.preyName     = prey.getName();
        this.timeRemaining = seconds;

        // Boss bar — updated every second with coords
        this.bossBar = Bukkit.createBossBar(
            buildBarTitle(prey),
            BarColor.RED, BarStyle.SOLID
        );
        for(Player p : Bukkit.getOnlinePlayers()) this.bossBar.addPlayer(p);

        // Announce
        this.broadcastStart(prey, seconds);

        // Prey personal message
        prey.sendMessage("§c§l[Cazador y Presa] §r§7¡Fuiste elegido como la §c§lPRESA§r§7!");
        prey.sendMessage("§7Sobrevivé §e" + formatTime(seconds) + " §7para ganar §e" + PREY_REWARD + " coins§7.");
        prey.sendMessage("§c¡No podés usar comandos de teletransporte!");
        prey.playSound(prey.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1f);

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());

        // Coords update task — every second
        this.coordsTask = Tasks.syncTimer(() -> {
            Player p = Bukkit.getPlayer(this.preyUuid);
            if(p != null && this.bossBar != null) {
                this.bossBar.setTitle(buildBarTitle(p));
            }
        }, 0L, 20L);

        // Countdown task
        this.timerTask = Tasks.syncTimer(() -> {
            this.timeRemaining--;

            // Warn prey at certain intervals
            Player p = Bukkit.getPlayer(this.preyUuid);
            if(p != null && (this.timeRemaining == 60 || this.timeRemaining == 30 || this.timeRemaining == 10)) {
                p.sendMessage("§e§l[Presa] §r§7Te quedan §e" + formatTime(this.timeRemaining) + " §7para ganar. ¡Aguantá!");
            }

            if(this.timeRemaining <= 0) {
                this.preyWins();
            }
        }, 20L, 20L);

        return true;
    }

    public void end(boolean announce, String reason) {
        if(!this.active) return;
        this.active = false;

        if(this.timerTask  != null) { this.timerTask.cancel();  this.timerTask  = null; }
        if(this.coordsTask != null) { this.coordsTask.cancel(); this.coordsTask = null; }
        if(this.bossBar    != null) { this.bossBar.removeAll(); this.bossBar    = null; }

        HandlerList.unregisterAll(this);

        if(announce && reason != null) Bukkit.broadcastMessage(reason);
    }

    // -------------------------------------------------------------------------
    // Outcomes
    // -------------------------------------------------------------------------

    private void preyWins() {
        Player prey = Bukkit.getPlayer(this.preyUuid);
        String name = prey != null ? prey.getName() : this.preyName;

        String[] lines = {
            "§8§m                                                  ",
            "   §a§l¡LA PRESA SOBREVIVIÓ!",
            "   §7§l" + name + " §r§7sobrevivió a todos los cazadores.",
            "   §7Recompensa: §e" + PREY_REWARD + " coins §8+ §e" + PREY_ELO_BONUS + " ELO",
            "§8§m                                                  "
        };

        this.end(false, null);
        for(String l : lines) Bukkit.broadcastMessage(l);

        if(prey != null) {
            AstroCore.getInstance().getEconomyManager().addBalance(prey, PREY_REWARD);
            me.vcb.astrocore.userdata.Userdata ud =
                AstroCore.getInstance().getUserdataManager().getUserdata(prey);
            if(ud != null) {
                ud.addElo(PREY_ELO_BONUS);
                me.vcb.astrocore.rank.RankManager.getInstance().checkRankUp(prey, ud);
            }
            prey.sendMessage("§a+§e" + PREY_REWARD + " coins §7y §a+§e" + PREY_ELO_BONUS + " ELO §7por sobrevivir.");
            prey.playSound(prey.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }

    private void killerWins(Player killer) {
        String preyName = this.preyName;
        String killerName = killer.getName();

        String[] lines = {
            "§8§m                                                  ",
            "   §c§l¡PRESA ELIMINADA!",
            "   §7§l" + killerName + " §r§7eliminó a la presa §c§l" + preyName + "§r§7.",
            "   §7Recompensa: §e" + KILLER_REWARD + " coins",
            "§8§m                                                  "
        };

        this.end(false, null);
        for(String l : lines) Bukkit.broadcastMessage(l);

        AstroCore.getInstance().getEconomyManager().addBalance(killer, KILLER_REWARD);
        killer.sendMessage("§a+§e" + KILLER_REWARD + " coins §7por eliminar a la presa.");
        killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreyDeath(PlayerDeathEvent event) {
        if(!this.active) return;
        if(!event.getEntity().getUniqueId().equals(this.preyUuid)) return;

        Player killer = event.getEntity().getKiller();
        if(killer != null) {
            this.killerWins(killer);
        } else {
            // Died to environment — prey is out but no killer reward
            this.end(true,
                "§8§m                                                  \n" +
                "   §7La presa §c" + this.preyName + " §7murió sin ser cazada. Nadie gana.\n" +
                "§8§m                                                  "
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreyQuit(PlayerQuitEvent event) {
        if(!this.active) return;
        if(!event.getPlayer().getUniqueId().equals(this.preyUuid)) return;

        this.end(true,
            "§8§m                                                  \n" +
            "   §7La presa §c" + this.preyName + " §7escapó del servidor. Evento cancelado.\n" +
            "§8§m                                                  "
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(this.active && this.bossBar != null) {
            this.bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreyCommand(PlayerCommandPreprocessEvent event) {
        if(!this.active) return;
        if(!event.getPlayer().getUniqueId().equals(this.preyUuid)) return;

        String cmd = event.getMessage().substring(1).split(" ")[0].toLowerCase();
        // Strip plugin prefix (e.g. "astrocore:spawn" → "spawn")
        if(cmd.contains(":")) cmd = cmd.split(":")[1];

        if(BLOCKED_COMMANDS.contains(cmd)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c§l[Presa] §r§cNo podés usar §f/" + cmd + " §cmientras sos la presa.");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String buildBarTitle(Player prey) {
        int x = prey.getLocation().getBlockX();
        int y = prey.getLocation().getBlockY();
        int z = prey.getLocation().getBlockZ();
        return "§c§l🎯 PRESA: §r§f" + prey.getName()
            + "  §8|  §7X: §f" + x + "  §7Y: §f" + y + "  §7Z: §f" + z;
    }

    private void broadcastStart(Player prey, int seconds) {
        String[] lines = {
            "§c§m                                                  ",
            "   §c§l🎯 CAZADOR Y PRESA",
            " ",
            "   §7La presa es: §c§l" + prey.getName(),
            "   §7Matala para ganar §e" + KILLER_REWARD + " coins§7.",
            "   §7Si sobrevive §e" + formatTime(seconds) + "§7, ella gana §e" + PREY_REWARD + " coins + " + PREY_ELO_BONUS + " ELO§7.",
            "   §7Sus coordenadas aparecen en la barra roja.",
            " ",
            "§c§m                                                  "
        };
        for(String line : lines) Bukkit.broadcastMessage(line);

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(!p.getUniqueId().equals(prey.getUniqueId())) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.2f);
            }
        }
    }

    public static String formatTime(int seconds) {
        if(seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        if(seconds >= 60)   return (seconds / 60) + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}

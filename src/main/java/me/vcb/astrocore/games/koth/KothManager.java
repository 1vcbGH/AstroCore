package me.vcb.astrocore.games.koth;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.SystemFaction;
import me.vcb.astrocore.factions.type.SystemType;
import me.vcb.astrocore.games.Cuboid;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.koth.event.KothStartEvent;
import me.vcb.astrocore.games.koth.event.KothStopEvent;
import me.vcb.astrocore.games.koth.listener.KothEventListeners;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.GsonUtils;
import me.vcb.astrocore.utils.ManagerEnabler;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class KothManager implements Listener, ManagerEnabler {

    private final File kothsFile;

    private List<KothData> koths;
    // CopyOnWriteArrayList: safe to iterate from async tick while main thread adds/removes
    private final List<RunningKoth> runningKoths;

    private BukkitTask tickTask;

    public KothManager() {
        this.kothsFile = FileUtils.getOrCreateFile(Config.GAMES_DIR, "koths.json");

        this.loadKoths();
        this.runningKoths = new CopyOnWriteArrayList<>();

        new KothEventListeners();
    }

    public void disable() {
        this.saveKoths();

        this.koths.clear();
        this.runningKoths.clear();

        if(this.tickTask != null) {
            this.cancelTickTask();
        }
    }

    private void loadKoths() {
        String content = FileUtils.readWholeFile(this.kothsFile);

        if(content == null) {
            this.koths = new ArrayList<>();
            return;
        }

        this.koths = AstroCore.getInstance().getGson().fromJson(content, GsonUtils.KOTH_TYPE);

        Iterator<KothData> iterator = this.koths.iterator();

        while(iterator.hasNext()) {
            KothData koth = iterator.next();

            if(koth.getFaction() == null) {
                iterator.remove();
                continue;
            }

            koth.setupKothColor();

            if(koth.getFactionPoints() == 0) {
                koth.setFactionPoints(Config.FACTION_TOP_KOTH_CAP);
            }
        }

        FactionsManager.getInstance().removeKothFactionsWithoutKoths(this.koths);
    }

    private void saveKoths() {
        if(this.koths == null) return;

        FileUtils.writeString(this.kothsFile, AstroCore.getInstance().getGson()
            .toJson(this.koths, GsonUtils.KOTH_TYPE));
    }

    public KothData getKoth(String name) {
        return this.koths.stream().filter(koth ->
            koth.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<KothData> getKoths() { return this.koths; }

    public List<RunningKoth> getRunningKoths() { return this.runningKoths; }

    public RunningKoth getRunningKoth(String name) {
        return this.runningKoths.stream().filter(koth ->
            koth.getKothData().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public RunningKoth getFirstRunningKoth() {
        return this.runningKoths.isEmpty() ? null : this.runningKoths.get(0);
    }

    public void createKoth(CommandSender sender, String name, int captime, Cuboid capzone) {
        SystemFaction faction = FactionsManager.getInstance().createSystemFaction(name, SystemType.KOTH, sender);
        LootData loot = AstroCore.getInstance().getLootManager().createLoot(name);

        int factionPoints = Config.FACTION_TOP_KOTH_CAP;
        this.koths.add(new KothData(name, faction.getId(), captime, factionPoints, capzone, faction.getColor(), loot));
    }

    public void deleteKoth(CommandSender sender, KothData koth) {
        FactionsManager.getInstance().disbandFaction(koth.getFactionId(), sender);

        AstroCore.getInstance().getLootManager().removeLoot(koth.getName());
        AstroCore.getInstance().getScheduleManager().removeScheduleByName(koth.getName());

        this.koths.remove(koth);

        RunningKoth runningKoth = this.getRunningKoth(koth.getName());
        if(runningKoth == null) return;

        this.stopKoth(runningKoth);
    }

    public void startKoth(KothData koth) {
        this.startKoth(koth, koth.getCaptime());
    }

    public void startKoth(KothData koth, int time) {
        if(this.tickTask == null) {
            Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
            this.startTickTask();
        }

        // Restore transient fields that are null after a server restart
        if(koth.getLoot() == null) {
            me.vcb.astrocore.games.loot.LootData loot =
                AstroCore.getInstance().getLootManager().getLootByName(koth.getName());
            if(loot == null) loot = AstroCore.getInstance().getLootManager().createLoot(koth.getName());
            koth.setLoot(loot);
        }
        if(koth.getColor() == null) {
            koth.setupKothColor();
        }

        RunningKoth runningKoth = new RunningKoth(koth, time);
        this.runningKoths.add(runningKoth);

        new KothStartEvent(runningKoth);

        Language.KOTH_START_STARTED.forEach(line -> Messages.sendMessage(Placeholder
            .RunningKothReplacer.parse(runningKoth, line)));
    }

    public void stopKoth(RunningKoth runningKoth) {
        this.runningKoths.remove(runningKoth);
        new KothStopEvent(runningKoth.getKothData());

        if(this.runningKoths.isEmpty()) {
            HandlerList.unregisterAll(this);
            this.cancelTickTask();
        }
    }

    public void stopAllKoths() {
        this.runningKoths.clear();

        HandlerList.unregisterAll(this);
        this.cancelTickTask();
    }

    private void startTickTask() {
        this.tickTask = Tasks.syncTimer(() -> {
            for(RunningKoth runningKoth : this.runningKoths) {
                // Fallback scan: detect capzone entry/exit every tick regardless of PlayerMoveEvent
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.isDead()) continue;
                    boolean inCapzone = runningKoth.getCapzone().getCuboid().containsIgnoreY(player.getLocation());
                    boolean tracked = runningKoth.getCapzone().getPlayers().contains(player.getName());
                    if(inCapzone && !tracked) {
                        runningKoth.onPlayerEnterCapzone(player);
                    } else if(!inCapzone && tracked) {
                        runningKoth.onPlayerLeaveCapzone(player);
                    }
                }
                runningKoth.tick();
            }
        }, 0L, 20L);
    }

    public boolean isMaxRunningKothsReached() {
        return this.runningKoths.size() >= Config.KOTH_MAX_RUNNING_KOTHS_AT_THE_SAME_TIME;
    }

    private void cancelTickTask() {
        if(this.tickTask == null) return;

        this.tickTask.cancel();
        this.tickTask = null;
    }

    public void listKoths(CommandSender sender) {
        if(this.koths.isEmpty()) {
            sender.sendMessage(Language.KOTH_PREFIX + Language.KOTH_LIST_NO_KOTHS);
            return;
        }

        sender.sendMessage(Language.KOTH_COMMAND_HEADER);
        sender.sendMessage(Language.KOTH_LIST_COMMAND_TITLE);

        this.koths.stream().sorted(Comparator.comparing(KothData::getName)).forEach(koth -> sender
            .sendMessage(Placeholder.KothReplacer.parse(koth, Language.KOTH_LIST_COMMAND_FORMAT)));

        sender.sendMessage(Language.KOTH_COMMAND_FOOTER);
    }

    private void checkCapzone(Player player, Location from, Location to) {
        if(player.isDead()) return;

        this.runningKoths.forEach(runningKoth -> {
            boolean fromCapzone = runningKoth.getKothData().getCuboid().containsIgnoreY(from);
            boolean toCapzone = runningKoth.getKothData().getCuboid().containsIgnoreY(to);

            if(!fromCapzone && toCapzone) {
                runningKoth.onPlayerEnterCapzone(player);
            } else if(fromCapzone && !toCapzone) {
                runningKoth.onPlayerLeaveCapzone(player);
            }
        });
    }

    public void togglePlayerCapzone(Player player, boolean join) {
        if(this.runningKoths.isEmpty()) return;

        this.runningKoths.forEach(koth -> {
            if(!koth.getCapzone().getCuboid().containsIgnoreY(player.getLocation())) return;

            if(join && !player.isDead()) {
                koth.onPlayerEnterCapzone(player);
            } else {
                koth.onPlayerLeaveCapzone(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if(from.getBlockX() == to.getBlockX()
            && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ()) return;

        this.checkCapzone(event.getPlayer(), from, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.checkCapzone(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.togglePlayerCapzone(event.getEntity(), false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.togglePlayerCapzone(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.togglePlayerCapzone(event.getPlayer(), false);
    }
}

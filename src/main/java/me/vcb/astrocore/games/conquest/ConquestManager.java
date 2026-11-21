package me.vcb.astrocore.games.conquest;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.event.FactionCreateEvent;
import me.vcb.astrocore.factions.event.FactionDisbandEvent;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.conquest.event.ConquestStartEvent;
import me.vcb.astrocore.games.conquest.event.ConquestStopEvent;
import me.vcb.astrocore.games.conquest.listener.ConquestEventListener;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.utils.FileUtils;
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

import java.io.File;
import java.util.Objects;

public class ConquestManager implements Listener, ManagerEnabler {

    private final File conquestFile;

    @Getter private ConquestData conquest;
    @Getter private RunningConquest runningConquest;

    public ConquestManager() {
        this.conquestFile = FileUtils.getOrCreateFile(Config.GAMES_DIR, "conquest.json");

        this.loadConquest();
        new ConquestEventListener();
    }

    public void disable() {
        this.saveConquest();

        if(this.isActive()) {
            this.stopConquest(true);
        }
    }

    private void loadConquest() {
        String content = FileUtils.readWholeFile(this.conquestFile);

        if(content == null) {
            this.conquest = new ConquestData();
            return;
        }

        this.conquest = AstroCore.getInstance().getGson().fromJson(content, ConquestData.class);
        this.conquest.getFaction().setDeathban(true);
    }

    private void saveConquest() {
        if(this.conquest == null) return;

        FileUtils.writeString(this.conquestFile, AstroCore.getInstance().getGson()
            .toJson(this.conquest, ConquestData.class));
    }

    public boolean isActive() {
        return this.runningConquest != null;
    }

    public void startConquest(CommandSender sender) {
        if(this.conquest.getCuboids().values().stream().anyMatch(Objects::isNull)) {
            sender.sendMessage(Language.CONQUEST_PREFIX + Language.CONQUEST_START_NOT_SETUP);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
        this.conquest.getFaction().setDeathban(false);

        LootData loot = AstroCore.getInstance().getLootManager().getLootByName("Conquest");
        this.runningConquest = new RunningConquest(this.conquest, loot, Config.CONQUEST_CAP_TIME);

        new ConquestStartEvent(this.runningConquest);

        Language.CONQUEST_START_STARTED.forEach(line -> Messages.sendMessage(
            Placeholder.ConquestReplacer.parse(this.conquest, line)));
    }

    public void stopConquest(boolean serverStop) {
        if(!serverStop) {
            this.conquest.getFaction().setDeathban(true);
        }

        HandlerList.unregisterAll(this);

        this.runningConquest.cancel();
        this.runningConquest = null;

        new ConquestStopEvent();
    }

    private void checkCapzone(Player player, Location from, Location to) {
        if(player.isDead()) return;

        this.conquest.getCuboids().forEach((type, capzone) -> {
            boolean fromCapzone = capzone.contains(from);
            boolean toCapzone = capzone.contains(to);

            if(!fromCapzone && toCapzone) {
                this.runningConquest.onPlayerEnterCapzone(player, type);
            } else if(fromCapzone && !toCapzone) {
                this.runningConquest.onPlayerLeaveCapzone(player, type);
            }
        });
    }

    public void togglePlayerCapzone(Player player, boolean join) {
        if(!this.isActive()) return;

        this.conquest.getCuboids().forEach((type, capzone) -> {
            if(!capzone.contains(player.getLocation())) return;

            if(join && !player.isDead()) {
                this.runningConquest.onPlayerEnterCapzone(player, type);
            } else {
                this.runningConquest.onPlayerLeaveCapzone(player, type);
            }
        });
    }

    public void onDeath(Player player, PlayerFaction faction) {
        if(!this.runningConquest.getFactionPoints().containsKey(faction)) return;

        this.runningConquest.removePoints(faction, Config.CONQUEST_DEATH_PENALTY);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to
        .getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        this.checkCapzone(event.getPlayer(), from, to);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.checkCapzone(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionCreate(FactionCreateEvent event) {
        Tasks.async(() -> {
            PlayerFaction faction = FactionsManager.getInstance().getPlayerFactionByName(event.getFactionName());
            if(faction == null) return;

            Player leader = faction.getLeader().getPlayer();
            if(leader == null) return;

            this.togglePlayerCapzone(leader, true);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onFactionDisband(FactionDisbandEvent event) {
        if(!(event.getFaction() instanceof PlayerFaction)) return;

        PlayerFaction faction = (PlayerFaction) event.getFaction();
        this.runningConquest.getFactionPoints().remove(faction);

        faction.getOnlinePlayers().forEach(online -> this.togglePlayerCapzone(online, false));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.togglePlayerCapzone(event.getEntity(), false);

        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(event.getEntity());

        if(faction != null) {
            this.onDeath(event.getEntity(), faction);
        }
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

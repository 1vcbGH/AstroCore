package me.vcb.astrocore.handlers.manager;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.handlers.BorderHandler;
import me.vcb.astrocore.handlers.BottleHandler;
import me.vcb.astrocore.handlers.CustomRecipeHandler;
import me.vcb.astrocore.handlers.DisenchantHandler;
import me.vcb.astrocore.handlers.DynamicEventHandler;
import me.vcb.astrocore.handlers.ExpAmplifierHandler;
import me.vcb.astrocore.handlers.GlisteringMelonHandler;
import me.vcb.astrocore.handlers.InventoryHandler;
import me.vcb.astrocore.handlers.MapkitHandler;
import me.vcb.astrocore.handlers.MobStackHandler;
import me.vcb.astrocore.handlers.SpawnTeleportHandler;
import me.vcb.astrocore.handlers.StatsHandler;
import me.vcb.astrocore.handlers.block.AutoSmeltHandler;
import me.vcb.astrocore.handlers.block.CobwebRemoveHandler;
import me.vcb.astrocore.handlers.block.CrowbarHandler;
import me.vcb.astrocore.handlers.block.DisabledBlocksHandler;
import me.vcb.astrocore.handlers.block.FilterHandler;
import me.vcb.astrocore.handlers.block.FoundOreHandler;
import me.vcb.astrocore.handlers.block.SubclaimHandler;
import me.vcb.astrocore.handlers.chat.AutoModHandler;
import me.vcb.astrocore.handlers.chat.BlockedCommandsHandler;
import me.vcb.astrocore.handlers.chat.BroadcastSchedulerHandler;
import me.vcb.astrocore.handlers.chat.ChatControlHandler;
import me.vcb.astrocore.handlers.chat.ChatHandler;
import me.vcb.astrocore.handlers.chat.MessagingHandler;
import me.vcb.astrocore.handlers.death.DeathMessageHandler;
import me.vcb.astrocore.handlers.death.DeathRecapHandler;
import me.vcb.astrocore.handlers.death.DeathSignHandler;
import me.vcb.astrocore.handlers.death.HeadDropHandler;
import me.vcb.astrocore.handlers.death.StattrakHandler;
import me.vcb.astrocore.handlers.elevator.MinecartElevatorHandler;
import me.vcb.astrocore.handlers.elevator.SignElevatorHandler;
import me.vcb.astrocore.handlers.kitmap.KillstreakHandler;
import me.vcb.astrocore.handlers.kitmap.KitmapHandler;
import me.vcb.astrocore.handlers.leaderboard.LeaderboardHandler;
import me.vcb.astrocore.handlers.limiter.EnchantLimiterHandler;
import me.vcb.astrocore.handlers.limiter.EntityLimiterHandler;
import me.vcb.astrocore.handlers.limiter.PotionLimiterHandler;
import me.vcb.astrocore.handlers.logger.CombatLoggerHandler;
import me.vcb.astrocore.handlers.portal.EndPortalHandler;
import me.vcb.astrocore.handlers.portal.NetherPortalTrapHandler;
import me.vcb.astrocore.handlers.portal.PortalHandler;
import me.vcb.astrocore.handlers.rank.RankAnnouncerHandler;
import me.vcb.astrocore.handlers.rank.RankReviveHandler;
import me.vcb.astrocore.handlers.rank.ReclaimHandler;
import me.vcb.astrocore.handlers.salvage.SalvageHandler;
import me.vcb.astrocore.handlers.settings.SettingsHandler;
import me.vcb.astrocore.handlers.staff.FreezeHandler;
import me.vcb.astrocore.handlers.staff.NotesHandler;
import me.vcb.astrocore.handlers.staff.RebootHandler;
import me.vcb.astrocore.handlers.staff.StaffChatHandler;
import me.vcb.astrocore.handlers.staff.WarpsHandler;
import me.vcb.astrocore.handlers.timer.CombatTagHandler;
import me.vcb.astrocore.handlers.timer.EnderPearlHandler;
import me.vcb.astrocore.handlers.timer.EotwHandler;
import me.vcb.astrocore.handlers.timer.GoldenAppleHandler;
import me.vcb.astrocore.handlers.timer.LogoutHandler;
import me.vcb.astrocore.handlers.timer.PurgeHandler;
import me.vcb.astrocore.handlers.timer.PvpProtHandler;
import me.vcb.astrocore.handlers.timer.SotwHandler;
import me.vcb.astrocore.hologram.HologramManager;
import me.vcb.astrocore.utils.ManagerEnabler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class HandlerManager implements ManagerEnabler {

    private final List<Handler> handlers;

    public HandlerManager() {
        this.handlers = new ArrayList<>();

        this.handlers.add(new AutoModHandler());
        this.handlers.add(new AutoSmeltHandler());
        this.handlers.add(new BlockedCommandsHandler());
        this.handlers.add(new BorderHandler());
        this.handlers.add(new BottleHandler());
        this.handlers.add(new ChatControlHandler());
        this.handlers.add(new ChatHandler());
        this.handlers.add(new CombatLoggerHandler());
        this.handlers.add(new CombatTagHandler());
        this.handlers.add(new CrowbarHandler());
        this.handlers.add(new CustomRecipeHandler());
        this.handlers.add(new BroadcastSchedulerHandler());
        this.handlers.add(new DeathMessageHandler());
        this.handlers.add(new DeathRecapHandler());
        this.handlers.add(new DeathSignHandler());
        this.handlers.add(new DisabledBlocksHandler());
        this.handlers.add(new DisenchantHandler());
        this.handlers.add(new DynamicEventHandler());
        this.handlers.add(new EnchantLimiterHandler());
        this.handlers.add(new EnderPearlHandler());
        this.handlers.add(new EndPortalHandler());
        this.handlers.add(new EntityLimiterHandler());
        this.handlers.add(new EotwHandler());
        this.handlers.add(new ExpAmplifierHandler());
        this.handlers.add(new FilterHandler());
        this.handlers.add(new FoundOreHandler());
        this.handlers.add(new FreezeHandler());
        this.handlers.add(new GlisteringMelonHandler());
        this.handlers.add(new GoldenAppleHandler());
        this.handlers.add(new HeadDropHandler());
        this.handlers.add(new InventoryHandler());
        this.handlers.add(new KillstreakHandler());
        this.handlers.add(new LeaderboardHandler());
        this.handlers.add(new LogoutHandler());
        this.handlers.add(new MapkitHandler());
        this.handlers.add(new MessagingHandler());
        this.handlers.add(new MinecartElevatorHandler());
        this.handlers.add(new NetherPortalTrapHandler());
        this.handlers.add(new NotesHandler());
        this.handlers.add(new PortalHandler());
        this.handlers.add(new PotionLimiterHandler());
        this.handlers.add(new PurgeHandler());
        this.handlers.add(new PvpProtHandler());
        this.handlers.add(new RankReviveHandler());
        this.handlers.add(new RebootHandler());
        this.handlers.add(new ReclaimHandler());
        this.handlers.add(new SalvageHandler());
        this.handlers.add(new SettingsHandler());
        this.handlers.add(new SignElevatorHandler());
        this.handlers.add(new SotwHandler());
        this.handlers.add(new SpawnTeleportHandler());
        this.handlers.add(new StaffChatHandler());
        this.handlers.add(new StatsHandler());
        this.handlers.add(new StattrakHandler());
        this.handlers.add(new SubclaimHandler());
        this.handlers.add(new WarpsHandler());

        if(Config.KITMAP_MODE_ENABLED) this.handlers.add(new KitmapHandler());
        if(Config.MOB_STACK_ENABLED) this.handlers.add(new MobStackHandler());
        if(Config.COBWEB_REMOVER_ENABLED) this.handlers.add(new CobwebRemoveHandler());
        if(Config.ONLINE_RANK_ANNOUNCER_ENABLED) this.handlers.add(new RankAnnouncerHandler());

        this.handlers.stream().filter(handler -> handler instanceof Listener).forEach(handler ->
            Bukkit.getPluginManager().registerEvents((Listener) handler, AstroCore.getInstance()));

        AstroCore.getInstance().log("- &7Enabled &a" + this.handlers.size() + " &7handlers.");
    }

    public void disable() {
        try {
            this.handlers.forEach(Handler::disable);
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.handlers.clear();
    }

    public Handler getHandler(Class<?> clazz) {
        return this.handlers.stream().filter(handler -> handler.getClass() == clazz).findFirst().orElse(null);
    }
}

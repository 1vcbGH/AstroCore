package me.vcb.astrocore.commands.manager;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.abilities.commands.AbilityCommand;
import me.vcb.astrocore.abilities.commands.AbilityTimerCommand;
import me.vcb.astrocore.commands.*;
import me.vcb.astrocore.commands.base.*;
import me.vcb.astrocore.commands.staff.*;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.economy.commands.BalanceCommand;
import me.vcb.astrocore.partner.commands.PartnerAdminCommand;
import me.vcb.astrocore.partner.commands.PartnerCommand;
import me.vcb.astrocore.partner.commands.PartnerSetupCommand;
import me.vcb.astrocore.economy.commands.EconomyCommand;
import me.vcb.astrocore.economy.commands.PayCommand;
import me.vcb.astrocore.utils.ManagerEnabler;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements ManagerEnabler {

    private final CommandMap commandMap;
    private final List<BaseCommand> commands;

    public CommandManager() {
        this.commandMap = NmsUtils.getInstance().getCommandMap();
        this.commands = new ArrayList<>();

        this.commands.add(new AbilityCommand());
        this.commands.add(new AbilityTimerCommand());

        this.commands.add(new ChatControlCommand());
        this.commands.add(new CopyInventoryCommand());
        this.commands.add(new CrowbarCommand());
        this.commands.add(new CustomTimerCommand());
        this.commands.add(new DeathbanCommand());
        this.commands.add(new EndPortalCommand());
        this.commands.add(new EotwCommand());
        this.commands.add(new ExitCommand());
        this.commands.add(new FreezeAllCommand());
        this.commands.add(new FreezeCommand());
        this.commands.add(new HardResetCommand());
        this.commands.add(new DisguiseCommand());
        this.commands.add(new HeadModCommand());
        this.commands.add(new HideStaffCommand());
        this.commands.add(new InventoryInspectCommand());
        this.commands.add(new InventoryRestoreCommand());
        this.commands.add(new LagCommand());
        this.commands.add(new LastDeathsCommand());
        this.commands.add(new LastKillsCommand());
        this.commands.add(new LivesCommand());
        this.commands.add(new NotesCommand());
        this.commands.add(new PlaytimeCommand());
        this.commands.add(new PurgeCommand());
        this.commands.add(new RandomTeleportCommand());
        this.commands.add(new RebootCommand());
        this.commands.add(new SaleCommand());
        this.commands.add(new SetExitCommand());
        this.commands.add(new SetReclaimCommand());
        this.commands.add(new SetSlotsCommand());
        this.commands.add(new SetSpawnCommand());
        this.commands.add(new StaffChatCommand());
        this.commands.add(new StaffModeCommand());
        this.commands.add(new StaffScoreboardCommand());
        this.commands.add(new TimerCommand());
        this.commands.add(new VanishCommand());
        this.commands.add(new ViewDistanceCommand());

        this.commands.add(new PartnerCommand());
        this.commands.add(new PartnerAdminCommand());
        this.commands.add(new PartnerSetupCommand());
        this.commands.add(new me.vcb.astrocore.rank.RankAdminCommand());
        this.commands.add(new me.vcb.astrocore.rank.RankCommand());
        this.commands.add(new me.vcb.astrocore.bounty.BountyCommand());
        this.commands.add(new me.vcb.astrocore.lottery.LotteryCommand());
        this.commands.add(new me.vcb.astrocore.friends.FriendCommand());
        this.commands.add(new AutoModCommand());
        this.commands.add(new IpCommand());
        this.commands.add(new AltsCommand());
        this.commands.add(new HistorialCommand());
        this.commands.add(new me.vcb.astrocore.coinshop.CoinShopCommand());
        this.commands.add(new me.vcb.astrocore.supplydrop.SupplyDropCommand());
        this.commands.add(new me.vcb.astrocore.events.EventCommand());
        this.commands.add(new me.vcb.astrocore.tags.TagsCommand());
        this.commands.add(new me.vcb.astrocore.tags.TagAdminCommand());
        this.commands.add(new me.vcb.astrocore.report.ReportsCommand());
        this.commands.add(new me.vcb.astrocore.customenchants.CustomEnchantCommand());
        this.commands.add(new me.vcb.astrocore.crates.CrateCommand());
        this.commands.add(new me.vcb.astrocore.npc.NpcCommand());

        // Punishment system (force-registered below to override vanilla /ban, /kick)
        this.commands.add(new me.vcb.astrocore.punishments.commands.BanCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.MuteCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.WarnCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.KickCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.BlacklistCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.UnbanCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.UnmuteCommand());
        this.commands.add(new me.vcb.astrocore.punishments.commands.PunishCommand());

        this.commands.add(new me.vcb.astrocore.perks.PerksCommand());
        this.commands.add(new me.vcb.astrocore.elevator.ElevatorCommand());
        this.commands.add(new me.vcb.astrocore.factions.vault.VaultAdminCommand());
        this.commands.add(new me.vcb.astrocore.games.koth.KothMenu());
        this.commands.add(new me.vcb.astrocore.missions.MissionsCommand());
        this.commands.add(new me.vcb.astrocore.battlepass.BattlePassCommand());
        this.commands.add(new me.vcb.astrocore.shop.ShopCommand());

        this.commands.add(new BalanceCommand());
        this.commands.add(new EconomyCommand());
        this.commands.add(new PayCommand());

        this.commands.add(new CompararCommand());
        this.commands.add(new CoordsCommand());
        this.commands.add(new FilterCommand());
        this.commands.add(new FocusCommand());
        this.commands.add(new GoldenAppleCommand());
        this.commands.add(new HelpCommand());
        this.commands.add(new AstroCoreCommand());
        this.commands.add(new LeaderboardCommand());
        this.commands.add(new LffCommand());
        this.commands.add(new LogoutCommand());
        this.commands.add(new MapkitCommand());
        this.commands.add(new MinerEffectsCommand());
        this.commands.add(new PvpCommand());
        this.commands.add(new RankReviveCommand());
        this.commands.add(new ReclaimCommand());
        this.commands.add(new ReportCommand());
        this.commands.add(new RequestCommand());
        this.commands.add(new SettingsCommand());
        this.commands.add(new SotwCommand());
        this.commands.add(new SpawnCommand());
        this.commands.add(new SpawnCreditsCommand());
        this.commands.add(new StatsCommand());
        this.commands.add(new SubclaimCommand());
        this.commands.add(new TellLocationCommand());
        this.commands.add(new ToggleChatCommand());
        this.commands.add(new ToggleCobbleCommand());
        this.commands.add(new ToggleDeathMessagesCommand());
        this.commands.add(new ToggleFoundOreCommand());
        this.commands.add(new ToggleLightningCommand());
        this.commands.add(new ToggleScoreboardCommand());
        this.commands.add(new TopBalanceCommand());
        this.commands.add(new TopDeathsCommand());
        this.commands.add(new TopKillsCommand());
        this.commands.add(new TopKillstreaksCommand());
        this.commands.add(new UnfocusCommand());
        this.commands.add(new PerfilCommand());

        this.commands.add(new BackCommand());
        this.commands.add(new BroadcastCommand());
        this.commands.add(new BroadcastRawCommand());
        this.commands.add(new ClearInventoryCommand());
        this.commands.add(new DayCommand());
        this.commands.add(new DeleteWarpCommand());
        this.commands.add(new EnchantCommand());
        this.commands.add(new EnderchestCommand());
        this.commands.add(new ExperienceCommand());
        this.commands.add(new FeedCommand());
        this.commands.add(new FlyCommand());
        this.commands.add(new GiveCommand());
        this.commands.add(new GodCommand());
        this.commands.add(new HealCommand());
        this.commands.add(new IgnoreCommand());
        this.commands.add(new InvseeCommand());
        this.commands.add(new ItemCommand());
        this.commands.add(new KickallCommand());
        this.commands.add(new KillallCommand());
        this.commands.add(new KillCommand());
        this.commands.add(new MessageCommand());
        this.commands.add(new MsgHistCommand());
        this.commands.add(new MoreCommand());
        this.commands.add(new NightCommand());
        this.commands.add(new RenameCommand());
        this.commands.add(new RepairCommand());
        this.commands.add(new ReplyCommand());
        this.commands.add(new SeenCommand());
        this.commands.add(new SetWarpCommand());
        this.commands.add(new SocialSpyCommand());
        this.commands.add(new SpawnerCommand());
        this.commands.add(new SpeedCommand());
        this.commands.add(new TeleportAllCommand());
        this.commands.add(new TeleportCommand());
        this.commands.add(new TeleportHereCommand());
        this.commands.add(new TeleportPositionCommand());
        this.commands.add(new ToggleMsgCommand());
        this.commands.add(new ToggleSoundsCommand());
        this.commands.add(new TopCommand());
        this.commands.add(new WarpCommand());
        this.commands.add(new WorldCommand());

        this.commands.forEach(this::registerCommand);

        // Force punishment labels to win over vanilla /ban, /kick, etc.
        forceOverride("ban", "mute", "warn", "kick", "blacklist", "unban", "unmute", "punish");

        AstroCore.getInstance().log("- &7Enabled &a" + this.commands.size() + " &7commands.");
    }

    public void disable() {
        this.commands.clear();
    }

    void registerCommand(BukkitCommand command) {
        if(!Config.DISABLED_LAZARUS_COMMANDS.contains(command.getName())) {
            this.commandMap.register("astrocore", command);
        }
    }

    /** Forces our command under the bare label, overriding any vanilla/plugin command. */
    @SuppressWarnings("unchecked")
    private void forceOverride(String... names) {
        try {
            java.lang.reflect.Method m = this.commandMap.getClass().getMethod("getKnownCommands");
            java.util.Map<String, org.bukkit.command.Command> known =
                (java.util.Map<String, org.bukkit.command.Command>) m.invoke(this.commandMap);
            for (String name : names) {
                BaseCommand ours = this.commands.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
                if (ours == null) continue;
                known.put(name.toLowerCase(), ours);
                known.put("astrocore:" + name.toLowerCase(), ours);
            }
        } catch (Exception e) {
            AstroCore.getInstance().log("&cNo se pudo forzar el registro de comandos de castigo: " + e.getMessage());
        }
    }
}

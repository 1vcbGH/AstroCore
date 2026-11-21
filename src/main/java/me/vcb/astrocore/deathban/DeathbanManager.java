package me.vcb.astrocore.deathban;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.deathban.event.PlayerDeathbanEvent;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent;
import me.vcb.astrocore.handlers.event.AstroCoreKickEvent.KickType;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.GsonUtils;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import me.vcb.astrocore.utils.Tasks;
import me.vcb.astrocore.utils.ManagerEnabler;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathbanManager implements Listener, ManagerEnabler {

    private final File deathbansFile;

    private Map<UUID, Deathban> deathbans;
    private final List<DeathBanTime> deathBanTimes;
    private final List<UUID> confirmations;

    public DeathbanManager() {
        this.deathbansFile = FileUtils.getOrCreateFile(Config.DEATHBAN_DIR, "deathbans.json");

        this.deathBanTimes = new ArrayList<>();
        this.confirmations = new ArrayList<>();

        this.loadDeathbans();
        this.loadDeathBanTimes();

        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        this.saveDeathbans(true);
        this.deathbans.clear();
    }

    private void loadDeathbans() {
        String content = FileUtils.readWholeFile(this.deathbansFile);

        if(content == null) {
            this.deathbans = new HashMap<>();
            return;
        }

        this.deathbans = AstroCore.getInstance().getGson().fromJson(content, GsonUtils.DEATHBAN_TYPE);
        deathbans.keySet().removeIf(uuid -> !this.isDeathBanned(uuid));

        AstroCore.getInstance().log("- &7Loaded &a" + this.deathbans.size() + " &7deathbans.");
    }

    private void saveDeathbans(boolean disable) {
        if(this.deathbans == null) return;

        FileUtils.writeString(this.deathbansFile, AstroCore.getInstance().getGson()
            .toJson(this.deathbans, GsonUtils.DEATHBAN_TYPE));

        if(disable) AstroCore.getInstance().log("- &7Saved &a" + this.deathbans.size() + " &7deathbans.");
    }

    private void loadDeathBanTimes() {
        ConfigurationSection section = AstroCore.getInstance().getConfig().getSection("DEATHBAN.BAN_TIMES");

        section.getKeys(false).forEach(time -> this.deathBanTimes
            .add(new DeathBanTime(Integer.parseInt(time), section.getString(time))));
    }

    public int getBanTime(Player player) {
        return this.deathBanTimes.stream().filter(time -> player.hasPermission(time.getPermission()))
        .findFirst().map(DeathBanTime::getTime).orElse(Config.DEATHBAN_DEFAULT_BAN_TIME) * 60 * 1000;
    }

    public boolean isDeathBanned(UUID uuid) {
        return this.deathbans.containsKey(uuid) && this.deathbans.get(uuid).getBannedUntil() > System.currentTimeMillis();
    }

    public Deathban getDeathban(UUID uuid) {
        return this.deathbans.get(uuid);
    }

    public int getLives(OfflinePlayer offlinePlayer) {
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(offlinePlayer);
        return data != null ? data.getLives() : 0;
    }

    public void deleteAllDeathbans(boolean log) {
        int deleted = this.deathbans.size();

        this.deathbans.clear();
        Tasks.async(() -> this.saveDeathbans(false));

        if(log) {
            AstroCore.getInstance().log("- &cDeleted &e" + deleted + " &cdeathbans.");
        }
    }

    public void revivePlayer(UUID uuid) {
        this.deathbans.remove(uuid);
        Tasks.async(() -> this.saveDeathbans(false));
    }

    public void revivePlayer(CommandSender sender, OfflinePlayer target) {
        if(!this.isDeathBanned(target.getUniqueId())) {
            sender.sendMessage(Language.PREFIX + Language.DEATHBAN_PLAYER_NOT_DEATHBANNED
            .replace("<player>", target.getName()));
            return;
        }

        this.revivePlayer(target.getUniqueId());

        Messages.sendMessage(Language.PREFIX + Language.DEATHBAN_REVIVED_PLAYER
            .replace("<player>", sender.getName())
            .replace("<target>", target.getName()), "astrocore.staff");
    }

    public void checkStatus(CommandSender sender, OfflinePlayer target) {
        if(!this.isDeathBanned(target.getUniqueId())) {
            sender.sendMessage(Language.PREFIX + Language.DEATHBAN_PLAYER_NOT_DEATHBANNED
            .replace("<player>", target.getName()));
            return;
        }

        Deathban deathban = this.getDeathban(target.getUniqueId());

        String duration = (deathban.getBannedUntil() - System.currentTimeMillis() <= 0)
            ? "Unbanned" : StringUtils.formatDeathban(deathban.getBannedUntil());

        Language.DEATHBAN_COMMAND_CHECK.forEach(message -> sender.sendMessage(message
            .replace("<player>", target.getName())
            .replace("<duration>", duration)
            .replace("<reason>", deathban.getReason())
            .replace("<location>", deathban.getLocation())));
    }

    public void sendLivesCount(CommandSender sender) {
        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_FOR_PLAYERS_ONLY);
            return;
        }

        Player player = (Player) sender;
        this.sendLivesCount(sender, player);
    }

    public void sendLivesCount(CommandSender sender, OfflinePlayer target) {
        if(sender == target) {
            sender.sendMessage(Language.PREFIX + Language.LIVES_CHECK_SELF
            .replace("<lives>", String.valueOf(this.getLives(target))));
            return;
        }

        sender.sendMessage(Language.PREFIX + Language.LIVES_CHECK_OTHERS
            .replace("<player>", target.getName())
            .replace("<lives>", String.valueOf(this.getLives(target))));
    }

    private boolean checkLivesZero(Player player, Userdata data) {
        if(data.getLives() > 0) return true;

        player.sendMessage(Language.PREFIX + Language.LIVES_ZERO_LIVES);
        return false;
    }

    public void reviveUsingLives(CommandSender sender, OfflinePlayer target) {
        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_FOR_PLAYERS_ONLY);
            return;
        }

        Player player = (Player) sender;

        if(!this.isDeathBanned(target.getUniqueId())) {
            sender.sendMessage(Language.PREFIX + Language.LIVES_PLAYER_NOT_DEATHBANNED.replace("<player>", target.getName()));
            return;
        }

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if(!this.checkLivesZero(player, data)) return;

        data.subtractLives(1);

        this.revivePlayer(target.getUniqueId());

        player.sendMessage(Language.PREFIX + Language.LIVES_SUCCESSFULLY_REVIVED_PLAYER
            .replace("<player>", target.getName()));
    }

    public void sendLives(CommandSender sender, OfflinePlayer target, String amount) {
        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_FOR_PLAYERS_ONLY);
            return;
        }

        if(!StringUtils.isInteger(amount)) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_NUMBER);
            return;
        }

        Player player = (Player) sender;

        if(player == target) {
            player.sendMessage(Language.PREFIX + Language.LIVES_CAN_NOT_SEND_LIVES_TO_YOURSELF);
            return;
        }

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if(!this.checkLivesZero(player, data)) return;

        int lives = Math.abs(Integer.parseInt(amount));

        if(data.getLives() < lives) {
            player.sendMessage(Language.PREFIX + Language.LIVES_NOT_ENOUGH_LIVES.replace("<amount>", String.valueOf(lives)));
            return;
        }

        Userdata targetData = AstroCore.getInstance().getUserdataManager().getUserdata(target);

        data.subtractLives(lives);
        targetData.addLives(lives);

        player.sendMessage(Language.PREFIX + Language.LIVES_SUCCESSFULLY_SENT_LIVES
            .replace("<amount>", String.valueOf(lives))
            .replace("<player>", target.getName()));

        if(target.isOnline()) {
            target.getPlayer().sendMessage(Language.PREFIX + Language.LIVES_SUCCESSFULLY_RECEIVED_LIVES
                .replace("<amount>", String.valueOf(lives))
                .replace("<player>", player.getName()));
        }
    }

    public void changeLives(CommandSender sender, OfflinePlayer target, String amount, boolean addLives) {
        if(!StringUtils.isInteger(amount)) {
            sender.sendMessage(Language.PREFIX + Language.COMMANDS_INVALID_NUMBER);
            return;
        }

        Userdata userdata = AstroCore.getInstance().getUserdataManager().getUserdata(target);

        if(addLives) {
            userdata.addLives(Math.abs(Integer.parseInt(amount)));

            sender.sendMessage(Language.PREFIX + Language.LIVES_ADDED
                .replace("<amount>", amount)
                .replace("<player>", target.getName()));

            if(target.isOnline()) {
                target.getPlayer().sendMessage(Language.PREFIX + Language.LIVES_ADD_RECEIVED
                    .replace("<amount>", amount)
                    .replace("<player>", sender.getName()));
            }
        } else {
            userdata.setLives(Math.abs(Integer.parseInt(amount)));

            sender.sendMessage(Language.PREFIX + Language.LIVES_SET
                .replace("<amount>", amount)
                .replace("<player>", target.getName()));

            if(target.isOnline()) {
                target.getPlayer().sendMessage(Language.PREFIX + Language.LIVES_SET_RECEIVED
                    .replace("<amount>", amount)
                    .replace("<player>", sender.getName()));
            }
        }
    }

    public void deathbanPlayer(Player player, Location location, int time, String reason) {
        AstroCore.getInstance().getInventoryRestoreManager().saveInventoryUponDeath(player);

        if(!Config.DEATHBAN_ENABLED || player.hasPermission("astrocore.deathban.bypass")) return;
        if(Config.KITMAP_MODE_ENABLED && Config.KITMAP_MODE_DISABLE_DEATHBAN) return;
        if(!ClaimManager.getInstance().getFactionAt(player).isDeathban()) return;

        PlayerDeathbanEvent event = new PlayerDeathbanEvent(player);
        if(event.isCancelled()) return;

        if(time != 0) {
            Deathban deathban = new Deathban();
            deathban.setBannedUntil(System.currentTimeMillis() + time);
            deathban.setReason(reason);
            deathban.setLocation(StringUtils.getLocationNameWithWorld(location));

            this.deathbans.put(player.getUniqueId(), deathban);
            Tasks.syncLater(() -> this.kickPlayer(player, reason), 20L);
        }
    }

    private void kickPlayer(Player player, String reason) {
        String kickMessage;

        if(AstroCore.getInstance().getEotwHandler().isActive()) {
            kickMessage = Language.DEATHBAN_EOTW_MESSAGE;
        } else {
            kickMessage = Language.DEATHBAN_BAN_MESSAGE.replace("<time>", DurationFormatUtils
                .formatDurationWords(this.getBanTime(player), true, true)).replace("<reason>", reason);
        }

        AstroCoreKickEvent event = new AstroCoreKickEvent(player, KickType.DEATHBAN, kickMessage);

        if(!event.isCancelled()) {
            player.kickPlayer(kickMessage);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (me.vcb.astrocore.handlers.logger.CombatLoggerHandler.isCombatLogDeath(player)) return;

        this.deathbanPlayer(player, player.getLocation(), this.getBanTime(player), event.getDeathMessage());
    }

    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if(!this.isDeathBanned(uuid)) {
            if(!this.deathbans.containsKey(uuid) || !AstroCore.getInstance().getEotwHandler().isActive()) return;

            event.disallow(Result.KICK_BANNED, Language.DEATHBAN_EOTW_MESSAGE);
            return;
        }

        if(AstroCore.getInstance().getEotwHandler().isActive()) {
            event.disallow(Result.KICK_BANNED, Language.DEATHBAN_EOTW_MESSAGE);
            return;
        }

        Deathban deathban = this.getDeathban(uuid);
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(uuid);

        if(data == null || data.getLives() <= 0) {
            event.disallow(Result.KICK_BANNED, Language.DEATHBAN_BAN_MESSAGE
                .replace("<time>", StringUtils.formatDeathban(deathban.getBannedUntil()))
                .replace("<reason>", deathban.getReason()));

            return;
        }

        if(this.confirmations.contains(uuid)) {
            data.subtractLives(1);
            this.deathbans.remove(uuid);
            this.confirmations.remove(uuid);
            event.allow();
            return;
        }

        this.confirmations.add(uuid);

        event.disallow(Result.KICK_BANNED, Language.DEATHBAN_BAN_MESSAGE
            .replace("<time>", StringUtils.formatDeathban(deathban.getBannedUntil()))
            .replace("<reason>", deathban.getReason()) + "\n\n" + Language.DEATHBAN_JOIN_AGAIN_FOR_REVIVE
            .replace("<amount>", String.valueOf(data.getLives())));
    }

    @Getter
    @AllArgsConstructor
    private static class DeathBanTime {

        private final int time;
        private final String permission;
    }
}

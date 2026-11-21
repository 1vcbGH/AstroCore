package me.vcb.astrocore.games.koth;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.games.Capzone;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.koth.event.KothCappedEvent;
import me.vcb.astrocore.games.koth.event.KothKnockedEvent;
import me.vcb.astrocore.games.koth.event.KothStartedCappingEvent;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunningKoth {

    @Getter private final KothData kothData;
    @Getter private final Capzone capzone;

    private int initialCapTime;

    private final long startTime;
    private long capperChange;

    private long antiSpamDelay;

    // Prevents handleWin() from firing twice when async tick overlaps with sync stop
    private final AtomicBoolean handled = new AtomicBoolean(false);

    RunningKoth(KothData kothData, int time) {
        this.kothData = kothData;
        this.capzone = new Capzone(kothData.getCuboid(), time);

        this.initialCapTime = time;
        this.startTime = this.capperChange = System.currentTimeMillis();

        this.capzone.getCuboid().getPlayers().forEach(player -> this.onPlayerEnterCapzone(player));
    }

    public void changeCapTime(int newTime) {
        if(newTime < this.capzone.getTime()) this.capzone.setTime(newTime);
        this.initialCapTime = newTime;
    }

    private void handleWin() {
        // compareAndSet: only one async tick wins, rest are ignored
        if(!this.handled.compareAndSet(false, true)) return;

        Player capper = this.capzone.getCapper();
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(capper);

        String uptime = StringUtils.formatMillis(System.currentTimeMillis() - this.startTime);

        if(faction == null) {
            Language.KOTH_CAPPED_NO_FACTION.forEach(line -> Messages.sendMessage(Placeholder.RunningKothReplacer
            .parse(this, line.replace("<uptime>", uptime))));
        } else {
            Language.KOTH_CAPPED_WITH_FACTION.forEach(line -> Messages.sendMessage(Placeholder.RunningKothReplacer
            .parse(this, line
                .replace("<faction>", faction.getDisplayName(capper))
                .replace("<uptime>", uptime))));
        }

        // tick() now runs on main thread (syncTimer), so call directly
        new KothCappedEvent(this.kothData, capper);

        me.vcb.astrocore.userdata.Userdata capperUd = AstroCore.getInstance().getUserdataManager().getUserdata(capper);
        if(capperUd != null) capperUd.addKothCapped();

        AstroCore.getInstance().getKothManager().stopKoth(this);
    }

    private void handlePlayerStartedCapping(Player player) {
        if(player == null) return;

        player.sendMessage(Language.KOTH_PREFIX + Placeholder.RunningKothReplacer
            .parse(this, Language.KOTH_YOU_STARTED_CAPPING));

        if((this.antiSpamDelay - System.currentTimeMillis()) <= 0) {
            Messages.sendMessageWithoutPlayer(player, Language.KOTH_PREFIX + Placeholder.RunningKothReplacer
                .parse(this, Language.KOTH_SOMEONE_STARTED_CAPPING));

            this.antiSpamDelay = System.currentTimeMillis() + Config.KOTH_ANTI_SPAM_MESSAGE_DELAY;
        }

        new KothStartedCappingEvent(this, player);
    }

    private void handlePlayerKnocked(Player player) {
        this.capzone.setTime(this.initialCapTime);

        long capTimeMillis = System.currentTimeMillis() - this.capperChange;
        this.capperChange = System.currentTimeMillis();

        if((this.antiSpamDelay - System.currentTimeMillis()) <= 0) {
            Messages.sendMessage(Language.KOTH_PREFIX + Placeholder.RunningKothReplacer
                .parse(this, Language.KOTH_KNOCKED));

            this.antiSpamDelay = System.currentTimeMillis() + Config.KOTH_ANTI_SPAM_MESSAGE_DELAY;
        }

        new KothKnockedEvent(this, player, capTimeMillis);
    }

    void onPlayerEnterCapzone(Player player) {
        if(AstroCore.getInstance().getStaffModeManager().isInStaffModeOrVanished(player)) return;

        if(this.capzone.hasNoPlayers()) {
            this.handlePlayerStartedCapping(player);
        }

        this.capzone.addPlayer(player);
    }

    void onPlayerLeaveCapzone(Player player) {
        if(!this.capzone.getPlayers().contains(player.getName())) return;

        if(this.capzone.isCapper(player)) {
            this.handlePlayerKnocked(player);

            if(this.capzone.getPlayers().size() > 1) {
                this.handlePlayerStartedCapping(this.capzone.getNextCapper());
            }
        }

        this.capzone.removePlayer(player);
    }

    void tick() {
        if(this.capzone.hasNoPlayers()) {
            int timePassed = (int) ((System.currentTimeMillis() - this.capperChange) / 1000);
            int nobodyCappingInterval = Config.KOTH_NOBODY_CAPPING_MESSAGE_INTERVAL;

            if(nobodyCappingInterval != 0 && timePassed > 0 && timePassed % nobodyCappingInterval == 0) {
                Messages.sendMessage(Language.KOTH_PREFIX + Placeholder
                    .RunningKothReplacer.parse(this, Language.KOTH_NO_ONE_CAPPING));
            }

            return;
        }

        if(this.capzone.decreaseTime() <= 0) {
            this.handleWin();
            return;
        }

        int cappingInterval = Config.KOTH_CAPPING_MESSAGE_INTERVAL;

        if(cappingInterval != 0 && this.capzone.getTime() % cappingInterval == 0) {
            Player capper = this.capzone.getCapper();

            capper.sendMessage(Language.KOTH_PREFIX + Placeholder
                .RunningKothReplacer.parse(this, Language.KOTH_YOU_ARE_CAPPING));

            Messages.sendMessageWithoutPlayer(capper, Language.KOTH_PREFIX + Placeholder
                .RunningKothReplacer.parse(this, Language.KOTH_SOMEONE_IS_CAPPING));
        }
    }

    public String getScoreboardEntry() {
        return this.capzone.getTimeLeft();
    }
}

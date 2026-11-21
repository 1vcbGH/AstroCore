package me.vcb.astrocore.scoreboard.nms;

import com.lunarclient.bukkitapi.LunarClientAPI;
import lombok.Setter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.rank.PlayerRank;
import me.vcb.astrocore.scoreboard.PlayerScoreboard;
import me.vcb.astrocore.scoreboard.ScoreboardInput;
import me.vcb.astrocore.scoreboard.base.ScoreboardBase_1_21;
import me.vcb.astrocore.timer.TimerManager;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerScoreboard_1_21 extends ScoreboardBase_1_21 implements PlayerScoreboard {

    private static final ScoreboardInput EMPTY_INPUT = new ScoreboardInput("", "", "");

    private final Deque<ScoreboardInput> entries;
    private Set<String> lastEntries;

    private final ScoreboardInput[] entryCache;

    private final AtomicBoolean update;
    private final AtomicBoolean lastLine;

    private Team members;
    private Team archers;
    private Team focused;
    private Team allies;
    private Team enemies;
    private Team sotw;
    private Team staff;
    private Team headMod;
    private Team invis;

    @Setter private int maxSize;

    public PlayerScoreboard_1_21(Player player) {
        super(player, NmsUtils.getInstance().getPlayerScoreboard(player));

        this.setupObjective();

        this.entries = new ArrayDeque<>();
        this.lastEntries = new HashSet<>();

        this.entryCache = new ScoreboardInput[15];

        for (int i = 0; i < 15; i++) {
            this.entryCache[i] = EMPTY_INPUT;
        }

        this.setupTeams();

        this.update = new AtomicBoolean(false);
        this.lastLine = new AtomicBoolean(false);

        // Minecraft's sidebar hard-caps at 15 lines. Use the full budget so that
        // class + energy + cooldown + combat tag all fit at once instead of one
        // pushing out another. addLinesAndFooter() reserves the top divider (1 line);
        // the footer is best-effort and yields when the board is completely full.
        this.maxSize = 14;

        player.setScoreboard(this.scoreboard);
    }

    @Override
    public void unregister() {
        synchronized (this.scoreboard) {
            for (Objective objective : this.scoreboard.getObjectives()) {
                objective.unregister();
            }
            for (Team team : this.scoreboard.getTeams()) {
                team.unregister();
            }
        }

        for (String entry : this.scoreboard.getEntries()) {
            this.resetScore(entry);
        }

        this.player = null;
    }

    @Override
    public void applyTitle() {
        Objective objective = this.scoreboard.getObjective("astrocore");
        if (objective != null) objective.setDisplayName(Config.SCOREBOARD_TITLE);
    }

    private void setupObjective() {
        Objective objective = this.scoreboard.getObjective("astrocore");

        if (objective == null) {
            objective = this.scoreboard.registerNewObjective("astrocore", "dummy", Config.SCOREBOARD_TITLE);
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Config.SCOREBOARD_TITLE);
    }

    private void setupTeams() {
        this.members = this.getTeam(Color.translate("&8&3members"));
        this.members.setPrefix(Config.TEAMMATE_COLOR);
        this.members.setCanSeeFriendlyInvisibles(true);

        this.allies = this.getTeam(Color.translate("&8&4allies"));
        this.allies.setPrefix(Config.ALLY_COLOR);

        this.archers = this.getTeam(Color.translate("&8&2archers"));
        this.archers.setPrefix(Config.ARCHER_TAGGED_COLOR);

        this.focused = this.getTeam(Color.translate("&8&1focused"));
        this.focused.setPrefix(Config.FOCUSED_COLOR);

        this.enemies = this.getTeam(Color.translate("&8&5enemies"));
        this.enemies.setPrefix(Config.ENEMY_COLOR);

        this.sotw = this.getTeam(Color.translate("&8&6sotw"));
        this.sotw.setPrefix(Config.SOTW_COLOR);

        this.staff = this.getTeam(Color.translate("&8&7staff"));
        this.staff.setPrefix(Config.STAFF_MODE_COLOR);

        this.headMod = this.getTeam(Color.translate("&8&9headmod"));
        this.headMod.setPrefix(Color.translate(Config.HEAD_MOD_COLOR));

        this.invis = this.getTeam(Color.translate("&8&8invis"));
        this.invis.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    @Override
    public void update() {
        if (!this.update.get() && this.lastEntries.isEmpty()) return;

        Set<String> addedEntries = new HashSet<>(this.entries.size());

        for (int i = Math.min(15, this.entries.size()); i > 0; i--) {
            ScoreboardInput input = this.entries.pollFirst();
            if (input == null) return;

            addedEntries.add(input.getName());

            if (this.entryCache[i - 1].equals(input)) {
                continue;
            }

            Team team = this.getTeam(input.getName());

            if (!team.hasEntry(input.getName())) {
                team.addEntry(input.getName());
            }

            this.updateTeam(team.getName(), input.getPrefix(), input.getSuffix());

            this.entryCache[i - 1] = input;
            this.setScore(input.getName(), i);
        }

        if (addedEntries.size() < this.lastEntries.size()) {
            for (int i = addedEntries.size(); i < this.lastEntries.size(); i++) {
                this.entryCache[i] = EMPTY_INPUT;
            }
        }

        for (String entry : this.lastEntries) {
            if (!addedEntries.contains(entry)) {
                this.resetScore(entry);
            }
        }

        this.lastEntries = addedEntries;
        this.update.set(false);
    }

    @Override
    public void addLine(ChatColor color) {
        if (!Config.SCOREBOARD_LINE_AFTER_EVERY_SECTION || this.entries.isEmpty()
                || this.lastLine.get() || this.entries.size() >= this.maxSize) return;

        this.entries.addLast(this.getScoreboardLineInput(color));
        this.lastLine.set(true);
    }

    @Override
    public void addEmptyLine(ChatColor color) {
        this.entries.addLast(new ScoreboardInput("", color.toString() + Config.SCOREBOARD_LINE_COLOR + ChatColor.STRIKETHROUGH, ""));
        this.lastLine.set(true);
    }

    @Override
    public void add(String value, String time) {
        if (value.isEmpty() || this.entries.size() >= this.maxSize) return;

        if (time.length() > 16) {
            time = time.substring(0, 16);
        }

        this.entries.addLast(this.splitInput(value, time));

        this.lastLine.set(false);
    }

    @Override
    public void addConquest(String prefix, String value, String suffix) {
        if (this.entries.size() >= this.maxSize) return;

        this.entries.addLast(new ScoreboardInput(prefix, Color.translate(value), suffix));
        this.lastLine.set(false);
    }

    @Override
    public void addLinesAndFooter() {
        this.entries.addFirst(this.getScoreboardLineInput(ChatColor.DARK_BLUE));

        if (Config.SCOREBOARD_FOOTER_ENABLED && this.lastLine.get()) {
            this.entries.pollLast();
            this.lastLine.set(false);
        }

        if (!this.lastLine.get()) {
            this.addFooter(Config.SCOREBOARD_FOOTER_PLACEHOLDER);

            if (!Config.SCOREBOARD_LINE_INVISIBLE) {
                this.entries.addLast(this.getScoreboardLineInput(ChatColor.DARK_GREEN));
            }
        }
    }

    private void addFooter(String footer) {
        if (!Config.SCOREBOARD_FOOTER_ENABLED) return;

        this.entries.addLast(new ScoreboardInput("", ChatColor.AQUA + ChatColor.RESET.toString(), ""));
        this.entries.addLast(this.splitInput(footer, ""));
    }

    /**
     * Splits a line into prefix(≤16) + entry(≤40) + suffix so it renders in full
     * even on legacy clients (1.8 caps team prefix/suffix at 16). The active color
     * is carried across the split so the text keeps its colour and never truncates.
     */
    private ScoreboardInput splitInput(String value, String time) {
        if (value == null) value = "";
        if (value.length() <= 16) {
            return new ScoreboardInput("", value, time);
        }

        int cut = 16;
        if (value.charAt(cut - 1) == '§') cut--; // don't split a § code in half

        String prefix = value.substring(0, cut);
        String rest   = value.substring(cut);
        String carry  = ChatColor.getLastColors(prefix);
        String name   = carry + rest;

        if (name.length() <= 40) {
            return new ScoreboardInput(prefix, name, time);
        }

        // Very long line: push the overflow (with carried colour) into the suffix.
        int nameCut = 40;
        if (name.charAt(nameCut - 1) == '§') nameCut--;
        String namePart = name.substring(0, nameCut);
        String overflow = ChatColor.getLastColors(namePart) + name.substring(nameCut);
        if (overflow.length() > 16) overflow = overflow.substring(0, 16);
        return new ScoreboardInput(prefix, namePart, overflow + time);
    }

    private ScoreboardInput getScoreboardLineInput(ChatColor color) {
        if (Config.SCOREBOARD_LINE_INVISIBLE) {
            return new ScoreboardInput("", color.toString(), "");
        } else {
            String line = Config.SCOREBOARD_LINE_COLOR + ChatColor.STRIKETHROUGH + "------";
            return new ScoreboardInput(line, color.toString() + line, "---------");
        }
    }

    private Team getTeam(String name) {
        synchronized (this.scoreboard) {
            Team team = this.scoreboard.getTeam(name);
            return team == null ? this.scoreboard.registerNewTeam(name) : team;
        }
    }

    @Override
    public void setUpdate(boolean value) {
        this.update.set(value);
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public void clear() {
        this.entries.clear();
    }

    @Override
    public void updateTabRelations(Iterable<? extends Player> players, boolean lunarOnly) {
        if (this.player == null || this.scoreboard == null || !AstroCore.getInstance().isFullyEnabled()) return;

        synchronized (this) {
            PlayerFaction playerFaction = FactionsManager.getInstance().getPlayerFaction(this.player);

            for (Player online : players) {
                List<String> nametag = null;

                if (Config.LUNAR_CLIENT_API_ENABLED && Config.LUNAR_CLIENT_API_NAMETAGS_ENABLED) {
                    nametag = new ArrayList<>();
                    PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(online);

                    if (faction != null) {
                        nametag.add(Config.LUNAR_CLIENT_API_NAMETAGS_FACTION
                            .replace("<faction>", faction.getName(this.player))
                            .replace("<dtr>", faction.getDtrString()));
                    }
                }

                if (AstroCore.getInstance().getVanishManager().isHeadMod(online)) {
                    this.addAndUpdate(online, nametag, this.headMod, lunarOnly);
                    continue;
                } else if (AstroCore.getInstance().getStaffModeManager().isInStaffMode(online)) {
                    this.addAndUpdate(online, nametag, this.staff, lunarOnly);
                    continue;
                } else if (this.player == online) {
                    this.addAndUpdate(online, nametag, this.members, lunarOnly);
                    continue;
                } else if (playerFaction == null) {
                    if (this.invis != null && online.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) {
                        this.addAndUpdate(online, nametag, this.invis, lunarOnly);
                    } else if (TimerManager.getInstance().getArcherTagTimer().isActive(online)) {
                        this.addAndUpdate(online, nametag, this.archers, lunarOnly);
                    } else if (AstroCore.getInstance().getSotwHandler().isUnderSotwProtection(online)) {
                        this.addAndUpdate(online, nametag, this.sotw, lunarOnly);
                    } else {
                        this.addAndUpdate(online, nametag, this.enemies, lunarOnly);
                    }
                    continue;
                }

                PlayerFaction targetFaction = FactionsManager.getInstance().getPlayerFaction(online);
                boolean isMemberOrAlly = playerFaction == targetFaction || playerFaction.isAlly(targetFaction);

                if (this.invis != null && online.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY) && !isMemberOrAlly) {
                    this.addAndUpdate(online, nametag, this.invis, lunarOnly);
                } else if (playerFaction.isFocusing(online) || playerFaction.isFocusing(targetFaction)) {
                    this.addAndUpdate(online, nametag, this.focused, lunarOnly);
                } else if (playerFaction == targetFaction) {
                    this.addAndUpdate(online, nametag, this.members, lunarOnly);
                } else if (playerFaction.isAlly(targetFaction)) {
                    this.addAndUpdate(online, nametag, this.allies, lunarOnly);
                } else if (TimerManager.getInstance().getArcherTagTimer().isActive(online)) {
                    this.addAndUpdate(online, nametag, this.archers, lunarOnly);
                } else if (AstroCore.getInstance().getSotwHandler().isUnderSotwProtection(online)) {
                    this.addAndUpdate(online, nametag, this.sotw, lunarOnly);
                } else {
                    this.addAndUpdate(online, nametag, this.enemies, lunarOnly);
                }
            }
        }
    }

    private void addAndUpdate(Player online, List<String> nametag, Team relTeam, boolean lunarOnly) {
        if (!lunarOnly) {
            // Per-player team: combines the relation color (prefix) and rank tag (suffix).
            // Name is "p_" + 4 hex chars of the online player's name hash — always ≤6 chars.
            String perName = "p_" + Integer.toHexString(online.getName().hashCode() & 0xFFFF);
            Team perTeam = this.scoreboard.getTeam(perName);
            if (perTeam == null) perTeam = this.scoreboard.registerNewTeam(perName);

            perTeam.setPrefix(relTeam.getPrefix());

            // Rank suffix: " §6[Fénix]"
            Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(online.getUniqueId());
            PlayerRank rank = (ud != null && ud.getRank() != null) ? ud.getRank() : PlayerRank.MORTAL;
            perTeam.setSuffix(" " + rank.getColor() + "[" + rank.getDisplayName() + "]");

            perTeam.addEntry(online.getName());
        }

        if (nametag != null) {
            nametag.add(relTeam.getPrefix() + online.getName());
            LunarClientAPI.getInstance().overrideNametag(online, nametag, this.player);
        }
    }
}

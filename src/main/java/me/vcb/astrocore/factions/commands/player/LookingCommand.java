package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LookingCommand extends SubCommand {

    private static final long COOLDOWN_MS = 60_000L * 3; // 3 min cooldown
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public LookingCommand() {
        super("looking", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(faction.getMember(player).getRole() != Role.LEADER && faction.getMember(player).getRole() != Role.CO_LEADER) {
            player.sendMessage(Color.translate(Language.FACTION_PREFIX + "&eDebés ser &clíder &eo &cco-líder &epara usar esto."));
            return;
        }

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(faction.getId());
        if(last != null && now - last < COOLDOWN_MS) {
            long remaining = COOLDOWN_MS - (now - last);
            player.sendMessage(Color.translate(Language.FACTION_PREFIX + "&ePodés usar esto en &c" + StringUtils.formatDurationWords(remaining) + "&e."));
            return;
        }

        cooldowns.put(faction.getId(), now);

        Messages.sendMessage(Color.translate(Language.FACTION_PREFIX
            + "&6[LFM] &e" + faction.getName()
            + " &eestá buscando miembros! &aUsá &b/f join " + faction.getName() + " &ao &b/f joinrandom&a."));
    }
}

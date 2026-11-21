package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.selection.SelectionType;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand extends SubCommand {

    public ClaimCommand() {
        super("claim", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CO_LEADER)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION.replace("<role>", Role.CO_LEADER.getName()));
            return;
        }

        int claimCount = faction.getClaims().size();

        if(claimCount >= Config.FACTION_MAX_CLAIMS) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_MAX_CLAIMS_EXCEEDED);
            return;
        }

        if(claimCount >= faction.getMembers().size() * Config.FACTION_CLAIMS_PER_PLAYER) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_MAX_CLAIMS_EXCEEDED);
            return;
        }

        if(player.getWorld().getEnvironment() != Environment.NORMAL) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_CAN_CLAIM_ONLY_IN_OVERWORLD);
            return;
        }

        if(player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_INVENTORY_FULL);
            return;
        }

        Location location = player.getLocation();
        int warzone = Config.WARZONE_RADIUS.get(Environment.NORMAL);

        if(Math.abs(location.getBlockX()) <= warzone && Math.abs(location.getBlockZ()) <= warzone) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_SPAWN_TOO_CLOSE
            .replace("<amount>", String.valueOf(warzone)));
            return;
        }

        AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.CLAIM, null);
        player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_CLAIMING_WAND_RECEIVED);
    }
}

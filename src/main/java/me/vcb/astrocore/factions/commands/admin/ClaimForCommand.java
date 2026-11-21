package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.factions.type.SystemFaction;
import me.vcb.astrocore.selection.Selection;
import me.vcb.astrocore.selection.SelectionType;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimForCommand extends SubCommand {

    public ClaimForCommand() {
        super("claimfor", "astrocore.factions.claimfor", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_USAGE);
            return;
        }

        Faction faction = FactionsManager.getInstance().getAnyFaction(args[0]);

        if(faction == null) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0]));
            return;
        }

        if(!(faction instanceof SystemFaction)) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_NOT_SYSTEM_FACTION);
            return;
        }

        Player player = (Player) sender;
        Selection selection = AstroCore.getInstance().getSelectionManager().getSelection(player);

        if(selection == null || selection.getType() != SelectionType.SYSTEM_CLAIM) {
            AstroCore.getInstance().getSelectionManager().toggleSelectionProcess(player, SelectionType.SYSTEM_CLAIM, null);
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_MAKE_A_SELECTION);
            return;
        }

        if(!selection.areBothPositionsSet()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_SET_BOTH_POSITIONS);
            return;
        }

        boolean isAfkPool = faction.getName().equalsIgnoreCase(me.vcb.astrocore.afk.AfkManager.FACTION_NAME);

        // AFKPool may be claimed over existing claims (e.g. spawn); others may not overlap.
        if(!isAfkPool && !ClaimManager.getInstance().getClaimsInSelection(selection.getPosOne(), selection.getPosTwo()).isEmpty()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_CLAIM_OVERLAPPING);
            return;
        }

        if(!ClaimManager.getInstance().addClaim(selection.toClaim(faction))) return;
        AstroCore.getInstance().getSelectionManager().removeSelectionProcess(player);

        if(isAfkPool && me.vcb.astrocore.afk.AfkManager.getInstance() != null) {
            org.bukkit.Location p1 = selection.getPosOne(), p2 = selection.getPosTwo();
            int minX = Math.min(p1.getBlockX(), p2.getBlockX()), maxX = Math.max(p1.getBlockX(), p2.getBlockX());
            int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ()), maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());
            me.vcb.astrocore.afk.AfkManager.getInstance().clearHolograms(p1.getWorld(), minX, maxX, minZ, maxZ);
            org.bukkit.Location center = new org.bukkit.Location(p1.getWorld(),
                (minX + maxX) / 2.0 + 0.5, Math.max(p1.getBlockY(), p2.getBlockY()) + 3.0, (minZ + maxZ) / 2.0 + 0.5);
            me.vcb.astrocore.afk.AfkManager.getInstance().spawnHologram(center);
        }

        Messages.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_CLAIM_FOR_CLAIM_CLAIMED
            .replace("<player>", player.getName())
            .replace("<faction>", faction.getDisplayName(sender)), "astrocore.staff");
    }
}

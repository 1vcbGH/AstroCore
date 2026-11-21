package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.claim.ClaimManager;
import org.bukkit.command.CommandSender;

public class SaveCommand extends SubCommand {

    public SaveCommand() {
        super("save", "astrocore.factions.save");

        this.setExecuteAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AstroCore.getInstance().log("&3===&b=============================================&3===");

        FactionsManager.getInstance().saveFactions(true, false);
        FactionsManager.getInstance().savePlayers(true);
        ClaimManager.getInstance().saveClaims(true);

        AstroCore.getInstance().log("&3===&b=============================================&3===");

        sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_SAVED);
    }
}

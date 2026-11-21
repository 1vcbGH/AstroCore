package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideStaffCommand extends BaseCommand {

    public HideStaffCommand() {
        super("hidestaff", "astrocore.hidestaff", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getVanishManager().toggleHideStaff(player);
    }
}

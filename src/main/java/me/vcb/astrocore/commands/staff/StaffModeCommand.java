package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class StaffModeCommand extends BaseCommand {

    public StaffModeCommand() {
        super("staffmode", Arrays.asList("staff", "mod"), "astrocore.staffmode");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            if(!this.checkConsoleSender(sender)) return;

            Player player = (Player) sender;
            AstroCore.getInstance().getStaffModeManager().toggleStaffMode(player);
            return;
        }

        if(!this.checkPermission(sender, "astrocore.staffmode.others")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        AstroCore.getInstance().getStaffModeManager().toggleStaffMode(target);

        this.sendChangedMessage(sender, target, AstroCore.getInstance().getStaffModeManager().isInStaffMode(target)
        ? Language.STAFF_MODE_ENABLED_OTHERS : Language.STAFF_MODE_DISABLED_OTHERS);
    }

    private void sendChangedMessage(CommandSender sender, Player target, String message) {
        Messages.sendMessage(Language.PREFIX + message.replace("<player>", sender.getName())
        .replace("<target>", target.getName()), "astrocore.staff");
    }
}

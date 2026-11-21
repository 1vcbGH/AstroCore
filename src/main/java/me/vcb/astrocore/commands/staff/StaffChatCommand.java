package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class StaffChatCommand extends BaseCommand {

    public StaffChatCommand() {
        super("staffchat", Arrays.asList("sc", "staffc", "schat"), "astrocore.staffchat", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            AstroCore.getInstance().getStaffChatHandler().toggleStaffChat(player);
            return;
        }

        String message = StringUtils.joinArray(args, " ", 1);

        Messages.sendMessage(Language.STAFF_CHAT_FORMAT
            .replace("<player>", player.getName())
            .replace("<message>", message), "astrocore.staffchat");
    }
}

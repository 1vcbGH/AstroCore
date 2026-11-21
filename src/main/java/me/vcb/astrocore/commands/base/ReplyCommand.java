package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ReplyCommand extends BaseCommand {

    public ReplyCommand() {
        super("reply", Collections.singletonList("r"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.REPLY_USAGE);
            return;
        }

        Player player = (Player) sender;

        String message = StringUtils.joinArray(args, " ", 1);
        AstroCore.getInstance().getMessagingHandler().sendReplyMessage(player, message);
    }
}

package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class BroadcastRawCommand extends BaseCommand {

    public BroadcastRawCommand() {
        super("broadcastraw", Collections.singletonList("bcraw"), "astrocore.broadcastraw");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.BROADCAST_RAW_USAGE);
            return;
        }

        Messages.sendMessage(Color.translate(StringUtils.joinArray(args, " ", 1)));
    }
}

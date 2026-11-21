package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class BroadcastCommand extends BaseCommand {

    public BroadcastCommand() {
        super("broadcast", Collections.singletonList("bc"), "astrocore.broadcast");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.BROADCAST_USAGE);
            return;
        }

        String message = Color.translate(StringUtils.joinArray(args, " ", 1));
        Messages.sendMessage(Language.BROADCAST_PREFIX + message);
    }
}

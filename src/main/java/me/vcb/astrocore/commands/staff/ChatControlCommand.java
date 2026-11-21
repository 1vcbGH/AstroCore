package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;

public class ChatControlCommand extends BaseCommand {

    public ChatControlCommand() {
        super("chat", "astrocore.chatcontrol");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Language.CHAT_USAGE.forEach(sender::sendMessage);
            return;
        }

        if(args.length == 1) {
            switch(args[0].toLowerCase()) {
                case "mute": {
                    AstroCore.getInstance().getChatControlHandler().toggleChat(sender);
                    return;
                }
                case "clear": {
                    AstroCore.getInstance().getChatControlHandler().clearChat(sender);
                    return;
                }
                default: {
                    Language.CHAT_USAGE.forEach(sender::sendMessage);
                    return;
                }
            }
        }

        switch(args[0].toLowerCase()) {
            case "delay": {
                AstroCore.getInstance().getChatControlHandler().setDelay(sender, args[1]);
                return;
            }
            case "toggle": {
                if(args[1].equalsIgnoreCase("foundore")) {
                    AstroCore.getInstance().getChatControlHandler().toggleFoundOreMessages(sender);
                    return;
                }

                Language.CHAT_USAGE.forEach(sender::sendMessage);
                return;
            }
            default: Language.CHAT_USAGE.forEach(sender::sendMessage);
        }
    }
}

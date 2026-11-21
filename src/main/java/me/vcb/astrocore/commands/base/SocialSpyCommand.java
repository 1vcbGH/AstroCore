package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class SocialSpyCommand extends BaseCommand {

    public SocialSpyCommand() {
        super("socialspy", Collections.singletonList("chatspy"), "astrocore.socialspy", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        AstroCore.getInstance().getMessagingHandler().toggleSocialSpy(player);
    }
}

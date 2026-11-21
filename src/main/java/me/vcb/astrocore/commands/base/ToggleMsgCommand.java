package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ToggleMsgCommand extends BaseCommand {

    public ToggleMsgCommand() {
        super("togglemessages", Arrays.asList("togglemsg", "tmsg", "togglepm", "tpm"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        data.getSettings().setMessages(!data.getSettings().isMessages());

        player.sendMessage(Language.PREFIX + (data.getSettings().isMessages()
            ? Language.TOGGLE_MESSAGES_TOGGLED_ON : Language.TOGGLE_MESSAGES_TOGGLED_OFF));
    }
}

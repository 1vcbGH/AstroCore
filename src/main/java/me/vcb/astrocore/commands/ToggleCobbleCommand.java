package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ToggleCobbleCommand extends BaseCommand {

    public ToggleCobbleCommand() {
        super("togglecobble", Arrays.asList("tcobble", "cobble"), "astrocore.cobble", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        data.getSettings().setCobble(!data.getSettings().isCobble());

        player.sendMessage(Language.PREFIX + (data.getSettings().isCobble()
            ? Language.TOGGLE_COBBLE_TOGGLED_ON : Language.TOGGLE_COBBLE_TOGGLED_OFF));
    }
}

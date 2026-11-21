package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ToggleSoundsCommand extends BaseCommand {

    public ToggleSoundsCommand() {
        super("togglesounds", Collections.singletonList("tsounds"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        data.getSettings().setSounds(!data.getSettings().isSounds());

        player.sendMessage(Language.PREFIX + (data.getSettings().isSounds()
            ? Language.TOGGLE_SOUNDS_TOGGLED_ON : Language.TOGGLE_SOUNDS_TOGGLED_OFF));
    }
}

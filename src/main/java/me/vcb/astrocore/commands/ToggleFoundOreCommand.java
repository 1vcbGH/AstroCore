package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ToggleFoundOreCommand extends BaseCommand {

    public ToggleFoundOreCommand() {
        super("togglefoundore", Collections.singletonList("tfoundore"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        data.getSettings().setFoundOre(!data.getSettings().isFoundOre());

        player.sendMessage(Language.PREFIX + (data.getSettings().isFoundOre()
            ? Language.TOGGLE_FOUND_ORE_TOGGLED_ON : Language.TOGGLE_FOUND_ORE_TOGGLED_OFF));
    }
}

package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand extends BaseCommand {

    public BackCommand() {
        super("back", "astrocore.back", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);

        if(data.getLastLocation() == null) {
            player.sendMessage(Language.PREFIX + Language.BACK_NO_LOCATION);
            return;
        }

        if(!player.teleport(data.getLastLocation())) return;
        player.sendMessage(Language.PREFIX + Language.BACK_TELEPORTED);
    }
}

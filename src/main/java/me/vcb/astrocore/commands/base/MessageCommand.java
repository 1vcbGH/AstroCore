package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MessageCommand extends BaseCommand {

    public MessageCommand() {
        super("message", Arrays.asList("msg", "m", "tell"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length <= 1) {
            sender.sendMessage(Language.PREFIX + Language.MESSAGE_USAGE);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        Player player = (Player) sender;

        if(!player.hasPermission("astrocore.staff") && AstroCore.getInstance().getVanishManager().isVanished(target)) {
            player.sendMessage(Language.PREFIX + Language.COMMANDS_PLAYER_NOT_ONLINE.replace("<player>", target.getName()));
            return;
        }

        AstroCore.getInstance().getMessagingHandler().sendMessage(player, target, StringUtils.joinArray(args, " ", 2));
    }
}

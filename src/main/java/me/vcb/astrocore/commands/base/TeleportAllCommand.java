package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class TeleportAllCommand extends BaseCommand {

    public TeleportAllCommand() {
        super("teleportall", Collections.singletonList("tpall"), "astrocore.teleportall", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Bukkit.getOnlinePlayers().stream().filter(online -> online != player).forEach(online -> {
            if(!online.teleport(player)) return;
            online.sendMessage(Language.PREFIX + Language.TELEPORT_MESSAGE.replace("<player>", player.getName()));
        });

        player.sendMessage(Language.PREFIX + Language.TELEPORT_ALL_MESSAGE);
    }
}

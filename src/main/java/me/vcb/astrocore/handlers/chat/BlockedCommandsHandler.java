package me.vcb.astrocore.handlers.chat;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.handlers.manager.Handler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BlockedCommandsHandler extends Handler implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("astrocore.blockedcommands.bypass")) return;

        String[] args = event.getMessage().split(" ", 2);

        if(args[0].contains(":") && Config.BLOCKED_COMMANDS_DISABLE_COLON) {
            event.setCancelled(true);
            player.sendMessage(Language.PREFIX + Language.DISABLED_COMMANDS_COLON_DISABLED);
            return;
        }

        if(Config.BLOCKED_COMMANDS.stream().noneMatch(command -> args[0].equalsIgnoreCase(command))) return;

        event.setCancelled(true);
        player.sendMessage(Language.PREFIX + Language.DISABLED_COMMANDS_MESSAGE);
    }
}

package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.factions.FactionsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ChatSpyCommand extends SubCommand {

    public ChatSpyCommand() {
        super("chatspy", Collections.singletonList("socialspy"), "astrocore.factions.chatspy", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        FactionsManager.getInstance().toggleChatSpy(player);
    }
}

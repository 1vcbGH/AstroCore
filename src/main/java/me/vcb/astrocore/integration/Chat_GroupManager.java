package me.vcb.astrocore.integration;

import me.vcb.astrocore.handlers.chat.ChatHandler;
import org.bukkit.entity.Player;

public class Chat_GroupManager extends ChatHandler {

    @Override
    public String getRankName(Player player) { return ""; }

    @Override
    public String getPrefix(Player player) { return ""; }

    @Override
    protected String getSuffix(Player player) { return ""; }
}

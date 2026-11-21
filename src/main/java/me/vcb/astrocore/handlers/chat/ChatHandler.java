package me.vcb.astrocore.handlers.chat;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.integration.*;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;

public class ChatHandler extends Handler implements Listener {

    @Getter public static ChatHandler instance;

    public static void setup() {
        if(Bukkit.getPluginManager().isPluginEnabled("AquaCore")) {
            instance = new Chat_AquaCore();
        } else if(Bukkit.getPluginManager().isPluginEnabled("Chronium")) {
            instance = new Chat_Chronium();
        } else if(Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            instance = new Chat_GroupManager();
        } else if(Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            instance = new Chat_LuckPerms();
        } else if(Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            instance = new Chat_PermissionsEx();
        } else {
            instance = new ChatHandler();
        }
    }

    public String getRankName(Player player) {
        return "";
    }

    protected String getTag(Player player) {
        return "";
    }

    public String getPrefix(Player player) {
        return "";
    }

    public String getNameColor(Player player) {
        return "";
    }

    protected String getSuffix(Player player) {
        return "";
    }

    protected String getChatColor(Player player) {
        return "";
    }

    private String getPlayerDisplayName(Player player) {
        String chatTag = buildChatTag(player);

        // Si el jugador está disfrazado, usar el nick falso
        me.vcb.astrocore.disguise.DisguiseManager disguiseManager = AstroCore.getInstance().getDisguiseManager();
        String displayedName = (disguiseManager != null && disguiseManager.isDisguised(player))
            ? disguiseManager.getFakeName(player)
            : player.getName();

        if(Config.CHAT_FORMAT_USE_PLAYER_DISPLAY_NAME) {
            return player.getDisplayName() + chatTag;
        }

        return Color.translate(instance.getTag(player) + instance.getPrefix(player)
            + instance.getNameColor(player) + displayedName + instance.getSuffix(player) + chatTag);
    }

    private String buildChatTag(Player player) {
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if(ud == null) return "";

        // No tag equipped
        if(ud.isTagHidden()) return "";

        me.vcb.astrocore.tags.ChatTag active = ud.getActiveTag();
        if(active == null) return "";

        // RANGO tag: show top-3 position, or nothing if not in top 3
        if(active == me.vcb.astrocore.tags.ChatTag.RANGO) {
            if(me.vcb.astrocore.rank.RankManager.getInstance() == null) return "";
            return me.vcb.astrocore.rank.RankManager.getInstance().getChatTag(player.getUniqueId());
        }

        // Purchased cosmetic tag
        return " " + active.getDisplay();
    }

    private String getChatMessage(PlayerFaction playerFaction, CommandSender recipient,
                                  String displayName, String chatColor, String message) {

        if(playerFaction == null) {
            return Config.CHAT_FORMAT.replace("<displayName>", displayName) + chatColor + message;
        }

        return Config.CHAT_FORMAT_WITH_FACTION
            .replace("<faction>", playerFaction.getName(recipient))
            .replace("<displayName>", displayName) + chatColor + message;
    }

    public static String getPlayerChatColor(Player player) {
        me.vcb.astrocore.userdata.Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (ud == null || ud.getSelectedChatColor() == null) return "";
        me.vcb.astrocore.coinshop.ChatColorType ct = me.vcb.astrocore.coinshop.ChatColorType.fromId(ud.getSelectedChatColor());
        return ct != null ? ct.getColorCode() : "";
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(!Config.CHAT_FORMAT_ENABLED) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        String displayName = this.getPlayerDisplayName(player);
        String chatColorRaw = Color.translate(instance.getChatColor(player));
        if (chatColorRaw.isEmpty()) chatColorRaw = me.vcb.astrocore.handlers.chat.ChatHandler.getPlayerChatColor(player);
        final String chatColor = chatColorRaw;

        String message = player.hasPermission("astrocore.chat.color")
            ? Color.translate(event.getMessage())
            : event.getMessage();

        Bukkit.getConsoleSender().sendMessage(this.getChatMessage(faction,
            Bukkit.getConsoleSender(), displayName, chatColor, message));

        event.getRecipients().forEach(recipient -> {
            Userdata userdata = AstroCore.getInstance().getUserdataManager().getUserdata(recipient);
            if(userdata == null) return;

            if((player != recipient && !player.hasPermission("astrocore.staff") && !userdata
                .getSettings().isPublicChat()) || userdata.isIgnoring(player)) return;

            recipient.sendMessage(this.getChatMessage(faction, recipient, displayName, chatColor, message));
        });
    }
}

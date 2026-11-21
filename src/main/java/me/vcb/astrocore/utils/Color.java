package me.vcb.astrocore.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {

    public static ChatColor[] CHAT_COLOR_CACHE = ChatColor.values();

    // Matches &#RRGGBB, #RRGGBB and <#RRGGBB>
    private static final Pattern HEX_PATTERN = Pattern.compile("(?:&|<)?#([A-Fa-f0-9]{6})>?");

    public static String translate(String line) {
        if (line == null) return null;

        // Convert hex codes to the legacy §x§R§R§G§G§B§B sequence first.
        Matcher matcher = HEX_PATTERN.matcher(line);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb,
                Matcher.quoteReplacement(net.md_5.bungee.api.ChatColor.of("#" + hex).toString()));
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static String strip(String line) {
        return ChatColor.stripColor(line);
    }
}

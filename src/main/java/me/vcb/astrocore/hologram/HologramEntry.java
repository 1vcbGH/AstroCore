package me.vcb.astrocore.hologram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
@AllArgsConstructor
public class HologramEntry {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    private int entityId;
    private String message;
    private Location location;

    private transient UUID displayUUID;

    public HologramEntry(int entityId, String message, Location location) {
        this.entityId = entityId;
        this.message = message;
        this.location = location;
    }

    public TextDisplay getDisplay() {
        if (this.displayUUID == null) return null;
        return (TextDisplay) Bukkit.getEntity(this.displayUUID);
    }

    public void spawnDisplay() {
        if (this.location == null || this.location.getWorld() == null) return;
        if (this.message == null) return;

        TextDisplay existing = this.getDisplay();
        if (existing != null && !existing.isDead()) existing.remove();

        Component component = buildComponent(this.message);

        TextDisplay display = this.location.getWorld().spawn(this.location, TextDisplay.class, entity -> {
            entity.text(component);
            entity.setVisibleByDefault(false);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(false);
            entity.setPersistent(false);
            entity.setShadowed(true);
            entity.setLineWidth(1000);
            entity.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // fully transparent
            entity.setTextOpacity((byte) 255);
        });

        this.displayUUID = display.getUniqueId();
    }

    public void updateMessage(String newMessage) {
        this.message = newMessage;
        TextDisplay display = this.getDisplay();
        if (display != null && !display.isDead()) {
            display.text(buildComponent(newMessage));
        }
    }

    public void removeDisplay() {
        TextDisplay display = this.getDisplay();
        if (display != null && !display.isDead()) {
            display.remove();
        }
        this.displayUUID = null;
    }

    public static Component buildComponent(String text) {
        String translated = translateHex(text);
        translated = org.bukkit.ChatColor.translateAlternateColorCodes('&', translated);
        return LegacyComponentSerializer.legacySection()
            .deserialize(translated)
            .decoration(TextDecoration.ITALIC, false);
    }

    // Converts &#RRGGBB → §x§R§R§G§G§B§B
    public static String translateHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

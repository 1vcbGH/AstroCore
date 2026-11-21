package me.vcb.astrocore.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

/** Custom textured player heads + a text progress bar, for prettier GUIs. */
public final class Heads {

    private Heads() {}

    // ── Decorative head textures (base64) ──────────────────────────────────
    public static final String CHEST =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvNmY2OGQ1MDliNWQxNjY5Yjk3MWRkMWQ0ZGYyZTQ3ZTE5YmNiMWIzM2Jm" +
        "MWE3ZmYxZGRhMjliZmM2ZjllYmYifX19";
    public static final String BEACON =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvOWRiZGFhNzU1MDk5ZWRkN2VmYTFmMTI4ODJjN2E1MWI1ODE1ZGI1MmUw" +
        "YjE2NGFlZjZkZjlhMWY1M2VjYTIzIn19fQ==";
    public static final String ARROW_LEFT =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5" +
        "OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==";
    public static final String RED_X =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQu" +
        "bmV0L3RleHR1cmUvMTJkN2E3NTFlYjA3MWUwOGRiYmM5NWJjNWQ5ZDY2ZTVmNTFkYzY3MTI2" +
        "NDBhZDJkZmEwM2RlZmJiNjhhN2YzYSJ9fX0=";

    /** Builds a textured head. Invalid textures simply render as a default head. */
    public static ItemStack skull(String base64, String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        } catch (Throwable ignored) {}
        if (name != null) meta.setDisplayName(Color.translate(name));
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /** Renders a colored progress bar, e.g. §a▰▰▰§7▱▱▱▱▱. */
    public static String progressBar(int current, int target, int length, String doneColor, String leftColor) {
        if (target <= 0) target = 1;
        int filled = (int) Math.round(((double) Math.min(current, target) / target) * length);
        StringBuilder sb = new StringBuilder(Color.translate(doneColor));
        for (int i = 0; i < length; i++) {
            if (i == filled) sb.append(Color.translate(leftColor));
            sb.append('▰');
        }
        return sb.toString();
    }
}

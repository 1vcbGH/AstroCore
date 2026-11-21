package me.vcb.astrocore.npc;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
public class NpcData {

    private final String id;
    private String displayName;         // shown above head via hologram
    private String skinName;            // player name whose skin to use
    private String skinTexture;         // base64 texture value (cached)
    private String skinSignature;       // base64 signature (cached)
    private Location location;
    private List<String> commands;      // run on click (%player% placeholder)

    // Equipment (serialized as Base64)
    private String helmetBase64;
    private String chestplateBase64;
    private String leggingsBase64;
    private String bootsBase64;
    private String mainHandBase64;
    private String offHandBase64;

    public NpcData(String id) {
        this.id       = id;
        this.displayName = "&f" + id;
        this.skinName    = "Steve";
        this.commands    = new ArrayList<>();
    }

    // ── Equipment helpers ─────────────────────────────────────────────────────

    public ItemStack getHelmet()     { return decode(helmetBase64); }
    public ItemStack getChestplate() { return decode(chestplateBase64); }
    public ItemStack getLeggings()   { return decode(leggingsBase64); }
    public ItemStack getBoots()      { return decode(bootsBase64); }
    public ItemStack getMainHand()   { return decode(mainHandBase64); }
    public ItemStack getOffHand()    { return decode(offHandBase64); }

    public void setHelmet(ItemStack i)     { helmetBase64     = encode(i); }
    public void setChestplate(ItemStack i) { chestplateBase64 = encode(i); }
    public void setLeggings(ItemStack i)   { leggingsBase64   = encode(i); }
    public void setBoots(ItemStack i)      { bootsBase64      = encode(i); }
    public void setMainHand(ItemStack i)   { mainHandBase64   = encode(i); }
    public void setOffHand(ItemStack i)    { offHandBase64    = encode(i); }

    private static String encode(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    private static ItemStack decode(String b64) {
        if (b64 == null) return null;
        try { return ItemStack.deserializeBytes(Base64.getDecoder().decode(b64)); }
        catch (Exception e) { return null; }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save(ConfigurationSection s) {
        s.set("display-name",   displayName);
        s.set("skin-name",      skinName);
        s.set("skin-texture",   skinTexture);
        s.set("skin-signature", skinSignature);
        s.set("commands",       commands);
        s.set("equipment.helmet",      helmetBase64);
        s.set("equipment.chestplate",  chestplateBase64);
        s.set("equipment.leggings",    leggingsBase64);
        s.set("equipment.boots",       bootsBase64);
        s.set("equipment.main-hand",   mainHandBase64);
        s.set("equipment.off-hand",    offHandBase64);
        if (location != null && location.getWorld() != null) {
            s.set("location.world", location.getWorld().getName());
            s.set("location.x",     location.getX());
            s.set("location.y",     location.getY());
            s.set("location.z",     location.getZ());
            s.set("location.yaw",   location.getYaw());
            s.set("location.pitch", location.getPitch());
        }
    }

    public static NpcData load(String id, ConfigurationSection s) {
        NpcData d = new NpcData(id);
        d.displayName  = s.getString("display-name", d.displayName);
        d.skinName     = s.getString("skin-name", "Steve");
        d.skinTexture  = s.getString("skin-texture");
        d.skinSignature= s.getString("skin-signature");
        d.commands     = new ArrayList<>(s.getStringList("commands"));
        ConfigurationSection eq = s.getConfigurationSection("equipment");
        if (eq != null) {
            d.helmetBase64     = eq.getString("helmet");
            d.chestplateBase64 = eq.getString("chestplate");
            d.leggingsBase64   = eq.getString("leggings");
            d.bootsBase64      = eq.getString("boots");
            d.mainHandBase64   = eq.getString("main-hand");
            d.offHandBase64    = eq.getString("off-hand");
        }
        ConfigurationSection loc = s.getConfigurationSection("location");
        if (loc != null) {
            World world = Bukkit.getWorld(loc.getString("world", "world"));
            if (world != null) {
                d.location = new Location(world,
                    loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z"),
                    (float) loc.getDouble("yaw"), (float) loc.getDouble("pitch"));
            }
        }
        return d;
    }
}

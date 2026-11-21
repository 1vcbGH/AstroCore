package me.vcb.astrocore.hologram;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Getter
public abstract class Hologram {

    protected int id;
    protected Location location;
    private String displayItemBase64;

    protected transient Set<UUID> viewers;
    protected transient List<HologramEntry> entries;
    private transient org.bukkit.entity.ItemDisplay itemDisplayEntity;
    private transient float itemDisplayRotation = 0f;

    public Hologram() {
        this.viewers = new HashSet<>();
        this.entries = new ArrayList<>();
    }

    public Hologram(int id, Location location) {
        this.id = id;
        this.location = location;
        this.viewers = new HashSet<>();
        this.entries = new ArrayList<>();
    }

    public void initTransient() {
        if (this.viewers == null) this.viewers = new HashSet<>();
        if (this.entries == null) this.entries = new ArrayList<>();
    }

    public abstract void createHologramLines();

    // -----------------------------------------------------------------------
    // Entry management
    // -----------------------------------------------------------------------

    public int getEntityId() {
        return ThreadLocalRandom.current().nextInt(100_000_000, 200_000_000);
    }

    public Location getLineLocation(Location parent) {
        return parent.clone().subtract(0, 0.3, 0);
    }

    public void addEntry(String message, Location location) {
        HologramEntry entry = new HologramEntry(this.getEntityId(), message, location);
        entry.spawnDisplay();
        this.entries.add(entry);
    }

    public void addEntry(int index, String message, Location location) {
        HologramEntry entry = new HologramEntry(this.getEntityId(), message, location);
        entry.spawnDisplay();
        this.entries.add(index, entry);
    }

    public HologramEntry getEntry(int index) {
        return this.entries.get(index);
    }

    public void removeEntry(int index) {
        HologramEntry entry = this.entries.remove(index);
        if (entry != null) entry.removeDisplay();
    }

    public void updateMessage(int index, String message) {
        HologramEntry entry = this.entries.get(index);
        entry.setMessage(message);
        entry.updateMessage(message);
    }

    public void decrementId() {
        this.id--;
    }

    // -----------------------------------------------------------------------
    // Show / hide
    // -----------------------------------------------------------------------

    public void sendHologram(Player player) {
        for (HologramEntry entry : this.entries) {
            if (entry.getMessage() == null) continue;
            TextDisplay display = entry.getDisplay();
            if (display == null || display.isDead()) {
                // Respawn and immediately show to this player
                Tasks.sync(() -> {
                    entry.spawnDisplay();
                    TextDisplay respawned = entry.getDisplay();
                    if (respawned != null && !respawned.isDead()) {
                        player.showEntity(AstroCore.getInstance(), respawned);
                    }
                });
                continue;
            }
            player.showEntity(AstroCore.getInstance(), display);
        }
    }

    public void removeHologram(Player player) {
        for (HologramEntry entry : this.entries) {
            TextDisplay display = entry.getDisplay();
            if (display != null && !display.isDead()) {
                player.hideEntity(AstroCore.getInstance(), display);
            }
        }
    }

    public void teleportHologram(Player player) {
        Location loc = player.getEyeLocation().clone();
        this.location = loc;
        for (HologramEntry entry : this.entries) {
            loc = this.getLineLocation(loc);
            entry.setLocation(loc);
            TextDisplay display = entry.getDisplay();
            if (display != null && !display.isDead() && !entry.getMessage().isEmpty()) {
                display.teleport(loc);
            }
        }
    }

    public void refreshForViewers() {
        this.forEachViewer(viewer -> {
            this.removeHologram(viewer);
            this.sendHologram(viewer);
        });
    }

    public void forEachViewer(Consumer<Player> action) {
        for (UUID viewer : this.viewers) {
            Player player = org.bukkit.Bukkit.getPlayer(viewer);
            if (player != null) action.accept(player);
        }
    }

    public boolean isInSameWorld(Player player) {
        return player.getWorld().getName().equals(this.location.getWorld().getName());
    }

    // -----------------------------------------------------------------------
    // Item display
    // -----------------------------------------------------------------------

    public org.bukkit.inventory.ItemStack getDisplayItem() {
        if (this.displayItemBase64 == null) return null;
        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(this.displayItemBase64);
            return org.bukkit.inventory.ItemStack.deserializeBytes(bytes);
        } catch (Exception e) { return null; }
    }

    public void setDisplayItem(org.bukkit.inventory.ItemStack item) {
        if (item == null) {
            this.displayItemBase64 = null;
        } else {
            this.displayItemBase64 = java.util.Base64.getEncoder().encodeToString(item.serializeAsBytes());
        }
    }

    public void spawnItemDisplay() {
        this.removeItemDisplay();
        org.bukkit.inventory.ItemStack item = this.getDisplayItem();
        if (item == null) return;
        if (this.location == null || this.location.getWorld() == null) return;

        org.bukkit.Location itemLoc = this.location.clone().add(0, 1.0, 0);
        this.itemDisplayEntity = itemLoc.getWorld().spawn(itemLoc, org.bukkit.entity.ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setTransformation(new org.bukkit.util.Transformation(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.AxisAngle4f(0, 0, 0, 1),
                new org.joml.Vector3f(0.7f, 0.7f, 0.7f),
                new org.joml.AxisAngle4f(0, 0, 0, 1)
            ));
        });
    }

    public void removeItemDisplay() {
        if (this.itemDisplayEntity != null && !this.itemDisplayEntity.isDead()) {
            this.itemDisplayEntity.remove();
        }
        this.itemDisplayEntity = null;
    }

    public void tickItemDisplayRotation() {
        if (this.itemDisplayEntity == null || this.itemDisplayEntity.isDead()) return;
        this.itemDisplayRotation += 3f;
        if (this.itemDisplayRotation >= 360f) this.itemDisplayRotation = 0f;
        float rad = (float) Math.toRadians(this.itemDisplayRotation);
        this.itemDisplayEntity.setTransformation(new org.bukkit.util.Transformation(
            new org.joml.Vector3f(0, 0, 0),
            new org.joml.AxisAngle4f(rad, 0, 1, 0),
            new org.joml.Vector3f(0.7f, 0.7f, 0.7f),
            new org.joml.AxisAngle4f(0, 0, 0, 1)
        ));
    }

    public void sendItemDisplay(Player player) {
        if (this.itemDisplayEntity != null && !this.itemDisplayEntity.isDead()) {
            player.showEntity(AstroCore.getInstance(), this.itemDisplayEntity);
        }
    }

    public void hideItemDisplay(Player player) {
        if (this.itemDisplayEntity != null && !this.itemDisplayEntity.isDead()) {
            player.hideEntity(AstroCore.getInstance(), this.itemDisplayEntity);
        }
    }
}

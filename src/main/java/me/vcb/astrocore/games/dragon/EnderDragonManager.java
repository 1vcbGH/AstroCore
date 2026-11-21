package me.vcb.astrocore.games.dragon;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.loot.LootData;
import me.vcb.astrocore.utils.LocationUtils;
import me.vcb.astrocore.utils.ManagerEnabler;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EnderDragonManager implements Listener, ManagerEnabler {

    private EnderDragon enderDragon;
    @Getter private Location spawnLocation;

    public EnderDragonManager() {
        this.spawnLocation = LocationUtils.stringToLocation(AstroCore.getInstance()
            .getUtilitiesFile().getString("ENDER_DRAGON_SPAWN"));

        NmsUtils.getInstance().registerEnderDragon();
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void disable() {
        if(this.isActive()) this.stopEnderDragon(true);
    }

    public boolean isActive() {
        return this.enderDragon != null;
    }

    public void startEnderDragon(CommandSender sender) {
        if(this.spawnLocation == null) {
            sender.sendMessage(Language.ENDER_DRAGON_PREFIX + Language.ENDER_DRAGON_EXCEPTION_SPAWN_NOT_SET);
            return;
        }

        LootData loot = AstroCore.getInstance().getLootManager().getLootByName("EnderDragon");
        this.enderDragon = NmsUtils.getInstance().spawnEnderDragon(this.spawnLocation, loot);

        Language.ENDER_DRAGON_START_STARTED.forEach(line -> Messages.sendMessage(Placeholder
            .EnderDragonReplacer.parse(this, line)));
    }

    public void stopEnderDragon(boolean removeEntity) {
        if(removeEntity) this.enderDragon.getDragonBukkitEntity().remove();

        this.enderDragon.disable();
        this.enderDragon = null;
    }

    public void changeDragonHealth(int amount) {
        if(this.enderDragon.getDragonMaxHealth() < amount) {
            this.enderDragon.setDragonMaxHealth(amount);
        }

        this.enderDragon.setDragonHealth(amount);
    }

    public void setSpawnLocation(Player player) {
        this.spawnLocation = player.getLocation();

        ConfigFile utilities = AstroCore.getInstance().getUtilitiesFile();
        utilities.set("ENDER_DRAGON_SPAWN", LocationUtils.locationToString(this.spawnLocation));
        utilities.save();
    }

    public void teleportToDragon(Player player) {
        player.teleport(this.enderDragon.getDragonBukkitEntity());
    }

    public String getScoreboardEntry() {
        return (int) this.enderDragon.getDragonHealth() + "/" + (int) this.enderDragon.getDragonMaxHealth();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityCreatePortal(EntityCreatePortalEvent event) {
        if(event.getPortalType() != PortalType.ENDER) return;
        if(event.getEntity().getType() != EntityType.ENDER_DRAGON) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if(!this.isActive()) return;

        Entity entity = this.enderDragon.getDragonBukkitEntity();
        Location location = entity.getLocation();

        int x = location.getBlockX() >> 4;
        int z = location.getBlockZ() >> 4;

        Chunk chunk = event.getChunk();

        if(chunk.getX() == x && chunk.getZ() == z) {
            chunk.setForceLoaded(true);
        }
    }
}

package me.vcb.astrocore.games.mountain;

import lombok.Getter;
import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.claim.Claim;
import me.vcb.astrocore.factions.claim.ClaimManager;
import me.vcb.astrocore.games.Cuboid;
import me.vcb.astrocore.games.Placeholder;
import me.vcb.astrocore.games.mountain.task.MountainRespawnTask;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.GsonUtils;
import me.vcb.astrocore.utils.ManagerEnabler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MountainManager implements ManagerEnabler {

    @Getter private List<MountainData> mountains;
    private final File mountainsFile;

    private final MountainRespawnTask pasteTask;

    public MountainManager() {
        this.mountainsFile = FileUtils.getOrCreateFile(Config.GAMES_DIR, "mountains.json");

        this.loadMountains();
        this.pasteTask = new MountainRespawnTask(this);
    }

    public void disable() {
        this.saveMountains();

        this.mountains.clear();
        this.pasteTask.cancel();
    }

    private void loadMountains() {
        String content = FileUtils.readWholeFile(this.mountainsFile);

        if(content == null) {
            this.mountains = new ArrayList<>();
            return;
        }

        this.mountains = AstroCore.getInstance().getGson().fromJson(content, GsonUtils.MOUNTAIN_TYPE);
        this.mountains.removeIf(mountain -> mountain.getFaction() == null);

        this.mountains.forEach(mountain -> Bukkit.getPluginManager().registerEvents(mountain, AstroCore.getInstance()));
    }

    private void saveMountains() {
        if(this.mountains == null) return;

        FileUtils.writeString(this.mountainsFile, AstroCore.getInstance().getGson()
            .toJson(this.mountains, GsonUtils.MOUNTAIN_TYPE));
    }

    public MountainData getMountain(int id) {
        return this.mountains.stream().filter(mountain -> mountain.getId() == id).findFirst().orElse(null);
    }

    public int createMountain(MountainType type, UUID factionId, Cuboid cuboid) {
        int id = this.mountains.size() + 1;

        MountainData mountainData = new MountainData(id, type, factionId, cuboid);
        this.mountains.add(mountainData);

        Bukkit.getPluginManager().registerEvents(mountainData, AstroCore.getInstance());
        return id;
    }

    public void deleteMountain(MountainData mountain) {
        Claim claim = ClaimManager.getInstance().getClaimAt(mountain.getCuboid().getCenter());
        if(claim != null && !ClaimManager.getInstance().removeClaim(claim)) return;

        this.mountains.remove(mountain);
        this.mountains.stream().filter(m -> m.getId() > mountain.getId()).forEach(m -> m.setId(m.getId() - 1));

        HandlerList.unregisterAll(mountain);
    }

    public String nextRespawnString() {
        return this.mountains.isEmpty() ? Language.MOUNTAIN_PREFIX + Language
        .MOUNTAIN_LIST_NO_MOUNTAINS : this.pasteTask.getNextRespawnString();
    }

    public void listMountains(CommandSender sender) {
        if(this.mountains.isEmpty()) {
            sender.sendMessage(Language.MOUNTAIN_PREFIX + Language.MOUNTAIN_LIST_NO_MOUNTAINS);
            return;
        }

        String format = sender.hasPermission("astrocore.mountain.admin") ? Language
        .MOUNTAIN_LIST_ADMIN_FORMAT : Language.MOUNTAIN_LIST_PLAYER_FORMAT;

        sender.sendMessage(Language.MOUNTAIN_COMMAND_HEADER);
        sender.sendMessage(Language.MOUNTAIN_LIST_TITLE);

        this.mountains.stream().sorted(Comparator.comparing(MountainData::getType)).forEach(
        mountain -> sender.sendMessage(Placeholder.MountainReplacer.parse(mountain, format)));

        sender.sendMessage(Language.MOUNTAIN_COMMAND_FOOTER);
    }
}

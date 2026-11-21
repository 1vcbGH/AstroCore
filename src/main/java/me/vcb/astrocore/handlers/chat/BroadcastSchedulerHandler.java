package me.vcb.astrocore.handlers.chat;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BroadcastSchedulerHandler extends Handler {

    private BukkitTask task;
    private int index = 0;
    private List<String> messages = new ArrayList<>();
    private int intervalSeconds = 300; // 5 min default

    @Override
    public void disable() {
        if(task != null) task.cancel();
    }

    public void load() {
        File file = new File(AstroCore.getInstance().getDataFolder(), "broadcasts.yml");
        if(!file.exists()) {
            AstroCore.getInstance().saveResource("broadcasts.yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        this.intervalSeconds = cfg.getInt("interval-seconds", 300);
        this.messages = cfg.getStringList("messages");
        reschedule();
    }

    public void reschedule() {
        if(task != null) task.cancel();
        if(messages.isEmpty()) return;
        long ticks = 20L * intervalSeconds;
        task = Bukkit.getScheduler().runTaskTimer(AstroCore.getInstance(), this::broadcastNext, ticks, ticks);
    }

    private void broadcastNext() {
        if(messages.isEmpty()) return;
        String msg = messages.get(index % messages.size());
        index++;
        Bukkit.broadcastMessage(Color.translate(msg));
    }

    public void reload() {
        load();
    }

    public List<String> getMessages() { return messages; }
    public int getIntervalSeconds() { return intervalSeconds; }
}

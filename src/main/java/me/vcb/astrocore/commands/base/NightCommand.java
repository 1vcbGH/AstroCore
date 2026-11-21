package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;

public class NightCommand extends BaseCommand {

    public NightCommand() {
        super("night", "astrocore.night");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Bukkit.getWorlds().stream()
            .filter(world -> world.getEnvironment() == Environment.NORMAL)
            .forEach(world -> world.setTime(14400));

        sender.sendMessage(Language.PREFIX + Language.TIME_MESSAGE_NIGHT);
    }
}

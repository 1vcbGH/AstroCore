package me.vcb.astrocore.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.ConfigFile;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;

public class AstroCoreCommand extends BaseCommand {

    public AstroCoreCommand() {
        super("astrocore");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            this.sendPluginInfo(sender);
            return;
        }

        if(!sender.hasPermission("astrocore.reload")) {
            this.sendPluginInfo(sender);
            return;
        }

        this.reloadConfigs(sender);
    }

    private void sendPluginInfo(CommandSender sender) {
        sender.sendMessage(Color.translate("&4&m---&8&m------------------------------------&4&m---"));
        sender.sendMessage(Color.translate(" &7AstroCore core made by: &c1vcb"));
        sender.sendMessage(Color.translate(" &7Plugin version: &c" + AstroCore.getInstance().getDescription().getVersion()));
        sender.sendMessage(Color.translate("&4&m---&8&m------------------------------------&4&m---"));
    }

    private void reloadConfigs(CommandSender sender) {
        long startTime = System.currentTimeMillis();

        AstroCore.getInstance().setConfig(new ConfigFile("config.yml"));
        AstroCore.getInstance().setLanguage(new ConfigFile("language.yml"));
        AstroCore.getInstance().setScoreboardFile(new ConfigFile("scoreboard.yml"));
        AstroCore.getInstance().setTabFile(new ConfigFile("tab.yml"));
        AstroCore.getInstance().setClassesFile(new ConfigFile("classes.yml"));
        AstroCore.getInstance().setLimitersFile(new ConfigFile("limiters.yml"));
        AstroCore.getInstance().setItemsFile(new ConfigFile("items.yml"));

        new Config();
        new Language();

        sender.sendMessage(Language.PREFIX + Language.PLUGIN_RELOAD_MESSAGE.replace("<time>",
        String.valueOf(System.currentTimeMillis() - startTime)));

        // Refresca scoreboard/tab de jugadores online sin necesidad de reiniciar.
        refreshLiveDisplays();
    }

    private void refreshLiveDisplays() {
        try {
            if (AstroCore.getInstance().getScoreboardManager() != null) {
                org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> {
                    var sb = AstroCore.getInstance().getScoreboardManager().getPlayerScoreboard(p);
                    if (sb != null) sb.applyTitle();
                });
            }
        } catch (Throwable ignored) {}
        try {
            if (Config.TAB_ENABLED && AstroCore.getInstance().getTabManager() != null) {
                AstroCore.getInstance().getTabManager().reload();
            }
        } catch (Throwable ignored) {}
        try {
            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> {
                if (me.vcb.astrocore.utils.nms.NmsUtils.getInstance().getClientVersion(p) >= 47)
                    me.vcb.astrocore.utils.nms.NmsUtils.getInstance().sendHeaderAndFooter(p);
            });
        } catch (Throwable ignored) {}
    }
}

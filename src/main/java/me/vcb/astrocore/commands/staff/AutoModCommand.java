package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.handlers.chat.AutoModHandler;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;

public class AutoModCommand extends BaseCommand {

    public AutoModCommand() {
        super("automod", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        AutoModHandler autoMod = (AutoModHandler) AstroCore.getInstance().getHandlerManager().getHandler(AutoModHandler.class);
        autoMod.toggle();
        boolean now = autoMod.isEnabled();
        sender.sendMessage(Color.translate("&6[AutoMod] &eAnti-spam " + (now ? "&aactivado" : "&cdesactivado") + "&e."));
    }
}

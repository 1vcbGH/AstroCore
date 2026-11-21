package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.staffmode.VanishManager;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class VanishCommand extends BaseCommand {

    public VanishCommand() {
        super("vanish", Collections.singletonList("v"), "astrocore.vanish");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        VanishManager manager = AstroCore.getInstance().getVanishManager();

        if(args.length == 0) {
            if(!this.checkConsoleSender(sender)) return;
            manager.toggleVanish((Player) sender);
            return;
        }

        if(args[0].equalsIgnoreCase("build")) {
            if(!this.checkConsoleSender(sender)) return;
            manager.toggleVanishBuild((Player) sender);
            return;
        }

        if(!this.checkPermission(sender, "astrocore.vanish.others")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        manager.toggleVanish(target);

        this.sendChangedMessage(sender, target, manager.isVanished(target)
            ? Language.VANISH_ENABLED_OTHERS : Language.VANISH_DISABLED_OTHERS);
    }

    private void sendChangedMessage(CommandSender sender, Player target, String message) {
        Messages.sendMessage(Language.PREFIX + message
            .replace("<player>", sender.getName())
            .replace("<target>", target.getName()), "astrocore.staff");
    }
}

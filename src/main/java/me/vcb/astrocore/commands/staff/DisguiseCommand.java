package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.disguise.DisguiseManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DisguiseCommand extends BaseCommand {

    public DisguiseCommand() {
        super("disguise", Arrays.asList("disfraz"), "astrocore.disguise");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!this.checkConsoleSender(sender)) return;
        Player player = (Player) sender;
        DisguiseManager manager = AstroCore.getInstance().getDisguiseManager();

        if (args.length == 0) {
            player.sendMessage(Color.translate("&6[Disguise] &eUso: /disguise on <nick> &o/disguise off"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "on":
            case "activar": {
                if (args.length < 2) {
                    player.sendMessage(Color.translate("&cUso: /disguise on <nick>"));
                    return;
                }
                String fakeName = args[1];
                if (fakeName.length() > 16) {
                    player.sendMessage(Color.translate("&cEl nombre no puede tener más de 16 caracteres."));
                    return;
                }
                manager.disguise(player, fakeName);
                break;
            }
            case "off":
            case "desactivar": {
                manager.undisguise(player);
                break;
            }
            default:
                player.sendMessage(Color.translate("&6[Disguise] &eUso: /disguise on <nick> &o/disguise off"));
        }
    }
}

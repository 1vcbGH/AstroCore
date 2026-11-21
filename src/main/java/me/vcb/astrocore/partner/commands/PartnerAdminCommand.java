package me.vcb.astrocore.partner.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.partner.PartnerManager;
import org.bukkit.command.CommandSender;

public class PartnerAdminCommand extends BaseCommand {

    public PartnerAdminCommand() {
        super("partneradmin", "astrocore.partner.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /partneradmin <create|delete|hide|show|resetstats> <nick> [displayName]");
            return;
        }

        String subCommand = args[0].toLowerCase();
        String nick = args[1];
        PartnerManager manager = AstroCore.getInstance().getPartnerManager();

        switch (subCommand) {
            case "create": {
                if (args.length < 3) {
                    sender.sendMessage("§cUso: /partneradmin create <nick> <displayName>");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) sb.append(" ");
                    sb.append(args[i]);
                }
                String displayName = org.bukkit.ChatColor.translateAlternateColorCodes('&', sb.toString());
                if (manager.createPartner(nick, displayName)) {
                    sender.sendMessage("§aPartner §e" + nick + " §acreado correctamente.");
                } else {
                    sender.sendMessage("§cYa existe un partner con el nick §e" + nick + "§c.");
                }
                break;
            }
            case "delete": {
                if (manager.deletePartner(nick)) {
                    sender.sendMessage("§aPartner §e" + nick + " §aeliminado.");
                } else {
                    sender.sendMessage("§cNo se encontró el partner §e" + nick + "§c.");
                }
                break;
            }
            case "hide": {
                if (manager.hidePartner(nick)) {
                    sender.sendMessage("§aPartner §e" + nick + " §aocultado.");
                } else {
                    sender.sendMessage("§cNo se encontró el partner §e" + nick + "§c.");
                }
                break;
            }
            case "show": {
                if (manager.showPartner(nick)) {
                    sender.sendMessage("§aPartner §e" + nick + " §aahora es visible.");
                } else {
                    sender.sendMessage("§cNo se encontró el partner §e" + nick + "§c.");
                }
                break;
            }
            case "resetstats": {
                if (manager.resetStats(nick)) {
                    sender.sendMessage("§aEstadísticas de §e" + nick + " §areiniciadas.");
                } else {
                    sender.sendMessage("§cNo se encontró el partner §e" + nick + "§c.");
                }
                break;
            }
            default: {
                sender.sendMessage("§cSubcomando desconocido. Usa: create|delete|hide|show|resetstats");
                break;
            }
        }
    }
}

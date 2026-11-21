package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Base for punishment commands: resolves an online or offline target by name. */
public abstract class PunishBaseCommand extends BaseCommand {

    protected PunishBaseCommand(String name) {
        super(name, "astrocore.staff");
    }

    protected interface Resolved { void run(UUID uuid, String name); }

    /** Resolves the target (online first, then offline data) off the main thread. */
    protected void resolve(CommandSender sender, String targetName, Resolved callback) {
        Player online = Bukkit.getPlayer(targetName);
        if (online != null) { callback.run(online.getUniqueId(), online.getName()); return; }

        Tasks.async(() -> {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(targetName);
            Tasks.sync(() -> {
                if (ud == null) {
                    sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado."));
                    return;
                }
                callback.run(ud.getUuid(), ud.getName());
            });
        });
    }

    /** Joins args[from..] into a reason, or a default if empty. */
    protected String joinReason(String[] args, int from, String def) {
        if (args.length <= from) return def;
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    protected String staffName(CommandSender sender) {
        return (sender instanceof Player p) ? p.getName() : "Consola";
    }

    /** Result of stripping the silent flag from args. */
    protected record SilentParse(String[] args, boolean silent) {}

    /** Removes any "-s" / "-silent" token from args and reports whether it was present. */
    protected SilentParse parseSilent(String[] args) {
        java.util.List<String> kept = new java.util.ArrayList<>();
        boolean silent = false;
        for (String a : args) {
            if (a.equalsIgnoreCase("-s") || a.equalsIgnoreCase("-silent")) { silent = true; continue; }
            kept.add(a);
        }
        return new SilentParse(kept.toArray(new String[0]), silent);
    }

    /** Broadcasts a message to everyone, or only to staff if silent. */
    protected void announce(boolean silent, CommandSender sender, String coloredMsg) {
        String msg = Color.translate(coloredMsg);
        if (silent) {
            sender.sendMessage(Color.translate("&7[&cSilencioso&7] ") + msg);
            for (Player online : Bukkit.getOnlinePlayers())
                if (online != sender && online.hasPermission("astrocore.staff"))
                    online.sendMessage(Color.translate("&7[&cSilencioso&7] ") + msg);
        } else {
            Bukkit.broadcastMessage(msg);
        }
    }
}

package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /blacklist &lt;nick&gt; [razón] — permanent, highest-severity ban. */
public class BlacklistCommand extends PunishBaseCommand {

    public BlacklistCommand() { super("blacklist"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        SilentParse sp = parseSilent(rawArgs);
        String[] args = sp.args();
        boolean silent = sp.silent();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/blacklist <nick> [razón] [-s]"));
            return;
        }
        String reason = joinReason(args, 1, "Blacklist");
        resolve(sender, args[0], (uuid, name) -> {
            PunishmentManager.getInstance().blacklist(uuid, name, staffName(sender), reason);
            announce(silent, sender, "&4[&cBlacklist&4] &e" + name
                + " &7fue añadido a la blacklist por &f" + staffName(sender) + " &7— &f" + reason);
        });
    }
}

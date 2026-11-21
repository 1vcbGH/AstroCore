package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /unban &lt;nick&gt; — removes active bans + blacklist. */
public class UnbanCommand extends PunishBaseCommand {

    public UnbanCommand() { super("unban"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        SilentParse sp = parseSilent(rawArgs);
        String[] args = sp.args();
        boolean silent = sp.silent();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/unban <nick> [-s]"));
            return;
        }
        resolve(sender, args[0], (uuid, name) -> {
            boolean changed = PunishmentManager.getInstance().unban(uuid, staffName(sender));
            if (changed) {
                announce(silent, sender, "&8[&aUnban&8] &e" + name
                    + " &7fue desbaneado por &f" + staffName(sender));
            } else {
                sender.sendMessage(Color.translate("&e" + name + " &7no tiene baneos activos."));
            }
        });
    }
}

package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /unmute &lt;nick&gt; */
public class UnmuteCommand extends PunishBaseCommand {

    public UnmuteCommand() { super("unmute"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        SilentParse sp = parseSilent(rawArgs);
        String[] args = sp.args();
        boolean silent = sp.silent();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/unmute <nick> [-s]"));
            return;
        }
        resolve(sender, args[0], (uuid, name) -> {
            boolean changed = PunishmentManager.getInstance().unmute(uuid, staffName(sender));
            if (changed) {
                announce(silent, sender, "&8[&aUnmute&8] &e" + name
                    + " &7fue des-silenciado por &f" + staffName(sender));
            } else {
                sender.sendMessage(Color.translate("&e" + name + " &7no está silenciado."));
            }
        });
    }
}

package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /kick &lt;nick&gt; [razón] — only works on online players. */
public class KickCommand extends PunishBaseCommand {

    public KickCommand() { super("kick"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        SilentParse sp = parseSilent(rawArgs);
        String[] args = sp.args();
        boolean silent = sp.silent();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/kick <nick> [razón] [-s]"));
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Color.translate("&cEse jugador no está conectado."));
            return;
        }
        String reason = joinReason(args, 1, "Expulsado por staff");
        PunishmentManager.getInstance().kick(target.getUniqueId(), target.getName(), staffName(sender), reason);
        announce(silent, sender, "&8[&6Kick&8] &e" + target.getName()
            + " &7fue expulsado por &f" + staffName(sender) + " &7— &f" + reason);
    }
}

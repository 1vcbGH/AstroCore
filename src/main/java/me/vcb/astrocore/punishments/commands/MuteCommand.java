package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.punishments.PunishmentUtil;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /mute &lt;nick&gt; [duración] [razón] */
public class MuteCommand extends PunishBaseCommand {

    public MuteCommand() { super("mute"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        SilentParse sp = parseSilent(rawArgs);
        String[] args = sp.args();
        boolean silent = sp.silent();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/mute <nick> [duración] [razón] [-s]"));
            sender.sendMessage(Color.translate("&7Duración: &f30m, 2h, 7d, perm &7(omitir = permanente)"));
            return;
        }

        String target = args[0];
        long durationMillis = 0;
        int reasonFrom = 1;

        if (args.length >= 2) {
            long parsed = PunishmentUtil.parseDuration(args[1]);
            if (parsed >= 0) { durationMillis = parsed; reasonFrom = 2; }
        }

        String reason = joinReason(args, reasonFrom, "Sin razón especificada");
        final long dur = durationMillis;

        resolve(sender, target, (uuid, name) -> {
            PunishmentManager.getInstance().mute(uuid, name, staffName(sender), reason, dur);
            String durLabel = dur <= 0 ? "Permanente" : PunishmentUtil.formatRemaining(dur);
            announce(silent, sender, "&8[&eMute&8] &e" + name
                + " &7fue silenciado por &f" + staffName(sender) + " &8(&7" + durLabel + "&8) &7— &f" + reason);
        });
    }
}

package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.PunishmentManager;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;

/** /warn &lt;nick&gt; [razón] */
public class WarnCommand extends PunishBaseCommand {

    public WarnCommand() { super("warn"); }

    @Override
    public void execute(CommandSender sender, String[] rawArgs) {
        String[] args = parseSilent(rawArgs).args();
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/warn <nick> [razón]"));
            return;
        }
        String reason = joinReason(args, 1, "Comportamiento inadecuado");
        resolve(sender, args[0], (uuid, name) -> {
            PunishmentManager.getInstance().warn(uuid, name, staffName(sender), reason);
            sender.sendMessage(Color.translate("&aAdvertencia enviada a &e" + name + " &7— &f" + reason));
        });
    }
}

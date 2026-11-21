package me.vcb.astrocore.punishments.commands;

import me.vcb.astrocore.punishments.gui.PunishGui;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /punish &lt;nick&gt; — opens the punishment GUI for the target. */
public class PunishCommand extends PunishBaseCommand {

    public PunishCommand() { super("punish"); }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(Color.translate("&cSolo jugadores pueden usar la GUI. Usá /ban, /mute, etc."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: &7/punish <nick>"));
            return;
        }
        resolve(sender, args[0], (uuid, name) ->
            PunishGui.getInstance().open(staff, uuid, name));
    }
}

package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.handlers.chat.MessagingHandler;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class MsgHistCommand extends BaseCommand {

    public MsgHistCommand() {
        super("msghist", Arrays.asList("msghist", "histmsg"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(Color.translate("&cUso: /msghist <jugador> [página]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Color.translate("&cJugador &e" + args[0] + " &cno está en línea."));
            return;
        }

        if (target == player) {
            player.sendMessage(Color.translate("&cNo puedes ver el historial contigo mismo."));
            return;
        }

        // Si es staff puede ver historial de cualquiera; si no, solo el propio
        if (!player.hasPermission("astrocore.staff")) {
            // Solo puede ver su propia conversación con target
        }

        int page = 1;
        if (args.length >= 2) {
            try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }

        MessagingHandler handler = AstroCore.getInstance().getMessagingHandler();
        int totalPages = handler.getHistoryPages(player.getUniqueId(), target.getUniqueId());

        if (totalPages == 0) {
            player.sendMessage(Color.translate("&eNo hay mensajes en el historial con &c" + target.getName() + "&e."));
            return;
        }

        if (page > totalPages) page = totalPages;

        List<String> lines = handler.getHistory(player.getUniqueId(), target.getUniqueId(), page);
        if (lines == null || lines.isEmpty()) {
            player.sendMessage(Color.translate("&cPágina &e" + page + " &cno existe."));
            return;
        }

        player.sendMessage(Color.translate("&6&l[Historial] &eConversación con &c" + target.getName()
            + " &8— &7Página " + page + "/" + totalPages));
        lines.forEach(player::sendMessage);
        if (page < totalPages) {
            player.sendMessage(Color.translate("&7Más: &b/msghist " + target.getName() + " " + (page + 1)));
        }
    }
}

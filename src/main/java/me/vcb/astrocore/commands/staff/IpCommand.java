package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IpCommand extends BaseCommand {

    public IpCommand() {
        super("ip", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: /ip <jugador>"));
            return;
        }

        String targetName = args[0];

        // Online: rápido
        Player online = Bukkit.getPlayer(targetName);
        if (online != null) {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(online);
            String ip = ud != null && ud.getLastIp() != null ? ud.getLastIp() : "desconocida";
            sender.sendMessage(Color.translate("&6[IP] &e" + online.getName() + " &8→ &b" + ip));
            return;
        }

        // Offline: async
        Tasks.async(() -> {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(targetName);
            Tasks.sync(() -> {
                if (ud == null) {
                    sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado."));
                    return;
                }
                String ip = ud.getLastIp() != null ? ud.getLastIp() : "desconocida";
                sender.sendMessage(Color.translate("&6[IP] &e" + ud.getName() + " &8→ &b" + ip
                    + " &8(&7última conexión&8)"));
            });
        });
    }
}

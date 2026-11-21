package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.FileUtils;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AltsCommand extends BaseCommand {

    public AltsCommand() {
        super("alts", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: /alts <jugador>"));
            return;
        }

        String targetName = args[0];
        sender.sendMessage(Color.translate("&6[Alts] &7Buscando cuentas con la misma IP..."));

        Tasks.async(() -> {
            // 1. Obtener IP del target
            Userdata targetUd = getUdByName(targetName);
            if (targetUd == null) {
                Tasks.sync(() -> sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado.")));
                return;
            }
            String targetIp = targetUd.getLastIp();
            if (targetIp == null || targetIp.isEmpty()) {
                Tasks.sync(() -> sender.sendMessage(Color.translate("&e" + targetName + " &cno tiene IP registrada.")));
                return;
            }

            // 2. Escanear todos los userdata buscando la misma IP
            List<String> alts = new ArrayList<>();
            File[] files = Config.USERDATA_DIR.listFiles((d, n) -> n.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    String content = FileUtils.readWholeFile(file);
                    if (content == null) continue;
                    try {
                        Userdata ud = AstroCore.getInstance().getGson().fromJson(content, Userdata.class);
                        if (ud == null || ud.getName() == null) continue;
                        if (ud.getName().equalsIgnoreCase(targetName)) continue;
                        if (targetIp.equals(ud.getLastIp())) alts.add(ud.getName());
                    } catch (Exception ignored) {}
                }
            }

            Tasks.sync(() -> {
                sender.sendMessage(Color.translate("&6[Alts] &eIP de &c" + targetUd.getName()
                    + "&e: &b" + targetIp));
                if (alts.isEmpty()) {
                    sender.sendMessage(Color.translate("  &7No se encontraron alts."));
                } else {
                    sender.sendMessage(Color.translate("  &7Alts encontradas &8(&e" + alts.size() + "&8)&7:"));
                    alts.forEach(name -> {
                        Player online = Bukkit.getPlayer(name);
                        String status = online != null ? "&a[En línea]" : "&c[Offline]";
                        sender.sendMessage(Color.translate("  &7- " + status + " &f" + name));
                    });
                }
            });
        });
    }

    private Userdata getUdByName(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return AstroCore.getInstance().getUserdataManager().getUserdata(online);
        return AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(name);
    }
}

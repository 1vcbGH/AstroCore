package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.sanctions.SanctionEntry;
import me.vcb.astrocore.sanctions.SanctionManager;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HistorialCommand extends BaseCommand {

    public HistorialCommand() {
        super("historial", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Color.translate("&cUso: /historial <jugador>"));
            return;
        }

        String targetName = args[0];
        Player online = Bukkit.getPlayer(targetName);

        if (online != null) {
            showHistory(sender, online.getUniqueId(), online.getName());
            return;
        }

        Tasks.async(() -> {
            Userdata ud = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(targetName);
            Tasks.sync(() -> {
                if (ud == null) {
                    sender.sendMessage(Color.translate("&cJugador &e" + targetName + " &cno encontrado."));
                    return;
                }
                showHistory(sender, ud.getUuid(), ud.getName());
            });
        });
    }

    private void showHistory(CommandSender sender, java.util.UUID uuid, String name) {
        List<SanctionEntry> list = SanctionManager.getInstance().getSanctions(uuid);
        sender.sendMessage(Color.translate("&6&l[Historial] &e" + name
            + " &8(&7" + list.size() + " sanciones&8)"));

        if (list.isEmpty()) {
            sender.sendMessage(Color.translate("  &7Sin sanciones registradas."));
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            sender.sendMessage(list.get(i).toDisplayLine(i + 1));
        }
    }
}

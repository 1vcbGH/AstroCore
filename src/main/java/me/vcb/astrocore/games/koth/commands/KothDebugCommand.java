package me.vcb.astrocore.games.koth.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.games.Cuboid;
import me.vcb.astrocore.games.koth.KothData;
import me.vcb.astrocore.games.koth.RunningKoth;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KothDebugCommand extends SubCommand {

    KothDebugCommand() {
        super("debug", "astrocore.koth.admin", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("§cUso: /koth debug <nombre>");
            return;
        }

        KothData koth = AstroCore.getInstance().getKothManager().getKoth(args[0]);
        if(koth == null) {
            sender.sendMessage("§cKoth no encontrado: " + args[0]);
            return;
        }

        Player player = (Player) sender;
        Cuboid cuboid = koth.getCuboid();

        sender.sendMessage("§e=== KOTH DEBUG: " + koth.getName() + " ===");

        if(cuboid == null) {
            sender.sendMessage("§cCuboid: §4NULL §c— el koth no tiene capzone asignada");
        } else {
            sender.sendMessage("§eCuboid world: §f" + cuboid.getWorldName());
            sender.sendMessage("§eCuboid X: §f" + cuboid.getMinX() + " → " + cuboid.getMaxX());
            sender.sendMessage("§eCuboid Y: §f" + cuboid.getMinY() + " → " + cuboid.getMaxY());
            sender.sendMessage("§eCuboid Z: §f" + cuboid.getMinZ() + " → " + cuboid.getMaxZ());
        }

        sender.sendMessage("§ePlayer world: §f" + player.getWorld().getName());
        sender.sendMessage("§ePlayer pos: §fX=" + player.getLocation().getBlockX()
            + " Y=" + player.getLocation().getBlockY()
            + " Z=" + player.getLocation().getBlockZ());

        if(cuboid != null) {
            boolean inside = cuboid.contains(player.getLocation());
            sender.sendMessage("§ePlayer in cuboid: §f" + (inside ? "§aSI" : "§cNO"));
        }

        RunningKoth running = AstroCore.getInstance().getKothManager().getRunningKoth(koth.getName());
        if(running == null) {
            sender.sendMessage("§eRunning: §cNo está corriendo");
        } else {
            sender.sendMessage("§eRunning: §aSi");
            sender.sendMessage("§eCapzone players: §f" + running.getCapzone().getPlayers());
            sender.sendMessage("§eCapzone time left: §f" + running.getCapzone().getTime() + "s");
            sender.sendMessage("§eCapzone cuboid world: §f" + running.getCapzone().getCuboid().getWorldName());
        }
    }
}

package me.vcb.astrocore.tags;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TagAdminCommand extends BaseCommand {

    public TagAdminCommand() {
        super("tag", Arrays.asList("giveTag"), "astrocore.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // /tag give <jugador> <tag>
        if(args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            sender.sendMessage("§cJugador §f" + args[1] + " §cno está online.");
            return;
        }

        ChatTag tag = ChatTag.fromId(args[2]);
        if(tag == null) {
            sender.sendMessage("§cTag desconocido: §f" + args[2]);
            sender.sendMessage("§7Tags disponibles: §f" + Arrays.stream(ChatTag.values())
                .map(ChatTag::getId).collect(Collectors.joining(", ")));
            return;
        }

        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(target);
        if(ud == null) {
            sender.sendMessage("§cNo se encontró el userdata de §f" + target.getName() + "§c.");
            return;
        }

        if(ud.ownsTag(tag)) {
            sender.sendMessage("§e" + target.getName() + " §7ya tiene el tag §f" + tag.getDisplay() + "§7.");
            return;
        }

        ud.giveTag(tag);
        sender.sendMessage("§aDiste el tag " + tag.getDisplay() + " §aa §e" + target.getName() + "§a.");
        target.sendMessage("§a¡Recibiste el tag " + tag.getDisplay() + " §ade un administrador!");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§cUso: §f/tag give <jugador> <tag>");
        sender.sendMessage("§7Tags: §f" + Arrays.stream(ChatTag.values())
            .map(ChatTag::getId).collect(Collectors.joining(", ")));
    }
}

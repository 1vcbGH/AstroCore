package me.vcb.astrocore.friends;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FriendCommand extends BaseCommand {

    public FriendCommand() {
        super("friend", Arrays.asList("friends", "amigo", "amigos"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        FriendsManager manager = FriendsManager.getInstance();

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
            case "agregar": {
                if (args.length < 2) { player.sendMessage(Color.translate("&cUso: /friend add <jugador>")); return; }
                manager.sendRequest(player, args[1]);
                break;
            }
            case "accept":
            case "aceptar": {
                if (args.length < 2) { player.sendMessage(Color.translate("&cUso: /friend accept <jugador>")); return; }
                manager.acceptRequest(player, args[1]);
                break;
            }
            case "decline":
            case "deny":
            case "rechazar": {
                if (args.length < 2) { player.sendMessage(Color.translate("&cUso: /friend decline <jugador>")); return; }
                manager.declineRequest(player, args[1]);
                break;
            }
            case "remove":
            case "delete":
            case "eliminar": {
                if (args.length < 2) { player.sendMessage(Color.translate("&cUso: /friend remove <jugador>")); return; }
                manager.removeFriend(player, args[1]);
                break;
            }
            case "list":
            case "lista": {
                manager.listFriends(player);
                break;
            }
            case "requests":
            case "solicitudes": {
                manager.listRequests(player);
                break;
            }
            default:
                sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(Color.translate("&6[Amigos] &eComandos:"));
        player.sendMessage(Color.translate("  &b/friend add <jugador> &7- Enviar solicitud"));
        player.sendMessage(Color.translate("  &b/friend accept <jugador> &7- Aceptar solicitud"));
        player.sendMessage(Color.translate("  &b/friend decline <jugador> &7- Rechazar solicitud"));
        player.sendMessage(Color.translate("  &b/friend remove <jugador> &7- Eliminar amigo"));
        player.sendMessage(Color.translate("  &b/friend list &7- Ver tus amigos"));
        player.sendMessage(Color.translate("  &b/friend requests &7- Ver solicitudes pendientes"));
    }
}

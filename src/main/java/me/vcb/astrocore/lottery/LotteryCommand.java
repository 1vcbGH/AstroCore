package me.vcb.astrocore.lottery;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LotteryCommand extends BaseCommand {

    public LotteryCommand() {
        super("lottery", Arrays.asList("loto"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        LotteryManager manager = LotteryManager.getInstance();

        if(args.length == 0) {
            player.sendMessage(Color.translate("&6[Lotería] &eUso: /lottery buy [cantidad]"));
            player.sendMessage(Color.translate("&6[Lotería] &eTenés &b" + manager.getTickets(player) + " &eticket(s). Precio: &c$" + LotteryManager.TICKET_PRICE + " &ec/u"));
            return;
        }

        if(args[0].equalsIgnoreCase("start")) {
            if(!player.hasPermission("astrocore.staff")) {
                player.sendMessage(Color.translate("&cNo tenés permiso."));
                return;
            }
            if(manager.isActive()) {
                player.sendMessage(Color.translate("&cLa lotería ya está activa."));
                return;
            }
            manager.startLottery();
            player.sendMessage(Color.translate("&aLotería iniciada manualmente."));
            return;
        }

        if(args[0].equalsIgnoreCase("stop")) {
            if(!player.hasPermission("astrocore.staff")) { player.sendMessage(Color.translate("&cNo tenés permiso.")); return; }
            if(!manager.isActive()) { player.sendMessage(Color.translate("&cLa lotería no está activa.")); return; }
            manager.stopLottery();
            return;
        }

        if(args[0].equalsIgnoreCase("results")) {
            if(!player.hasPermission("astrocore.staff")) { player.sendMessage(Color.translate("&cNo tenés permiso.")); return; }
            if(!manager.isActive()) { player.sendMessage(Color.translate("&cLa lotería no está activa.")); return; }
            manager.drawWinnerNow();
            return;
        }

        if(args[0].equalsIgnoreCase("buy")) {
            int amount = 1;
            if(args.length >= 2) {
                try {
                    amount = Integer.parseInt(args[1]);
                    if(amount < 1) {
                        player.sendMessage(Color.translate("&cCantidad inválida."));
                        return;
                    }
                } catch(NumberFormatException e) {
                    player.sendMessage(Color.translate("&cUsá un número válido."));
                    return;
                }
            }
            manager.buyTicket(player, amount);
            return;
        }

        player.sendMessage(Color.translate("&6[Lotería] &eUso: /lottery buy [cantidad]"));
    }
}

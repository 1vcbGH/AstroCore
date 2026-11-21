package me.vcb.astrocore.elevator;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /elevators give &lt;nick&gt; [cantidad] — entrega bloques de elevador. */
public class ElevatorCommand extends BaseCommand {

    public ElevatorCommand() {
        super("elevators", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Color.translate("&cUso: &7/elevators give <nick> [cantidad]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Color.translate("&cEse jugador no está conectado."));
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[2]))); }
            catch (NumberFormatException e) { sender.sendMessage(Color.translate("&cCantidad inválida.")); return; }
        }

        ItemStack item = ElevatorManager.getInstance().createItem(amount);
        target.getInventory().addItem(item);
        target.sendMessage(Color.translate("&aRecibiste &f" + amount + " &abloque(s) de elevador."));
        sender.sendMessage(Color.translate("&aLe diste &f" + amount + " &aelevador(es) a &e" + target.getName() + "&a."));
    }
}

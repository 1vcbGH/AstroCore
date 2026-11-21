package me.vcb.astrocore.bounty;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class BountyCommand extends BaseCommand {

    private static final int MIN_BOUNTY = 100;
    private static final int MAX_BOUNTY = 100_000;

    public BountyCommand() {
        super("bounty", Arrays.asList("precio", "recompensa"), null, true); // players only
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            this.sendBountyList(player);
            return;
        }

        // /bounty list
        if (args[0].equalsIgnoreCase("list")) {
            this.sendBountyList(player);
            return;
        }

        // /bounty <nick> <coins>
        if (args.length < 2) {
            player.sendMessage("§cUso: §e/bounty <jugador> <coins>");
            player.sendMessage("§cO usa §e/bounty list §cpara ver bounties activos.");
            return;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage("§cNo puedes ponerte bounty a ti mismo.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cCantidad inválida: §e" + args[1]);
            return;
        }

        if (amount < MIN_BOUNTY) {
            player.sendMessage("§cEl bounty mínimo es §e$" + MIN_BOUNTY + " coins§c.");
            return;
        }
        if (amount > MAX_BOUNTY) {
            player.sendMessage("§cEl bounty máximo es §e$" + MAX_BOUNTY + " coins§c.");
            return;
        }

        // Deduct from setter's balance
        Userdata setterUd = AstroCore.getInstance().getUserdataManager().getUserdata(player);
        if (setterUd == null) return;

        if (setterUd.getBalance() < amount) {
            player.sendMessage("§cNo tienes suficientes coins. Tienes §e" + setterUd.getBalance() + "§c.");
            return;
        }

        // Resolve target (online or offline by name)
        Player onlineTarget = Bukkit.getPlayer(targetName);
        String resolvedName = onlineTarget != null ? onlineTarget.getName() : targetName;
        java.util.UUID resolvedUuid = onlineTarget != null ? onlineTarget.getUniqueId() : null;

        // Try to get UUID from offline userdata if not online
        if (resolvedUuid == null) {
            Userdata targetUd = AstroCore.getInstance().getUserdataManager().getOfflineUserdataByName(targetName);
            if (targetUd == null) {
                player.sendMessage("§cJugador §e" + targetName + " §cno encontrado.");
                return;
            }
            resolvedUuid = targetUd.getUuid();
            resolvedName = targetUd.getName();
        }

        // Deduct coins
        setterUd.changeBalance(setterUd.getBalance() - amount);

        // Place bounty
        BountyManager.getInstance().placeBounty(resolvedUuid, resolvedName, amount, player.getName());
    }

    private void sendBountyList(Player player) {
        player.sendMessage("§8§m                                        ");
        player.sendMessage("§6§l  Bounties activos:");
        player.sendMessage("§8§m                                        ");

        java.util.Collection<BountyData> bounties = BountyManager.getInstance().getAllBounties();
        if (bounties.isEmpty()) {
            player.sendMessage("§7  No hay bounties activos.");
        } else {
            bounties.stream()
                .sorted((a, b) -> b.getAmount() - a.getAmount())
                .forEach(b -> player.sendMessage("  §e" + b.getTargetName()
                    + " §8| §6$" + b.getAmount() + " coins"
                    + " §8(por §7" + b.getSetterName() + "§8)"));
        }

        player.sendMessage("§8§m                                        ");
    }
}

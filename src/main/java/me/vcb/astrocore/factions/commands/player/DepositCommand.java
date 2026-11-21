package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class DepositCommand extends SubCommand {

    public DepositCommand() {
        super("deposit", Collections.singletonList("d"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_DEPOSIT_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        int balance = AstroCore.getInstance().getEconomyManager().getBalance(player);
        int amount;

        if(args[0].equalsIgnoreCase("all")) {
            amount = balance;
        } else {
            if(!this.checkNumber(sender, args[0])) return;
            amount = Math.abs(Integer.parseInt(args[0]));
        }

        if(amount > balance) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_DEPOSIT_NOT_ENOUGH_MONEY
            .replace("<amount>", String.valueOf(balance)));
            return;
        }

        if(amount == 0) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_DEPOSIT_CAN_NOT_DEPOSIT_ZERO);
            return;
        }

        // Faction balance cap (raised by the /f upgrades balance-cap upgrade).
        int cap = faction.getUpgradedMaxBalance();
        if(faction.getBalance() + amount > cap) {
            player.sendMessage(Language.FACTION_PREFIX + me.vcb.astrocore.utils.Color.translate(
                "&cLa facción llegó a su tope de dinero (&6$" + cap + "&c). Mejoralo en &f/f upgrades&c."));
            return;
        }

        AstroCore.getInstance().getEconomyManager().removeBalance(player, amount);
        faction.addBalance(amount);
        faction.addLog("&a+ &f" + player.getName() + " &7depositó &6$" + amount);
        if (me.vcb.astrocore.missions.MissionsManager.getInstance() != null)
            me.vcb.astrocore.missions.MissionsManager.getInstance().onDeposit(player, amount);

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_DEPOSIT_DEPOSITED
            .replace("<player>", player.getName())
            .replace("<amount>", String.valueOf(amount)));
    }
}

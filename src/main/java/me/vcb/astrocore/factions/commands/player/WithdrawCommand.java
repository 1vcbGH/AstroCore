package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.enums.Role;
import me.vcb.astrocore.factions.type.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class WithdrawCommand extends SubCommand {

    public WithdrawCommand() {
        super("withdraw", Collections.singletonList("w"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_WITHDRAW_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!faction.getMember(player).getRole().isAtLeast(Role.CAPTAIN)) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NO_PERMISSION
                .replace("<role>", Role.CAPTAIN.getName()));
            return;
        }

        int amount;

        if(args[0].equalsIgnoreCase("all")) {
            amount = faction.getBalance();
        } else {
            if(!this.checkNumber(sender, args[0])) return;
            amount = Math.abs(Integer.parseInt(args[0]));
        }

        if(amount > faction.getBalance()) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_WITHDRAW_NOT_ENOUGH_MONEY
                .replace("<amount>", String.valueOf(faction.getBalance())));
            return;
        }

        AstroCore.getInstance().getEconomyManager().addBalance(player, amount);
        faction.removeBalance(amount);
        faction.addLog("&c- &f" + player.getName() + " &7retiró &6$" + amount);

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_WITHDRAW_WITHDRAWN
            .replace("<player>", player.getName())
            .replace("<amount>", String.valueOf(amount)));
    }
}

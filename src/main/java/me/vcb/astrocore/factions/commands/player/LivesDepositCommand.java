package me.vcb.astrocore.factions.commands.player;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.userdata.Userdata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class LivesDepositCommand extends SubCommand {

    public LivesDepositCommand() {
        super("livesdeposit", Collections.singletonList("depositlives"), true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LIVES_DEPOSIT_USAGE);
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = FactionsManager.getInstance().getPlayerFaction(player);

        if(faction == null) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_NOT_IN_FACTION_SELF);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;

        Userdata data = AstroCore.getInstance().getUserdataManager().getUserdata(player);

        int amount = Math.abs(Integer.parseInt(args[0]));

        if(data.getLives() < amount) {
            player.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LIVES_DEPOSIT_NOT_ENOUGH_LIVES
            .replace("<amount>", String.valueOf(data.getLives())));
            return;
        }

        data.setLives(data.getLives() - amount);
        faction.setLives(faction.getLives() + amount);

        faction.sendMessage(Language.FACTION_PREFIX + Language.FACTIONS_LIVES_DEPOSITED.replace("<player>",
        player.getName()).replace("<amount>", String.valueOf(amount)));
    }
}

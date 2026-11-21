package me.vcb.astrocore.commands.base;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.timer.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class EnderchestCommand extends BaseCommand {

    public EnderchestCommand() {
        super("enderchest", Collections.singletonList("ec"), "astrocore.enderchest", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(Config.COMBAT_TAG_DISABLE_ENDERCHESTS && TimerManager.getInstance().getCombatTagTimer().isActive(player)) {
            player.sendMessage(Language.PREFIX + Language.COMBAT_TAG_ENDERCHEST_DENY);
            return;
        }

        if(args.length == 0) {
            player.openInventory(player.getEnderChest());
            return;
        }

        if(!this.checkPermission(sender, "astrocore.enderchest.others")) return;

        Player target = Bukkit.getPlayer(args[0]);
        if(!this.checkPlayer(sender, target, args[0])) return;

        player.openInventory(target.getEnderChest());
    }
}

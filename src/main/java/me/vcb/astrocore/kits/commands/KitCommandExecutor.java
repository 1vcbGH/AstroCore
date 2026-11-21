package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.kits.gui.KitMenuGui;
import me.vcb.astrocore.kits.kit.KitData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class KitCommandExecutor extends SubCommandExecutor {

    private final KitMenuGui kitMenuGui;

    public KitCommandExecutor() {
        super("kit", Collections.singletonList("kits"), Language.KITS_COMMAND_USAGE_PLAYER);

        this.setPrefix(Language.KIT_PREFIX);


        this.kitMenuGui = new KitMenuGui();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args.length == 0) {
            if(sender instanceof Player) {
                this.kitMenuGui.openMain((Player) sender);
                return true;
            }

            Language.KITS_COMMAND_USAGE_PLAYER.forEach(sender::sendMessage);
            return true;
        }

        SubCommand sub = this.getSubCommand(args[0]);

        if(sub == null) {
            KitData kit = AstroCore.getInstance().getKitsManager().getKit(args[0]);

            if(kit != null && sender instanceof Player) {
                Player player = (Player) sender;
                AstroCore.getInstance().getKitsManager().giveKit(player, kit);
                return true;
            }

            if(kit != null && sender instanceof ConsoleCommandSender) {
                sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_FOR_PLAYERS_ONLY);
                return true;
            }

            sender.sendMessage(Language.KIT_PREFIX + Language.KITS_EXCEPTION_DOESNT_EXISTS.replace("<kit>", args[0]));
            return true;
        }

        if(sub.isForPlayersOnly() && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_FOR_PLAYERS_ONLY);
            return true;
        }

        if(sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(Language.KIT_PREFIX + Language.COMMANDS_NO_PERMISSION);
            return true;
        }

        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }
}

package me.vcb.astrocore.kits.commands;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.commands.manager.SubCommandExecutor;
import me.vcb.astrocore.config.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.Collections;

public class KitEditorCommandExecutor extends SubCommandExecutor {

    public KitEditorCommandExecutor() {
        super("kiteditor", Collections.singletonList("kitedit"), Language.KITS_COMMAND_USAGE_ADMIN);

        this.setPrefix(Language.KIT_PREFIX);

        this.addSubCommand(new KitCreateCommand());
        this.addSubCommand(new KitDelayCommand());
        this.addSubCommand(new KitEditCommand());
        this.addSubCommand(new KitGiveCommand());
        this.addSubCommand(new KitListCommand());
        this.addSubCommand(new KitRemoveCommand());
        this.addSubCommand(new KitCategoryCommand());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args.length == 0) {
            this.getUsageMessage(sender).forEach(sender::sendMessage);
            return true;
        }

        SubCommand sub = this.getSubCommand(args[0]);

        if(sub == null) {
            sender.sendMessage(Language.KIT_PREFIX + "§cSubcomando desconocido. Usá /kiteditor para ver la ayuda.");
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

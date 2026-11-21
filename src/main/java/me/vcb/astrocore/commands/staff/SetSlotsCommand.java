package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.nms.NmsUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class SetSlotsCommand extends BaseCommand {

    public SetSlotsCommand() {
        super("setslots", Collections.singletonList("slots"), "astrocore.setslots");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(Language.PREFIX + Language.SET_SLOTS_USAGE);
            return;
        }

        if(!this.checkNumber(sender, args[0])) return;
        int amount = Math.abs(Integer.parseInt(args[0]));

        NmsUtils.getInstance().changeServerSlots(amount);

        Language.SET_SLOTS_MESSAGE.forEach(line -> Messages.sendMessage(line
            .replace("<number>", String.valueOf(amount))
            .replace("<player>", sender.getName())));
    }
}

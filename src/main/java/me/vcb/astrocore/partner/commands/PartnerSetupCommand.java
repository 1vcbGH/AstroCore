package me.vcb.astrocore.partner.commands;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.partner.menu.PartnerSetupMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartnerSetupCommand extends BaseCommand {

    private final PartnerSetupMenu menu;

    public PartnerSetupCommand() {
        super("partnersetup", "astrocore.partner.admin", true);
        this.menu = new PartnerSetupMenu();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        this.menu.open(player);
    }
}

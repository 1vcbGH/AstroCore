package me.vcb.astrocore.partner.commands;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.partner.menu.PartnerMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartnerCommand extends BaseCommand {

    private final PartnerMenu menu;

    public PartnerCommand() {
        super("partner", true);
        this.menu = new PartnerMenu();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        this.menu.open(player);
    }
}

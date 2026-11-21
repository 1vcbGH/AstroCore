package me.vcb.astrocore.commands.staff;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HeadModCommand extends BaseCommand {

    public HeadModCommand() {
        super("headmod", "astrocore.headmod");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!this.checkConsoleSender(sender)) return;
        Player player = (Player) sender;

        boolean entering = !AstroCore.getInstance().getVanishManager().isHeadMod(player);

        // Primero cambiar el flag headmod, LUEGO toggleStaffMode (que llama updateTabRelations)
        AstroCore.getInstance().getVanishManager().toggleHeadMod(player);
        AstroCore.getInstance().getStaffModeManager().toggleStaffMode(player);

        // Forzar refresh del scoreboard DESPUÉS de que ambos flags estén en su estado final
        AstroCore.getInstance().getScoreboardManager().updateTabRelations(player, false);

        if(entering) {
            player.sendMessage(Color.translate("&6[HeadMod] &aActivado. &7Staffs comunes no te ven, tú sí a ellos."));
        } else {
            player.sendMessage(Color.translate("&6[HeadMod] &cDesactivado."));
        }
    }
}

package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.hologram.Hologram;
import org.bukkit.command.CommandSender;

public class HologramClearItemCommand extends SubCommand {

    public HologramClearItemCommand() {
        super("clearitem", "astrocore.hologram.clearitem", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage("&cUso: /hologram clearitem <id>");
            return;
        }

        int id;
        try { id = Integer.parseInt(args[0]); }
        catch(NumberFormatException e) { sender.sendMessage("&cID invalido."); return; }

        Hologram hologram = AstroCore.getInstance().getHologramManager().getHologramById(id);
        if(hologram == null) { sender.sendMessage("&cHologram #" + id + " no existe."); return; }

        hologram.forEachViewer(hologram::hideItemDisplay);
        hologram.removeItemDisplay();
        hologram.setDisplayItem(null);

        sender.sendMessage("&aItem del hologram #" + id + " eliminado.");
    }
}

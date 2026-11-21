package me.vcb.astrocore.hologram.command;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.hologram.Hologram;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HologramSetItemCommand extends SubCommand {

    public HologramSetItemCommand() {
        super("setitem", "astrocore.hologram.setitem", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length < 1) {
            player.sendMessage("&cUso: /hologram setitem <id>");
            return;
        }

        int id;
        try { id = Integer.parseInt(args[0]); }
        catch(NumberFormatException e) { player.sendMessage("&cID invalido."); return; }

        Hologram hologram = AstroCore.getInstance().getHologramManager().getHologramById(id);
        if(hologram == null) { player.sendMessage("&cHologram #" + id + " no existe."); return; }

        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType().isAir()) { player.sendMessage("&cTen un item en la mano."); return; }

        hologram.setDisplayItem(item.clone());
        hologram.spawnItemDisplay();
        hologram.forEachViewer(hologram::sendItemDisplay);

        player.sendMessage("&aItem del hologram #" + id + " actualizado.");
    }
}

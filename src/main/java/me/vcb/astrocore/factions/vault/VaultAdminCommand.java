package me.vcb.astrocore.factions.vault;

import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.factions.Faction;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Color;
import org.bukkit.command.CommandSender;

/** /vaultadmin reset &lt;vault|leadervault&gt; &lt;faction&gt; — limpia el vault y resetea niveles. */
public class VaultAdminCommand extends BaseCommand {

    public VaultAdminCommand() {
        super("vaultadmin", "astrocore.staff");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage(Color.translate("&cUso: &7/vaultadmin reset <vault|leadervault> <facción>"));
            return;
        }

        String type = args[1].toLowerCase();
        boolean leader;
        if (type.equals("vault")) leader = false;
        else if (type.equals("leadervault") || type.equals("lvault")) leader = true;
        else { sender.sendMessage(Color.translate("&cTipo inválido. Usá: vault o leadervault")); return; }

        Faction faction = FactionsManager.getInstance().getAnyFaction(args[2]);
        if (!(faction instanceof PlayerFaction fac)) {
            sender.sendMessage(Color.translate("&cFacción de jugador no encontrada: &e" + args[2]));
            return;
        }

        FactionVaultManager.getInstance().reset(fac, leader);
        sender.sendMessage(Color.translate("&aReseteado el &f" + (leader ? "leadervault" : "vault")
            + " &ade &e" + fac.getName() + " &a(items borrados + nivel de páginas a 0)."));
    }
}

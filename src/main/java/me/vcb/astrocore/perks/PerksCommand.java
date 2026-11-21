package me.vcb.astrocore.perks;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.userdata.Userdata;
import me.vcb.astrocore.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /perks — abre la tienda.
 * /perks admin give|set &lt;nick&gt; &lt;perk&gt; &lt;cantidad&gt; — gestión de perks (staff).
 *   perks: falltrap, base, life, credits
 */
public class PerksCommand extends BaseCommand {

    public PerksCommand() {
        super("perks");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            handleAdmin(sender, args);
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Color.translate("&cSolo jugadores."));
            return;
        }
        PerksMenu.getInstance().open(player);
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("astrocore.staff")) {
            sender.sendMessage(Color.translate("&cNo tenés permiso."));
            return;
        }
        // /perks admin disable|enable <perk>
        if (args.length >= 3 && (args[1].equalsIgnoreCase("disable") || args[1].equalsIgnoreCase("enable"))) {
            boolean disable = args[1].equalsIgnoreCase("disable");
            PerksMenu.Perk perk = PerksMenu.resolvePerk(args[2]);
            if (perk == null) {
                sender.sendMessage(Color.translate("&cPerk inválido. Usá: falltrap, base, skybase, life, credits"));
                return;
            }
            PerksMenu.getInstance().setDisabled(perk, disable);
            sender.sendMessage(Color.translate("&aPerk &f" + perk.name + " &a" + (disable ? "&cDESHABILITADO" : "&aHABILITADO") + " &apara este mapa."));
            return;
        }

        // /perks admin give|set <nick> <perk> <cantidad>
        if (args.length < 5 || !(args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("set"))) {
            sender.sendMessage(Color.translate("&cUso: &7/perks admin <give|set> <nick> <perk> <cantidad>"));
            sender.sendMessage(Color.translate("&7     &7/perks admin <disable|enable> <perk>"));
            sender.sendMessage(Color.translate("&7Perks: &ffalltrap&7, &fbase&7, &fskybase&7, &flife&7, &fcredits"));
            return;
        }

        boolean set = args[1].equalsIgnoreCase("set");
        String targetName = args[2];
        String perk = args[3].toLowerCase();
        int amount;
        try { amount = Integer.parseInt(args[4]); }
        catch (NumberFormatException e) { sender.sendMessage(Color.translate("&cCantidad inválida.")); return; }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Color.translate("&cEse jugador no está conectado."));
            return;
        }
        Userdata ud = AstroCore.getInstance().getUserdataManager().getUserdata(target);
        if (ud == null) { sender.sendMessage(Color.translate("&cNo se encontró su data.")); return; }

        int newValue;
        String label;
        switch (perk) {
            case "falltrap", "falltrap_token", "falltraptoken" -> {
                newValue = set ? amount : ud.getFalltrapTokens() + amount;
                ud.setFalltrapTokens(Math.max(0, newValue)); label = "Tokens de Falltrap";
            }
            case "base", "base_token", "basetoken" -> {
                newValue = set ? amount : ud.getBaseTokens() + amount;
                ud.setBaseTokens(Math.max(0, newValue)); label = "Tokens de Base";
            }
            case "skybase", "skybase_token", "skybasetoken" -> {
                newValue = set ? amount : ud.getSkyBaseTokens() + amount;
                ud.setSkyBaseTokens(Math.max(0, newValue)); label = "Tokens de SkyBase";
            }
            case "life", "lives", "vida", "vidas" -> {
                newValue = set ? amount : ud.getLives() + amount;
                ud.setLives(Math.max(0, newValue)); label = "Vidas";
            }
            case "credits", "credit", "spawncredits", "creditos" -> {
                newValue = set ? amount : ud.getSpawnCredits() + amount;
                ud.setSpawnCredits(Math.max(0, newValue)); label = "Créditos de Spawn";
            }
            default -> {
                sender.sendMessage(Color.translate("&cPerk inválido. Usá: falltrap, base, skybase, life, credits"));
                return;
            }
        }

        sender.sendMessage(Color.translate("&a" + (set ? "Seteaste" : "Diste") + " &f" + label
            + " &ade &e" + target.getName() + " &a→ &f" + Math.max(0, newValue)));
        target.sendMessage(Color.translate("&aTus &f" + label + " &aahora son: &f" + Math.max(0, newValue)));
    }
}

package me.vcb.astrocore.factions.commands.admin;

import me.vcb.astrocore.commands.manager.SubCommand;
import me.vcb.astrocore.config.Language;
import me.vcb.astrocore.factions.FactionsManager;
import me.vcb.astrocore.factions.type.PlayerFaction;
import me.vcb.astrocore.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DisqualifyCommand extends SubCommand {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public DisqualifyCommand() {
        super("disqualify", "astrocore.factions.disqualify");
    }

    private static String t(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private PlayerFaction resolveFaction(String arg) {
        PlayerFaction f = FactionsManager.getInstance().getPlayerFactionByName(arg);
        if(f == null) f = FactionsManager.getInstance().getPlayerFaction(arg);
        return f;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(t(Language.FACTION_PREFIX + "&cUso: /f disqualify <faction/jugador> <motivo>"));
            return;
        }

        PlayerFaction faction = this.resolveFaction(args[0]);
        if(faction == null) {
            sender.sendMessage(t(Language.FACTION_PREFIX + Language.FACTIONS_FACTION_DOESNT_EXIST.replace("<argument>", args[0])));
            return;
        }

        if(faction.isDisqualified()) {
            sender.sendMessage(t(Language.FACTION_PREFIX + "&cEsa faction ya esta descalificada."));
            return;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String date = DATE_FORMAT.format(new Date());

        faction.applyDisqualify(reason, sender.getName(), date);

        faction.sendMessage(t("&c&l¡EQUIPO DESCALIFICADO!"));
        faction.sendMessage(t("&7Motivo: &e" + reason));
        faction.sendMessage(t("&7Aplicado por: &b" + sender.getName()));
        faction.sendMessage(t("&7Fecha: &f" + date));
        faction.sendMessage("");
        faction.sendMessage(t("&4&l⚠ &cEste clan no puede disbandearse ni"));
        faction.sendMessage(t("&cmodificar sus miembros hasta el fin del mapa."));

        sender.sendMessage(t(Language.FACTION_PREFIX + "&7Faction &c" + faction.getName() + " &7descalificada correctamente."));

        Messages.sendMessage(t("&c&l[DESCALIFICACION] &cLa faction &l" + faction.getName() +
            " &cha sido descalificada por &b" + sender.getName() + "&c. &8Motivo: &e" + reason));

        FactionsManager.getInstance().saveFactions(false, false);
    }
}

package me.vcb.astrocore.events;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.commands.manager.BaseCommand;
import me.vcb.astrocore.events.bloodmoon.BloodMoonManager;
import me.vcb.astrocore.events.hunterprey.HunterPreyManager;
import me.vcb.astrocore.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommand extends BaseCommand {

    private static final String PREFIX = "§4☾ §c§lLuna Sangrienta §4☾ §r ";

    public EventCommand() {
        super("event", "astrocore.event.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // /event start <tipo> <tiempo>
        // /event end <tipo>
        if(args.length < 2) {
            sendUsage(sender);
            return;
        }

        String action = args[0].toLowerCase();
        String type   = args[1].toLowerCase();

        switch(type) {
            case "bloodmoon":
            case "luna":
            case "lunasangrienta":
                handleBloodMoon(sender, action, args);
                break;
            case "cazador":
            case "hunterprey":
            case "presa":
                handleHunterPrey(sender, action, args);
                break;
            case "eloboost":
            case "boost":
                handleEloBoost(sender, action, args);
                break;
            default:
                sender.sendMessage(PREFIX + "§cEvento desconocido: §f" + args[1]);
                sender.sendMessage(PREFIX + "§7Eventos disponibles: §flunasangrienta§7, §fcazador§7, §feloboost");
        }
    }

    private void handleBloodMoon(CommandSender sender, String action, String[] args) {
        BloodMoonManager bm = BloodMoonManager.getInstance();

        switch(action) {
            case "start":
            case "iniciar": {
                if(bm.isActive()) {
                    sender.sendMessage(PREFIX + "§cLa Luna Sangrienta ya está activa.");
                    return;
                }
                if(args.length < 3) {
                    sender.sendMessage(PREFIX + "§cUso: §f/event start bloodmoon <tiempo> §7(ej: 30m, 1h, 600)");
                    return;
                }
                int seconds = StringUtils.parseSeconds(args[2]);
                if(seconds <= 0) {
                    sender.sendMessage(PREFIX + "§cTiempo inválido. Usá: 30m, 1h, 600 (segundos)");
                    return;
                }
                bm.start(seconds);
                sender.sendMessage(PREFIX + "§aLuna Sangrienta iniciada por §f" + formatTime(seconds) + "§a.");
                break;
            }
            case "end":
            case "stop":
            case "terminar": {
                if(!bm.isActive()) {
                    sender.sendMessage(PREFIX + "§cLa Luna Sangrienta no está activa.");
                    return;
                }
                bm.end(true);
                sender.sendMessage(PREFIX + "§aLuna Sangrienta terminada.");
                break;
            }
            default:
                sendUsage(sender);
        }
    }

    private void handleHunterPrey(CommandSender sender, String action, String[] args) {
        HunterPreyManager hp = HunterPreyManager.getInstance();

        switch(action) {
            case "start":
            case "iniciar": {
                if(hp.isActive()) {
                    sender.sendMessage(PREFIX + "§cYa hay un evento Cazador y Presa activo.");
                    return;
                }
                if(args.length < 3) {
                    sender.sendMessage(PREFIX + "§cUso: §f/event start cazador <tiempo> [jugador]");
                    return;
                }
                int seconds = StringUtils.parseSeconds(args[2]);
                if(seconds <= 0) {
                    sender.sendMessage(PREFIX + "§cTiempo inválido. Usá: 30m, 1h, 600");
                    return;
                }
                // Optional: specific prey player
                if(args.length >= 4) {
                    Player target = Bukkit.getPlayer(args[3]);
                    if(target == null) {
                        sender.sendMessage(PREFIX + "§cJugador §f" + args[3] + " §cno está online.");
                        return;
                    }
                    hp.startWithPlayer(target, seconds);
                } else {
                    if(Bukkit.getOnlinePlayers().isEmpty()) {
                        sender.sendMessage(PREFIX + "§cNo hay jugadores online.");
                        return;
                    }
                    hp.start(seconds);
                }
                sender.sendMessage(PREFIX + "§aEvento Cazador y Presa iniciado.");
                break;
            }
            case "end":
            case "stop":
            case "terminar": {
                if(!hp.isActive()) {
                    sender.sendMessage(PREFIX + "§cNo hay un evento Cazador y Presa activo.");
                    return;
                }
                hp.end(true, "§8§m                                                  \n" +
                    "   §7El evento §cCazador y Presa §7fue cancelado por un admin.\n" +
                    "§8§m                                                  ");
                sender.sendMessage(PREFIX + "§aEvento cancelado.");
                break;
            }
            default:
                sendUsage(sender);
        }
    }

    private void handleEloBoost(CommandSender sender, String action, String[] args) {
        me.vcb.astrocore.events.eloboost.EloBoostManager eb =
            me.vcb.astrocore.events.eloboost.EloBoostManager.getInstance();

        switch (action) {
            case "start":
            case "iniciar": {
                if (eb.isActive()) {
                    sender.sendMessage(PREFIX + "§cEl ELO Boost ya está activo.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage(PREFIX + "§cUso: §f/event start eloboost <tiempo> [multiplicador]");
                    sender.sendMessage(PREFIX + "§7Ej: §f/event start eloboost 30m 1.5");
                    return;
                }
                int seconds = StringUtils.parseSeconds(args[2]);
                if (seconds <= 0) {
                    sender.sendMessage(PREFIX + "§cTiempo inválido. Usá: 30m, 1h, 600");
                    return;
                }
                float mult = 1.5f;
                if (args.length >= 4) {
                    try { mult = Float.parseFloat(args[3]); }
                    catch (NumberFormatException ex) {
                        sender.sendMessage(PREFIX + "§cMultiplicador inválido (ej: 1.5, 2)");
                        return;
                    }
                }
                eb.start(seconds, mult);
                sender.sendMessage(PREFIX + "§aELO Boost iniciado — §e×" + mult
                    + " §7durante §f" + formatTime(seconds) + "§a.");
                break;
            }
            case "end":
            case "stop":
            case "terminar": {
                if (!eb.isActive()) {
                    sender.sendMessage(PREFIX + "§cNo hay un ELO Boost activo.");
                    return;
                }
                eb.end(true);
                sender.sendMessage(PREFIX + "§aELO Boost terminado.");
                break;
            }
            default:
                sendUsage(sender);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§8§m                              ");
        sender.sendMessage(" §c§l/event §7— Eventos custom");
        sender.sendMessage(" §f/event start bloodmoon <tiempo>");
        sender.sendMessage(" §f/event end bloodmoon");
        sender.sendMessage(" §f/event start cazador <tiempo> [jugador]");
        sender.sendMessage(" §f/event end cazador");
        sender.sendMessage(" §f/event start eloboost <tiempo> [multiplicador]");
        sender.sendMessage(" §f/event end eloboost");
        sender.sendMessage("§8§m                              ");
    }

    private String formatTime(int seconds) {
        if(seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        if(seconds >= 60) return (seconds / 60) + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}

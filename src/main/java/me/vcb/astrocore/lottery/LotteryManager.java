package me.vcb.astrocore.lottery;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.economy.EconomyManager;
import me.vcb.astrocore.utils.Color;
import me.vcb.astrocore.utils.Messages;
import me.vcb.astrocore.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LotteryManager {

    private static LotteryManager instance;

    public static final int TICKET_PRICE = 100;
    public static final int MAX_TICKETS_PER_PLAYER = 10;
    private static final long LOTTERY_INTERVAL_TICKS = 20L * 60 * 60; // 1 hora
    private static final long ANNOUNCE_INTERVAL_TICKS = 20L * 60 * 60; // anuncio cada 1h

    private final Map<UUID, Integer> tickets = new HashMap<>();
    private BukkitTask lotteryTask;
    private BukkitTask announceTask;
    private boolean active = false;
    private long startTime;

    public LotteryManager() {
        instance = this;
        this.scheduleTasks();
    }

    public static LotteryManager getInstance() {
        return instance;
    }

    private void scheduleTasks() {
        // Anuncio cada hora
        this.announceTask = Tasks.syncTimer(() -> {
            startLottery();
        }, ANNOUNCE_INTERVAL_TICKS, ANNOUNCE_INTERVAL_TICKS);
    }

    public void startLottery() {
        if(active) return;
        this.active = true;
        this.startTime = System.currentTimeMillis();
        Messages.sendMessage(Color.translate(
            "&8&m                                                  \n" +
            "  &6&l¡La lotería ha iniciado!\n" +
            "  &eComprá tickets con &b/lottery buy &e(&c$" + TICKET_PRICE + " &ec/u, máx " + MAX_TICKETS_PER_PLAYER + ")\n" +
            "  &eEl ganador es elegido en &c1 hora&e.\n" +
            "&8&m                                                  "
        ));
        if(lotteryTask != null) lotteryTask.cancel();
        this.lotteryTask = Tasks.syncLater(this::drawWinner, LOTTERY_INTERVAL_TICKS);
    }

    public boolean buyTicket(Player player, int amount) {
        if(!active) {
            player.sendMessage(Color.translate("&cLa lotería no está activa en este momento."));
            return false;
        }

        EconomyManager economy = AstroCore.getInstance().getEconomyManager();
        int currentTickets = tickets.getOrDefault(player.getUniqueId(), 0);

        if(currentTickets + amount > MAX_TICKETS_PER_PLAYER) {
            player.sendMessage(Color.translate("&cSólo podés tener &e" + MAX_TICKETS_PER_PLAYER + " &ctickets. Tenés &e" + currentTickets + "&c."));
            return false;
        }

        int totalCost = TICKET_PRICE * amount;
        if(economy.getBalance(player) < totalCost) {
            player.sendMessage(Color.translate("&cNo tenés suficiente dinero. Necesitás &e$" + totalCost + "&c."));
            return false;
        }

        economy.removeBalance(player, totalCost);
        tickets.merge(player.getUniqueId(), amount, Integer::sum);
        player.sendMessage(Color.translate("&aCompraste &e" + amount + " &aticket(s). Tenés &e" + tickets.get(player.getUniqueId()) + " &aen total. Costo: &c$" + totalCost));
        return true;
    }

    public void stopLottery() {
        if(!active) return;
        active = false;
        if(lotteryTask != null) { lotteryTask.cancel(); lotteryTask = null; }

        EconomyManager economy = AstroCore.getInstance().getEconomyManager();
        tickets.forEach((uuid, amount) -> {
            int refund = amount * TICKET_PRICE;
            Player p = Bukkit.getPlayer(uuid);
            if(p != null) {
                economy.addBalance(p, refund);
                p.sendMessage(Color.translate("&6[Lotería] &eSe canceló la lotería. Se te devolvieron &b$" + refund + "&e."));
            }
        });
        tickets.clear();
        Messages.sendMessage(Color.translate("&6[Lotería] &cLa lotería fue cancelada por un staff. Se devolvió el dinero a los participantes."));
    }

    public void drawWinnerNow() {
        if(lotteryTask != null) { lotteryTask.cancel(); lotteryTask = null; }
        drawWinner();
    }

    private void drawWinner() {
        active = false;

        if(tickets.isEmpty()) {
            Messages.sendMessage(Color.translate("&6[Lotería] &eNo había participantes. La lotería fue cancelada."));
            return;
        }

        // Weighted random: mas tickets = mas chances
        List<UUID> pool = new ArrayList<>();
        for(Map.Entry<UUID, Integer> entry : tickets.entrySet()) {
            for(int i = 0; i < entry.getValue(); i++) {
                pool.add(entry.getKey());
            }
        }

        int pot = tickets.values().stream().mapToInt(t -> t * TICKET_PRICE).sum();
        UUID winnerUuid = pool.get((int)(Math.random() * pool.size()));
        tickets.clear();

        Player winner = Bukkit.getPlayer(winnerUuid);
        String winnerName = winner != null ? winner.getName() : Bukkit.getOfflinePlayer(winnerUuid).getName();

        if(winner != null) {
            AstroCore.getInstance().getEconomyManager().addBalance(winner, pot);
        }

        Messages.sendMessage(Color.translate(
            "&8&m                                                  \n" +
            "  &6&l¡Resultado de la Lotería!\n" +
            "  &eGanador: &a" + winnerName + "\n" +
            "  &ePremio: &b$" + pot + "\n" +
            "&8&m                                                  "
        ));
    }

    public int getTickets(Player player) {
        return tickets.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean isActive() { return active; }

    public void disable() {
        if(lotteryTask != null) lotteryTask.cancel();
        if(announceTask != null) announceTask.cancel();
        tickets.clear();
    }
}

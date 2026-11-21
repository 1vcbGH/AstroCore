package me.vcb.astrocore.punishments;

import me.vcb.astrocore.utils.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PunishmentListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Punishment ban = PunishmentManager.getInstance().getActiveBan(event.getPlayer().getUniqueId());
        if (ban == null) return;
        event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
            PunishmentManager.getInstance().buildBanScreen(
                ban.getType(), ban.getReason(), ban.getStaff(), ban.getExpiresAt()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Punishment mute = PunishmentManager.getInstance().getActiveMute(player.getUniqueId());
        if (mute == null) return;

        event.setCancelled(true);
        String dur = mute.isPermanent()
            ? "&cPermanente"
            : "&7" + PunishmentUtil.formatRemaining(mute.remainingMillis());
        player.sendMessage(Color.translate("&cEstás silenciado y no podés hablar."));
        player.sendMessage(Color.translate("&7Razón: &f" + mute.getReason() + " &8| &7Expira: " + dur));
    }
}

package me.vcb.astrocore.handlers;

import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.handlers.manager.Handler;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ExpAmplifierHandler extends Handler implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();

        ItemStack item = player.getItemInHand();
        if(item == null || !item.containsEnchantment(Enchantment.LOOTING)) return;

        int amplifier = item.getEnchantmentLevel(Enchantment.LOOTING) * Config.EXP_AMPLIFIER_LOOTING;

        event.setDroppedExp(event.getDroppedExp() * amplifier);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if(item == null || !item.containsEnchantment(Enchantment.FORTUNE)) return;

        int amplifier = item.getEnchantmentLevel(Enchantment.FORTUNE) * Config.EXP_AMPLIFIER_FORTUNE;

        event.setExpToDrop(event.getExpToDrop() * amplifier);
    }
}

package me.vcb.astrocore.handlers;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.config.Config;
import me.vcb.astrocore.handlers.manager.Handler;
import me.vcb.astrocore.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class GlisteringMelonHandler extends Handler implements Listener {

    public GlisteringMelonHandler() {
        if(!Config.GLISTERING_MELON_CRAFTING) return;

        this.registerEasyRecipe();
    }

    private void registerEasyRecipe() {
        ItemStack easyMelon = new ItemStack(Material.GLISTERING_MELON_SLICE, 1);

        NamespacedKey key = new NamespacedKey(AstroCore.getInstance(), "easy_glistering_melon");
        ShapelessRecipe recipe = new ShapelessRecipe(key, easyMelon);
        recipe.addIngredient(1, Material.MELON_SLICE);
        recipe.addIngredient(1, Material.GOLD_NUGGET);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;
        if(event.getRecipe().getResult().getType() != Material.GLISTERING_MELON_SLICE) return;

        Player player = (Player) event.getWhoClicked();
        ItemUtils.updateInventory(player);
    }
}

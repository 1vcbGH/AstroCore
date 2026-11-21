package me.vcb.astrocore.partner.menu;

import me.vcb.astrocore.AstroCore;
import me.vcb.astrocore.partner.Partner;
import me.vcb.astrocore.partner.PartnerManager;
import me.vcb.astrocore.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class PartnerSetupMenu implements Listener {

    private static final String TITLE = "§e§lSetup Partners";
    private static final int SIZE = 54;

    private static final ItemStack FILLER;

    static {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        FILLER = pane;
    }

    public PartnerSetupMenu() {
        Bukkit.getPluginManager().registerEvents(this, AstroCore.getInstance());
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        PartnerManager manager = AstroCore.getInstance().getPartnerManager();
        List<Partner> all = manager.getAllPartners();

        for (int i = 0; i < all.size() && i < SIZE; i++) {
            Partner partner = all.get(i);

            @SuppressWarnings("deprecation")
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(partner.getNick()));
            skull.setItemMeta(skullMeta);

            List<String> lore = Arrays.asList(
                "§7Nick: §e" + partner.getNick(),
                "§7Usos: §e" + partner.getClaimCount(),
                "§7Oculto: §e" + (partner.isHidden() ? "§cSí" : "§aNo"),
                "§7Comandos: §e" + partner.getRewardCommands().size(),
                "",
                "§8» §eClick para configurar recompensas"
            );

            skull = new ItemBuilder(skull)
                .setName(partner.getDisplayName())
                .setLore(lore)
                .build();

            inv.setItem(i, skull);
        }

        for (int i = all.size(); i < SIZE; i++) {
            inv.setItem(i, FILLER);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!TITLE.equals(event.getView().getTitle())) return;

        event.setCancelled(true);

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR || current.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        if (current.getItemMeta() == null || " ".equals(current.getItemMeta().getDisplayName())) return;

        Player player = (Player) event.getWhoClicked();
        PartnerManager manager = AstroCore.getInstance().getPartnerManager();
        List<Partner> all = manager.getAllPartners();

        int slot = event.getSlot();
        if (slot < 0 || slot >= all.size()) return;

        Partner partner = all.get(slot);
        new PartnerRewardMenu(partner).open(player);
    }
}
